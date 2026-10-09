package com.example.CAMPUSDESK.Service;

import com.example.CAMPUSDESK.Dto.Request.*;
import com.example.CAMPUSDESK.Dto.Response.TicketResponseDTO;
import com.example.CAMPUSDESK.Entity.History;
import com.example.CAMPUSDESK.Entity.Ticket;
import com.example.CAMPUSDESK.Entity.User;
import com.example.CAMPUSDESK.Enums.Category;
import com.example.CAMPUSDESK.Enums.Priority;
import com.example.CAMPUSDESK.Enums.Role;
import com.example.CAMPUSDESK.Enums.TicketStatus;
import com.example.CAMPUSDESK.Exception.BadRequestException;
import com.example.CAMPUSDESK.Exception.InvalidStatusTransitionException;
import com.example.CAMPUSDESK.Exception.ResourceNotFoundException;
import com.example.CAMPUSDESK.Exception.UnauthorizedAccessException;
import com.example.CAMPUSDESK.Repository.HistoryRepository;
import com.example.CAMPUSDESK.Repository.TicketRepository;
import com.example.CAMPUSDESK.Repository.UserRepository;
import com.example.CAMPUSDESK.Security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Lógica de negocio de tickets (RF-03, RF-04). TODAS las reglas del ciclo de
 * vida se validan aquí, nunca en los controllers (RT-01).
 */
@Service
@RequiredArgsConstructor
public class TicketServiceImpl implements TicketService {

    private static final Sort DEFAULT_SORT = Sort.by(Sort.Direction.DESC, "fechaCreacion");

    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final HistoryRepository historyRepository;

    // ---------------- Creación (CP-06) ----------------

    @Override
    @Transactional
    public TicketResponseDTO create(TicketCreateDTO dto, UserPrincipal me) {
        User solicitante = loadUser(me.getId());
        Ticket ticket = Ticket.builder()
                .titulo(dto.titulo().trim())
                .descripcion(dto.descripcion().trim())
                .categoria(dto.categoria())
                .prioridad(dto.prioridad())
                .estado(TicketStatus.ABIERTA) // Regla 1: siempre inicia ABIERTA
                .solicitante(solicitante)
                .build();
        ticket = ticketRepository.save(ticket);
        // Registro inicial del historial (RF-06): creación => ABIERTA
        historyRepository.save(History.builder()
                .ticket(ticket)
                .estadoAnterior(null)
                .estadoNuevo(TicketStatus.ABIERTA)
                .usuarioResponsable(solicitante)
                .build());
        return TicketResponseDTO.from(ticket);
    }

    // ---------------- Consultas con scope por rol (RF-08, CP-15) ----------------

    @Override
    @Transactional(readOnly = true)
    public List<TicketResponseDTO> findScoped(UserPrincipal me, TicketStatus estado,
                                              Priority prioridad, Category categoria) {
        List<Ticket> tickets = switch (me.getAuthorities().iterator().next().getAuthority()) {
            case "ROLE_ADMIN" ->
                    ticketRepository.filterAll(estado, prioridad, categoria, DEFAULT_SORT);
            case "ROLE_TECHNICIAN" ->
                    ticketRepository.filterByTecnico(me.getId(), estado, prioridad, categoria, DEFAULT_SORT);
            default ->
                    ticketRepository.filterBySolicitante(me.getId(), estado, prioridad, categoria, DEFAULT_SORT);
        };
        return tickets.stream().map(TicketResponseDTO::from).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public TicketResponseDTO findById(Long id, UserPrincipal me) {
        return TicketResponseDTO.from(loadVisibleTicket(id, me));
    }

    // ---------------- Edición de datos (RF-01 USER, CP-12) ----------------

    @Override
    @Transactional
    public TicketResponseDTO update(Long id, TicketUpdateDTO dto, UserPrincipal me) {
        Ticket ticket = loadTicket(id);
        if (ticket.getEstado().isClosed()) {
            throw new BadRequestException("Un ticket CERRADO es inmutable: no admite edición (CP-12)");
        }
        boolean isAdmin = hasRole(me, Role.ADMIN);
        if (!isAdmin) {
            // El USER solo puede editar sus propios tickets ABIERTA y sin técnico.
            if (!ticket.getSolicitante().getId().equals(me.getId())) {
                throw new UnauthorizedAccessException("No es propietario de este ticket");
            }
            if (ticket.getEstado() != TicketStatus.ABIERTA || ticket.getTecnicoAsignado() != null) {
                throw new BadRequestException(
                        "Solo se puede editar un ticket ABIERTA y sin técnico asignado");
            }
        }
        ticket.setTitulo(dto.titulo().trim());
        ticket.setDescripcion(dto.descripcion().trim());
        if (dto.categoria() != null) ticket.setCategoria(dto.categoria());
        if (dto.prioridad() != null) ticket.setPrioridad(dto.prioridad());
        return TicketResponseDTO.from(ticketRepository.save(ticket));
    }

    // ---------------- Asignación / Reasignación (RF-04 reglas 2 y 7, CP-07) ----------------

    @Override
    @Transactional
    public TicketResponseDTO assignTechnician(Long id, TicketAssignDTO dto, UserPrincipal me) {
        requireRole(me, Role.ADMIN, "Solo el ADMIN puede asignar o reasignar técnicos");
        Ticket ticket = loadTicket(id);
        if (ticket.getEstado().isClosed()) {
            throw new BadRequestException("El ticket está CERRADO y es inmutable");
        }
        if (ticket.getEstado() != TicketStatus.ABIERTA && ticket.getEstado() != TicketStatus.ASIGNADA) {
            throw new BadRequestException("La asignación solo es posible en estados ABIERTA o ASIGNADA");
        }
        User tecnico = loadUser(dto.tecnicoId());
        if (tecnico.getRol() != Role.TECHNICIAN) {
            throw new BadRequestException("El usuario seleccionado no tiene rol TECHNICIAN");
        }
        TicketStatus anterior = ticket.getEstado();
        ticket.setTecnicoAsignado(tecnico);
        ticket.setEstado(TicketStatus.ASIGNADA); // Regla 2: ABIERTA -> ASIGNADA automática
        ticket = ticketRepository.save(ticket);
        saveHistory(ticket, anterior, TicketStatus.ASIGNADA, loadUser(me.getId()));
        return TicketResponseDTO.from(ticket);
    }

    // ---------------- Cambios de estado (RF-04, CP-08..CP-11) ----------------

    @Override
    @Transactional
    public TicketResponseDTO changeStatus(Long id, TicketStatusUpdateDTO dto, UserPrincipal me) {
        Ticket ticket = loadTicket(id);
        TicketStatus anterior = ticket.getEstado();
        TicketStatus nuevo = dto.nuevoEstado();

        // Regla 6: cerrado es inmutable.
        if (anterior.isClosed()) {
            throw new BadRequestException("El ticket está CERRADO y no admite cambios de estado");
        }
        // Regla 8: flujo estricto, sin saltos ni reversa.
        if (!anterior.canTransitionTo(nuevo)) {
            throw new InvalidStatusTransitionException(String.format(
                    "Transición ilegal: %s -> %s. El flujo obligatorio es " +
                            "ABIERTA -> ASIGNADA -> EN_PROCESO -> RESUELTA -> CERRADA", anterior, nuevo));
        }
        // Reglas 3, 4 y 5: quién puede ejecutar cada transición.
        switch (nuevo) {
            case EN_PROCESO, RESUELTA -> {
                requireRole(me, Role.TECHNICIAN, "Solo el técnico asignado puede avanzar este ticket");
                requireAssignedTechnician(ticket, me);
            }
            case CERRADA -> {
                if (!ticket.getSolicitante().getId().equals(me.getId())) {
                    throw new UnauthorizedAccessException(
                            "Solo el usuario solicitante puede cerrar su ticket RESUELTA");
                }
            }
            default -> throw new InvalidStatusTransitionException(
                    "Esta transición debe realizarse mediante la asignación de un técnico por el ADMIN");
        }
        ticket.setEstado(nuevo);
        ticket = ticketRepository.save(ticket);
        saveHistory(ticket, anterior, nuevo, loadUser(me.getId()));
        return TicketResponseDTO.from(ticket);
    }

    // ---------------- Helpers ----------------

    /** Valida que el ticket exista y sea visible para el rol autenticado (RF-02). */
    Ticket loadVisibleTicket(Long id, UserPrincipal me) {
        Ticket ticket = loadTicket(id);
        if (hasRole(me, Role.ADMIN)) return ticket;
        if (hasRole(me, Role.TECHNICIAN)) {
            if (ticket.getTecnicoAsignado() == null || !ticket.getTecnicoAsignado().getId().equals(me.getId())) {
                throw new UnauthorizedAccessException("El ticket no está asignado a usted");
            }
            return ticket;
        }
        if (!ticket.getSolicitante().getId().equals(me.getId())) {
            throw new UnauthorizedAccessException("No es propietario de este ticket");
        }
        return ticket;
    }

    private Ticket loadTicket(Long id) {
        return ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket no encontrado: " + id));
    }

    private User loadUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + id));
    }

    private void requireAssignedTechnician(Ticket ticket, UserPrincipal me) {
        if (ticket.getTecnicoAsignado() == null || !ticket.getTecnicoAsignado().getId().equals(me.getId())) {
            throw new UnauthorizedAccessException("Solo el técnico ASIGNADO a este ticket puede realizar esta acción");
        }
    }

    private boolean hasRole(UserPrincipal me, Role role) {
        return me.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_" + role.name()));
    }

    private void requireRole(UserPrincipal me, Role role, String message) {
        if (!hasRole(me, role)) {
            throw new UnauthorizedAccessException(message);
        }
    }

    /** RF-06 regla 9: el historial se escribe en la misma transacción que el cambio de estado. */
    private void saveHistory(Ticket ticket, TicketStatus anterior, TicketStatus nuevo, User responsable) {
        historyRepository.save(History.builder()
                .ticket(ticket)
                .estadoAnterior(anterior)
                .estadoNuevo(nuevo)
                .usuarioResponsable(responsable)
                .build());
    }
}

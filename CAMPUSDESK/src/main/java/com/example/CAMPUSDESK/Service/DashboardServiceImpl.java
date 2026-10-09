package com.example.CAMPUSDESK.Service;

import com.example.CAMPUSDESK.Dto.Response.DashboardMetricsDTO;
import com.example.CAMPUSDESK.Enums.Role;
import com.example.CAMPUSDESK.Enums.TicketStatus;
import com.example.CAMPUSDESK.Repository.TicketRepository;
import com.example.CAMPUSDESK.Security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Métricas del dashboard procesadas en PostgreSQL (RF-07). */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {

    private final TicketRepository ticketRepository;

    @Override
    public DashboardMetricsDTO metrics(UserPrincipal me) {
        // ADMIN => userId null (global). USER/TECHNICIAN => solo sus tickets.
        Long scopeUserId = hasRole(me, Role.ADMIN) ? null : me.getId();

        long total   = countScoped(scopeUserId, null);
        long abierta = countScoped(scopeUserId, TicketStatus.ABIERTA);
        long asignada = countScoped(scopeUserId, TicketStatus.ASIGNADA);
        long proceso = countScoped(scopeUserId, TicketStatus.EN_PROCESO);
        long resuelta = countScoped(scopeUserId, TicketStatus.RESUELTA);
        long cerrada = countScoped(scopeUserId, TicketStatus.CERRADA);

        return new DashboardMetricsDTO(total, abierta, asignada, proceso, resuelta, cerrada);
    }

    private long countScoped(Long userId, TicketStatus estado) {
        return estado == null
                ? (userId == null ? ticketRepository.count() : ticketRepository.countScoped(userId))
                : ticketRepository.countByEstadoScoped(userId, estado);
    }

    private boolean hasRole(UserPrincipal me, Role role) {
        return me.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_" + role.name()));
    }
}

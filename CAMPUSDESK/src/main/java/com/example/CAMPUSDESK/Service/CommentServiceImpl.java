package com.example.CAMPUSDESK.Service;

import com.example.CAMPUSDESK.Dto.Request.CommentCreateDTO;
import com.example.CAMPUSDESK.Dto.Response.CommentResponseDTO;
import com.example.CAMPUSDESK.Entity.Comment;
import com.example.CAMPUSDESK.Entity.Ticket;
import com.example.CAMPUSDESK.Exception.BadRequestException;
import com.example.CAMPUSDESK.Repository.CommentRepository;
import com.example.CAMPUSDESK.Repository.UserRepository;
import com.example.CAMPUSDESK.Security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Comentarios de tickets (RF-05, CP-13). */
@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final TicketServiceImpl ticketService; // reutiliza loadVisibleTicket (mismo paquete)
    private final UserRepository userRepository;

    @Override
    @Transactional
    public CommentResponseDTO create(Long ticketId, CommentCreateDTO dto, UserPrincipal me) {
        Ticket ticket = ticketService.loadVisibleTicket(ticketId, me);
        if (ticket.getEstado().isClosed()) {
            throw new BadRequestException("No se pueden agregar comentarios a un ticket CERRADO");
        }
        Comment comment = Comment.builder()
                .contenido(dto.contenido().trim())
                .autor(userRepository.findById(me.getId())
                        .orElseThrow(() -> new BadRequestException("Usuario autenticado no encontrado")))
                .ticket(ticket)
                .build();
        return CommentResponseDTO.from(commentRepository.save(comment));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentResponseDTO> findByTicket(Long ticketId, UserPrincipal me) {
        ticketService.loadVisibleTicket(ticketId, me); // valida rol + propiedad
        return commentRepository.findByTicketId(ticketId, Sort.by(Sort.Direction.ASC, "fechaCreacion"))
                .stream().map(CommentResponseDTO::from).toList();
    }
}

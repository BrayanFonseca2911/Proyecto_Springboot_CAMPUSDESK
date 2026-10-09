package com.example.CAMPUSDESK.Service;

import com.example.CAMPUSDESK.Dto.Response.HistoryResponseDTO;
import com.example.CAMPUSDESK.Repository.HistoryRepository;
import com.example.CAMPUSDESK.Security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Historial de cambios: solo lectura, inmutable (RF-06, CP-14). */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HistoryServiceImpl implements HistoryService {

    private final HistoryRepository historyRepository;
    private final TicketServiceImpl ticketService;

    @Override
    public List<HistoryResponseDTO> findByTicket(Long ticketId, UserPrincipal me) {
        ticketService.loadVisibleTicket(ticketId, me); // valida rol + propiedad
        return historyRepository.findByTicketIdOrderByFechaHoraAsc(ticketId)
                .stream().map(HistoryResponseDTO::from).toList();
    }
}

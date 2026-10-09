package com.example.CAMPUSDESK.Service;

import com.example.CAMPUSDESK.Dto.Response.HistoryResponseDTO;
import com.example.CAMPUSDESK.Security.UserPrincipal;

import java.util.List;

public interface HistoryService {
    /** Solo lectura: el historial es inmutable desde la interfaz (RF-06). */
    List<HistoryResponseDTO> findByTicket(Long ticketId, UserPrincipal me);
}

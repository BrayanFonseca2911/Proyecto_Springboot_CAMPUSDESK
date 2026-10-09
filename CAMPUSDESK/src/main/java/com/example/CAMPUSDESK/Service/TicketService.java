package com.example.CAMPUSDESK.Service;

import com.example.CAMPUSDESK.Dto.Request.*;
import com.example.CAMPUSDESK.Dto.Response.TicketResponseDTO;
import com.example.CAMPUSDESK.Enums.Category;
import com.example.CAMPUSDESK.Enums.Priority;
import com.example.CAMPUSDESK.Enums.TicketStatus;
import com.example.CAMPUSDESK.Security.UserPrincipal;

import java.util.List;

public interface TicketService {
    TicketResponseDTO create(TicketCreateDTO dto, UserPrincipal me);
    List<TicketResponseDTO> findScoped(UserPrincipal me, TicketStatus estado, Priority prioridad, Category categoria);
    TicketResponseDTO findById(Long id, UserPrincipal me);
    TicketResponseDTO update(Long id, TicketUpdateDTO dto, UserPrincipal me);
    TicketResponseDTO assignTechnician(Long id, TicketAssignDTO dto, UserPrincipal me);
    TicketResponseDTO changeStatus(Long id, TicketStatusUpdateDTO dto, UserPrincipal me);
}

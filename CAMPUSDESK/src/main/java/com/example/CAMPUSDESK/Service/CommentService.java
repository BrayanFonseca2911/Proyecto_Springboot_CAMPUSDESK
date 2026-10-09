package com.example.CAMPUSDESK.Service;

import com.example.CAMPUSDESK.Dto.Request.CommentCreateDTO;
import com.example.CAMPUSDESK.Dto.Response.CommentResponseDTO;
import com.example.CAMPUSDESK.Security.UserPrincipal;

import java.util.List;

public interface CommentService {
    CommentResponseDTO create(Long ticketId, CommentCreateDTO dto, UserPrincipal me);
    List<CommentResponseDTO> findByTicket(Long ticketId, UserPrincipal me);
}

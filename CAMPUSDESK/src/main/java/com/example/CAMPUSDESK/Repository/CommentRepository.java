package com.example.CAMPUSDESK.Repository;

import com.example.CAMPUSDESK.Entity.Comment;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    /** Comentarios de un ticket en orden cronológico (RF-05). */
    List<Comment> findByTicketId(Long ticketId, Sort sort);
}

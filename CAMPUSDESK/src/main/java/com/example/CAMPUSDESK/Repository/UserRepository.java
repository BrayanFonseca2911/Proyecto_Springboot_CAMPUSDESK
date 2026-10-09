package com.example.CAMPUSDESK.Repository;

import com.example.CAMPUSDESK.Entity.User;
import com.example.CAMPUSDESK.Enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    /** Lista de técnicos disponibles para asignación (RF-01 ADMIN). */
    List<User> findByRol(Role rol);
}

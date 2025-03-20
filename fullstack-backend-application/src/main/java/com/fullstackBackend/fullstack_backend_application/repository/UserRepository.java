package com.fullstackBackend.fullstack_backend_application.repository;

import com.fullstackBackend.fullstack_backend_application.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
}
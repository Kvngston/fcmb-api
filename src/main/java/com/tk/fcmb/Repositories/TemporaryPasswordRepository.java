package com.tk.fcmb.Repositories;

import com.tk.fcmb.Entities.TemporaryPassword;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface TemporaryPasswordRepository extends JpaRepository<TemporaryPassword, Long> {

    Optional<TemporaryPassword> findByGeneratedPassword(String generatedPassword);
}

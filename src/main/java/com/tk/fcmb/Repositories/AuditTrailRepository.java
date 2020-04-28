package com.tk.fcmb.Repositories;

import com.tk.fcmb.Entities.AuditTrail;
import com.tk.fcmb.Entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface AuditTrailRepository extends JpaRepository<AuditTrail, Long> {

    List<AuditTrail> findAllByCreatedAtDate(LocalDate date);
    List<AuditTrail> findAllByUser(User user);
    List<AuditTrail> findAllByIpAddress(String ipAddress);

}

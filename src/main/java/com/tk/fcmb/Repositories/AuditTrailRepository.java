package com.tk.fcmb.Repositories;

import com.tk.fcmb.Entities.AuditTrail;
import com.tk.fcmb.Entities.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface AuditTrailRepository extends CrudRepository<AuditTrail, Long> {

    Page<AuditTrail> findAllByCreatedAtDate(LocalDate date, Pageable pageable);
    Page<AuditTrail> findAllByUser(User user, Pageable pageable);
    Page<AuditTrail> findAllByIpAddress(String ipAddress, Pageable pageable);
    Page<AuditTrail> findAll(Pageable pageable);

    @Override
    List<AuditTrail> findAll();

}

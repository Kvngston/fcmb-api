package com.tk.fcmb.Service;

import com.tk.fcmb.Entities.AuditTrail;
import com.tk.fcmb.Entities.dto.AuditTrailDto;
import com.tk.fcmb.Enums.RoleType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface AuditTrailService {

    void createNewEvent(AuditTrailDto auditTrailDto);
    List<AuditTrail> getAllAuditTrails();
    List<AuditTrail> getAllAuditTrailsByDate(LocalDate date);
    List<AuditTrail> getAllByRole(RoleType roleType);
    List<AuditTrail> getAllByStaffId(String staffId);
    List<AuditTrail> getAllByIpAddress(String ipAddress);


}

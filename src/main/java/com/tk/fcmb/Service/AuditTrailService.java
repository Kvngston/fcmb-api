package com.tk.fcmb.Service;

import com.tk.fcmb.Entities.dto.AuditTrailDto;
import com.tk.fcmb.Enums.RoleType;
import org.springframework.data.domain.Page;

import java.time.LocalDate;

public interface AuditTrailService {

    void createNewEvent(AuditTrailDto auditTrailDto);
    Page<?> getAllAuditTrails(int page, int size);
    Page<?> getAllAuditTrailsByDate(LocalDate date, int page, int size);
    Page<?> getAllByRole(RoleType roleType, int page, int size);
    Page<?> getAllByStaffId(String staffId, int page, int size);
    Page<?> getAllByIpAddress(String ipAddress, int page, int size);


}

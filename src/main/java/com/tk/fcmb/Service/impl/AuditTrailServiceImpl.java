package com.tk.fcmb.Service.impl;

import com.tk.fcmb.Entities.AuditTrail;
import com.tk.fcmb.Entities.User;
import com.tk.fcmb.Entities.dto.AuditTrailDto;
import com.tk.fcmb.Enums.RoleType;
import com.tk.fcmb.Job.IpAddressGetter;
import com.tk.fcmb.Repositories.AuditTrailRepository;
import com.tk.fcmb.Repositories.UserRepository;
import com.tk.fcmb.Service.AuditTrailService;
import com.tk.fcmb.utils.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AuditTrailServiceImpl implements AuditTrailService {

    @Autowired
    private AuditTrailRepository auditTrailRepository;

    @Autowired
    private UserRepository userRepository;


    @Override
    public void createNewEvent(AuditTrailDto auditTrailDto) {
        AuditTrail auditTrail = new AuditTrail();
        auditTrail.setTitle(auditTrailDto.getTitle());
        auditTrail.setTransactionDetails(auditTrailDto.getTransactionDetails());
        auditTrail.setUser(userRepository.findByStaffId(auditTrailDto.getStaffId()));
        auditTrail.setIpAddress(auditTrailDto.getIpAddress());

        auditTrailRepository.save(auditTrail);
    }

    @Override
    public List<AuditTrail> getAllAuditTrails() {
        return auditTrailRepository.findAll();
    }

    @Override
    public List<AuditTrail> getAllAuditTrailsByDate(LocalDate date) {

        return auditTrailRepository.findAllByCreatedAtDate(date);
    }

    @Override
    public List<AuditTrail> getAllByRole(RoleType roleType) {
        List<AuditTrail> auditTrails = getAllAuditTrails();

        return auditTrails
                .stream()
                .filter(auditTrail -> auditTrail.getUser().getRole().getRoleType() == roleType).collect(Collectors.toList());


    }

    @Override
    public List<AuditTrail> getAllByStaffId(String staffId) {

        User user = userRepository.findByStaffId(staffId);

        if (user == null) {
            return null;
        }

        return auditTrailRepository.findAllByUser(user);
    }

    @Override
    public List<AuditTrail> getAllByIpAddress(String ipAddress) {
        return auditTrailRepository.findAllByIpAddress(ipAddress);
    }
}

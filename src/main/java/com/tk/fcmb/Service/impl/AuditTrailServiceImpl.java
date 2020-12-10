package com.tk.fcmb.Service.impl;

import com.tk.fcmb.Entities.AuditTrail;
import com.tk.fcmb.Entities.User;
import com.tk.fcmb.Entities.dto.AuditTrailDto;
import com.tk.fcmb.Enums.RoleType;
import com.tk.fcmb.Repositories.AuditTrailRepository;
import com.tk.fcmb.Repositories.UserRepository;
import com.tk.fcmb.Service.AuditTrailService;
import com.tk.fcmb.utils.ConvertToPageable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
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
    public Page<?> getAllAuditTrails(int page, int size) {
        //ConvertToPageable convertToPageable = new ConvertToPageable();
        Pageable pageable = PageRequest.of(page, size);
        Page<AuditTrail> auditTrails = auditTrailRepository.findAll(pageable);
        auditTrails.forEach(auditTrail -> auditTrail.getUser().setPassword(""));
        return auditTrails;
    }

    @Override
    public Page<?> getAllAuditTrailsByDate(LocalDate date, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<AuditTrail> auditTrails = auditTrailRepository.findAllByCreatedAtDate(date,pageable);
        auditTrails.forEach(auditTrail -> auditTrail.getUser().setPassword(""));
        return auditTrails;
    }

    @Override
    public Page<?> getAllByRole(RoleType roleType, int page, int size) {

        List<AuditTrail> auditTrails = auditTrailRepository.findAll();
        ConvertToPageable convertToPageable = new ConvertToPageable(page,size);
        Page<AuditTrail> auditTrailPage = (Page<AuditTrail>)  convertToPageable.convertListToPage(auditTrails
                .stream()
                .filter(auditTrail -> auditTrail.getUser().getRole().getRoleType() == roleType)
                .collect(Collectors.toList()));

        auditTrailPage.forEach(auditTrail -> auditTrail.getUser().setPassword(""));
        return auditTrailPage;
    }

    @Override
    public Page<?> getAllByStaffId(String staffId, int page, int size) {

        Pageable pageable = PageRequest.of(page, size);

        User user = userRepository.findByStaffId(staffId);

        if (user == null) {
            return null;
        }
        Page<AuditTrail> auditTrails = auditTrailRepository.findAllByUser(user,pageable);

        auditTrails.forEach(auditTrail -> auditTrail.getUser().setPassword(""));

        return auditTrails;
    }

    @Override
    public Page<?> getAllByIpAddress(String ipAddress, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        Page<AuditTrail> auditTrails = auditTrailRepository.findAllByIpAddress(ipAddress,pageable);
        auditTrails.forEach(auditTrail -> auditTrail.getUser().setPassword(""));
        return auditTrails;
    }
}

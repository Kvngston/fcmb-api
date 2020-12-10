package com.tk.fcmb.Repositories;

import com.tk.fcmb.Entities.ProcessApprovalRequest;
import com.tk.fcmb.Enums.ProcessType;
import com.tk.fcmb.Enums.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProcessApprovalRequestRepository extends JpaRepository<ProcessApprovalRequest, Long> {

    ProcessApprovalRequest findByTrackingNumber(String trackingNumber);
    List<ProcessApprovalRequest> findAllByMobileNumberAndProcessTypeAndApproved(String mobileNumber, ProcessType processType, boolean check);
    List<ProcessApprovalRequest> findAllByMobileNumberAndApproved(String mobileNumber, boolean check);
    List<ProcessApprovalRequest> findAllByRequestStatus(RequestStatus check);
}

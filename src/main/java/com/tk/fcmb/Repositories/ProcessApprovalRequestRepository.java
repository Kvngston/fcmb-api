package com.tk.fcmb.Repositories;

import com.tk.fcmb.Entities.ProcessApprovalRequest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcessApprovalRequestRepository extends JpaRepository<ProcessApprovalRequest, Long> {

    ProcessApprovalRequest findByTrackingNumber(String trackingNumber);

}

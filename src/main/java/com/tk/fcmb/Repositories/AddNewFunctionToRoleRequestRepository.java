package com.tk.fcmb.Repositories;

import com.tk.fcmb.Entities.AddNewFunctionToRoleRequest;
import com.tk.fcmb.Enums.RequestStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AddNewFunctionToRoleRequestRepository extends JpaRepository<AddNewFunctionToRoleRequest, Long> {

    AddNewFunctionToRoleRequest findByTicketNumber(String ticketNumber);
    Page<AddNewFunctionToRoleRequest> findAllByRequestStatus(RequestStatus check, Pageable pageable);
}

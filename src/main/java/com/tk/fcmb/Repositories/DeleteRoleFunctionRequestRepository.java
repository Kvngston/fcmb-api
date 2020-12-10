package com.tk.fcmb.Repositories;

import com.tk.fcmb.Entities.AddNewFunctionToRoleRequest;
import com.tk.fcmb.Entities.DeleteRoleFunctionRequest;
import com.tk.fcmb.Enums.RequestStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeleteRoleFunctionRequestRepository extends JpaRepository<DeleteRoleFunctionRequest, Long> {

    DeleteRoleFunctionRequest findByTicketNumber(String ticketNumber);

    Page<DeleteRoleFunctionRequest> findAllByRequestStatus(RequestStatus check, Pageable pageable);
}

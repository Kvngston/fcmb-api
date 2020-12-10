package com.tk.fcmb.Repositories;

import com.tk.fcmb.Entities.ChangeAllRoleFunctionsRequest;
import com.tk.fcmb.Enums.RequestStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChangedAllRoleFunctionsRequestRepository extends JpaRepository<ChangeAllRoleFunctionsRequest, Long> {

    ChangeAllRoleFunctionsRequest findByTicketNumber(String ticketNumber);
    Page<ChangeAllRoleFunctionsRequest> findAllByRequestStatus(RequestStatus check, Pageable pageable);

}

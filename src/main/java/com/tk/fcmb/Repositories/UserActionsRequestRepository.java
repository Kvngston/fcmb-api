package com.tk.fcmb.Repositories;

import com.tk.fcmb.Entities.User;
import com.tk.fcmb.Entities.UserActionsRequest;
import com.tk.fcmb.Enums.ActionType;
import com.tk.fcmb.Enums.RequestStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface UserActionsRequestRepository extends CrudRepository<UserActionsRequest, Long> {

    UserActionsRequest findByTicketNumber(String ticketNumber);
    Page<UserActionsRequest> findAllByRequestStatus(RequestStatus check, Pageable pageable);
    List<UserActionsRequest> findAllByUserAndAttendedToAndActionType(User user, boolean check, ActionType actionType);

}

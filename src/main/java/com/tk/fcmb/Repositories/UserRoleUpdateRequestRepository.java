package com.tk.fcmb.Repositories;

import com.tk.fcmb.Entities.User;
import com.tk.fcmb.Entities.UserRoleUpdateRequest;
import com.tk.fcmb.Enums.RequestStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface UserRoleUpdateRequestRepository extends CrudRepository<UserRoleUpdateRequest, Long> {

    UserRoleUpdateRequest findByTicketNumber(String ticketNumber);
    Page<UserRoleUpdateRequest> findAllByRequestStatus(RequestStatus check, Pageable pageable);
    List<UserRoleUpdateRequest> findAllByUserAndApproved(User user, boolean check);
}

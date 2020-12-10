package com.tk.fcmb.Repositories;

import com.tk.fcmb.Entities.Role;
import com.tk.fcmb.Enums.RequestStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Integer> {

    Role findByRoleName(String roleName);
    Role findByTicketNumber(String ticketNumber);
    Page<Role> findAllByRequestStatus(RequestStatus requestStatus, Pageable pageable);

}

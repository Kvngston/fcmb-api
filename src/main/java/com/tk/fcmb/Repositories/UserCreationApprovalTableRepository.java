package com.tk.fcmb.Repositories;

import com.tk.fcmb.Entities.User;
import com.tk.fcmb.Entities.UserCreationApprovalTable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserCreationApprovalTableRepository extends JpaRepository<UserCreationApprovalTable, Long> {


    UserCreationApprovalTable findByUser(User user);

}

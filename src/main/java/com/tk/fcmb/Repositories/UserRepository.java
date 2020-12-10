package com.tk.fcmb.Repositories;

import com.tk.fcmb.Entities.Role;
import com.tk.fcmb.Entities.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends CrudRepository<User, Long> {

    User findByStaffId(String staffId);
//    User findByEmailAndStaffIdAndPhoneNumber(String email, String staffId, String phoneNumber);
    List<User> findByRole(Role role);
    User findByEmail(String email);
    User findByPhoneNumber(String phoneNumber);
    User findById(long id);
    List<User> findAllByApproved(boolean check);
    @Override
    List<User> findAll();

    Page<User> findAll(Pageable pageable);

}

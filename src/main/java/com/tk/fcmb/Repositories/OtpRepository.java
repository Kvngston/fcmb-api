package com.tk.fcmb.Repositories;

import com.tk.fcmb.Entities.OTP;
import com.tk.fcmb.Entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OtpRepository extends JpaRepository<OTP, Long> {

    List<OTP> findAllByUser(User user);
    List<OTP> findAllByValid(boolean validity);
    OTP findById(long id);
    OTP findByUserAndValid(User user, boolean valid);


}

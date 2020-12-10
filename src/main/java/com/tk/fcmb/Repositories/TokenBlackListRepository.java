package com.tk.fcmb.Repositories;

import com.tk.fcmb.Entities.TokenBlackList;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TokenBlackListRepository  extends JpaRepository<TokenBlackList, Long> {

    TokenBlackList findByToken(String token);

}

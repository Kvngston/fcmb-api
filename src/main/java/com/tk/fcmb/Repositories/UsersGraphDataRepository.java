package com.tk.fcmb.Repositories;

import com.tk.fcmb.Entities.UsersGraphData;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsersGraphDataRepository extends JpaRepository<UsersGraphData, Integer> {
    UsersGraphData findByIdentifier(String identifier);

}

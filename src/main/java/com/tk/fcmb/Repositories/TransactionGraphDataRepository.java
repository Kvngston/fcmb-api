package com.tk.fcmb.Repositories;

import com.tk.fcmb.Entities.TransactionGraphData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionGraphDataRepository extends JpaRepository<TransactionGraphData, Integer> {
    TransactionGraphData findByIdentifier(String identifier);
}

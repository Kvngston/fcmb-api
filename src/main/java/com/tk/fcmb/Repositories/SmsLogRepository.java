package com.tk.fcmb.Repositories;

import com.tk.fcmb.Entities.SmsLog;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SmsLogRepository extends CrudRepository<SmsLog, Long> {
}

package com.tk.fcmb.Repositories;

import com.tk.fcmb.Entities.SmsLog;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SmsLogRepository extends CrudRepository<SmsLog, Long> {
    @Query("SELECT s FROM SmsLog s WHERE s.destinationMobile = :destinationMobile AND s.responseCode = :responseCode")
    Iterable<SmsLog> findSmsLogByDestinationMobileAndSuccessTokenAAndResponseCode(@Param("destinationMobile") String destinationMobile, @Param("responseCode") String responseCode);
}

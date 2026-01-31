package com.vansh.manger.Manger.Repository;

import com.vansh.manger.Manger.Entity.ActivityLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {


    List<ActivityLog> findTop10BySchool_IdOrderByCreatedAtDesc(Long schoolId);

    Page<ActivityLog> findBySchool_IdOrderByCreatedAtDesc(Long schoolId, Pageable pageable);

}

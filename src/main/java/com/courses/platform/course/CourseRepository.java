package com.courses.platform.course;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CourseRepository extends JpaRepository<Course, UUID>, JpaSpecificationExecutor<Course>  {

    @Modifying
    @Query("""
            UPDATE Course c
            SET c.occupiedSeats = c.occupiedSeats + 1
            WHERE c.id = :courseId
              AND c.status = :status
              AND c.occupiedSeats < c.maxSeats
            """)
    int reserveSeat(
            @Param("courseId") UUID courseId,
            @Param("status") CourseStatus status);


    @Modifying
    @Query("""
            UPDATE Course c
            SET c.occupiedSeats = c.occupiedSeats - 1
            WHERE c.id = :courseId
              AND c.occupiedSeats > 0
            """)
    int releaseSeat(@Param("courseId") UUID courseId);
}
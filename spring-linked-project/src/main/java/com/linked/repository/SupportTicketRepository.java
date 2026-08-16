package com.linked.repository;

import com.linked.model.entity.SupportTicket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SupportTicketRepository extends JpaRepository<SupportTicket, Long> {

    @Query("SELECT DISTINCT st FROM SupportTicket st " +
           "JOIN st.usuarios u " +
           "WHERE u.username = :username " +
           "ORDER BY st.creationTimestamp DESC")
    List<SupportTicket> findByUsername(@Param("username") String username);

    List<SupportTicket> findAllByOrderByCreationTimestampDesc();
}

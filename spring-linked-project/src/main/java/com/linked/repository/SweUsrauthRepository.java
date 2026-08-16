package com.linked.repository;

import com.linked.model.entity.SweUsrauth;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SweUsrauthRepository extends JpaRepository<SweUsrauth, Long> {

    Optional<SweUsrauth> findByUsrapl_Id(Long usraplId);

    Optional<SweUsrauth> findByResetPasswordToken(String token);

    void deleteByUsrapl_Id(Long usraplId);
}

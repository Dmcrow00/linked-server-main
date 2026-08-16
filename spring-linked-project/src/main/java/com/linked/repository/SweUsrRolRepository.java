package com.linked.repository;

import com.linked.model.entity.SweUsrrolapl;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SweUsrRolRepository extends JpaRepository<SweUsrrolapl, Long> {

    List<SweUsrrolapl> findByUsrapl_Id(Long usraplId);

    void deleteByUsrapl_Id(Long usraplId);
}

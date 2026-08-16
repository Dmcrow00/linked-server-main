package com.linked.repository;

import com.linked.model.entity.SweItemmenu;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SweItemmenuRepository extends JpaRepository<SweItemmenu, Long> {

    List<SweItemmenu> findByParentItemIsNullAndStatusOrderByOrderNumberAsc(String status);

    List<SweItemmenu> findByParentItem_IdAndStatusOrderByOrderNumberAsc(Long parentId, String status);
}

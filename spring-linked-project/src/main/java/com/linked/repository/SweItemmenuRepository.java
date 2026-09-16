package com.linked.repository;

import com.linked.model.entity.SweItemmenu;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/** Acceso a ítems del menú de navegación ({@link SweItemmenu}). */
public interface SweItemmenuRepository extends JpaRepository<SweItemmenu, Long> {

    /** Ítems de primer nivel (sin padre) con el estado indicado, en orden de visualización. */
    List<SweItemmenu> findByParentItemIsNullAndStatusOrderByOrderNumberAsc(String status);

    /** Hijos de un ítem con el estado indicado, en orden de visualización. */
    List<SweItemmenu> findByParentItem_IdAndStatusOrderByOrderNumberAsc(Long parentId, String status);
}

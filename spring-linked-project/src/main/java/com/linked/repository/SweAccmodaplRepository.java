package com.linked.repository;

import com.linked.model.entity.SweAccmodapl;
import com.linked.model.enums.PermissionType;
import com.linked.model.enums.ResourceType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/** Acceso a permisos por rol ({@link SweAccmodapl}). */
public interface SweAccmodaplRepository extends JpaRepository<SweAccmodapl, Long> {

    List<SweAccmodapl> findByRolapl_IdAndStatus(Long rolId, String status);

    boolean existsByRolapl_IdAndResourceTypeAndResourceNameAndProjectNameAndPermissionType(
            Long rolId, ResourceType resourceType, String resourceName,
            String projectName, PermissionType permissionType);
}

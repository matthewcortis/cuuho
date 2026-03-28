package com.backend.cuutro.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.backend.cuutro.entities.TinhNguyenVienEntity;

@Repository
public interface TinhNguyenVienRepository extends JpaRepository<TinhNguyenVienEntity, Long>, JpaSpecificationExecutor<TinhNguyenVienEntity> {
}

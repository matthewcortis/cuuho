package com.backend.cuutro.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.backend.cuutro.entities.DoiNhomTinhNguyenVienEntity;

@Repository
public interface DoiNhomTinhNguyenVienRepository extends JpaRepository<DoiNhomTinhNguyenVienEntity, Long>, JpaSpecificationExecutor<DoiNhomTinhNguyenVienEntity> {
	boolean existsByDoiNhom_IdAndTinhNguyenVien_Id(Long doiNhomId, Long tinhNguyenVienId);

	long countByTinhNguyenVien_Id(Long tinhNguyenVienId);
}

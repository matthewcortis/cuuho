package com.backend.cuutro.services;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.backend.cuutro.dto.request.PhieuCuuTroFilterRequest;
import com.backend.cuutro.entities.PhieuCuuTroEntity;
import com.backend.cuutro.repository.PhieuCuuTroRepository;
import com.backend.cuutro.repository.specification.PhieuCuuTroSpecifications;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PhieuCuuTroService {

	private final PhieuCuuTroRepository phieuCuuTroRepository;

	public Page<PhieuCuuTroEntity> search(PhieuCuuTroFilterRequest filter, Pageable pageable) {
		return phieuCuuTroRepository.findAll(PhieuCuuTroSpecifications.withFilter(filter), pageable);
	}

	public List<PhieuCuuTroEntity> search(PhieuCuuTroFilterRequest filter) {
		return phieuCuuTroRepository.findAll(PhieuCuuTroSpecifications.withFilter(filter));
	}
}

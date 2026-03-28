package com.backend.cuutro.services;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.backend.cuutro.dto.request.VatPhamFilterRequest;
import com.backend.cuutro.entities.VatPhamEntity;
import com.backend.cuutro.repository.VatPhamRepository;
import com.backend.cuutro.repository.specification.VatPhamSpecifications;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VatPhamService {

	private final VatPhamRepository vatPhamRepository;

	public Page<VatPhamEntity> search(VatPhamFilterRequest filter, Pageable pageable) {
		return vatPhamRepository.findAll(VatPhamSpecifications.withFilter(filter), pageable);
	}

	public List<VatPhamEntity> search(VatPhamFilterRequest filter) {
		return vatPhamRepository.findAll(VatPhamSpecifications.withFilter(filter));
	}
}

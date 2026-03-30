package com.backend.cuutro.services.impl;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.cuutro.dto.request.VatPhamFilterRequest;
import com.backend.cuutro.dto.request.VatPhamUpsertRequest;
import com.backend.cuutro.dto.response.entities.VatPhamDto;
import com.backend.cuutro.entities.DonViEntity;
import com.backend.cuutro.entities.NhomVatPhamEntity;
import com.backend.cuutro.entities.TepTinEntity;
import com.backend.cuutro.entities.VatPhamEntity;
import com.backend.cuutro.mapper.VatPhamMapper;
import com.backend.cuutro.repository.DonViRepository;
import com.backend.cuutro.repository.NhomVatPhamRepository;
import com.backend.cuutro.repository.TepTinRepository;
import com.backend.cuutro.repository.VatPhamRepository;
import com.backend.cuutro.repository.specification.VatPhamSpecifications;
import com.backend.cuutro.services.VatPhamService;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VatPhamServiceImpl implements VatPhamService {

	private final VatPhamRepository vatPhamRepository;
	private final DonViRepository donViRepository;
	private final NhomVatPhamRepository nhomVatPhamRepository;
	private final TepTinRepository tepTinRepository;
	private final VatPhamMapper vatPhamMapper;

	@Override
	public Page<VatPhamEntity> search(VatPhamFilterRequest filter, Pageable pageable) {
		return vatPhamRepository.findAll(VatPhamSpecifications.withFilter(filter), pageable);
	}

	@Override
	public List<VatPhamEntity> search(VatPhamFilterRequest filter) {
		return vatPhamRepository.findAll(VatPhamSpecifications.withFilter(filter));
	}

	@Override
	@Transactional
	public VatPhamDto create(VatPhamUpsertRequest request) {
		VatPhamEntity entity = new VatPhamEntity();
		applyRequest(entity, request);
		return vatPhamMapper.toDto(vatPhamRepository.save(entity));
	}

	@Override
	@Transactional
	public VatPhamDto update(Long id, VatPhamUpsertRequest request) {
		VatPhamEntity entity = getEntityOrThrow(id);
		applyRequest(entity, request);
		return vatPhamMapper.toDto(vatPhamRepository.save(entity));
	}

	@Override
	@Transactional
	public void delete(Long id) {
		VatPhamEntity entity = getEntityOrThrow(id);
		vatPhamRepository.delete(entity);
	}

	@Override
	public VatPhamDto getById(Long id) {
		return vatPhamMapper.toDto(getEntityOrThrow(id));
	}

	@Override
	public List<VatPhamDto> getAll() {
		return vatPhamMapper.toDtoList(vatPhamRepository.findAll(Sort.by(Sort.Direction.DESC, "id")));
	}

	private VatPhamEntity getEntityOrThrow(Long id) {
		return vatPhamRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("VatPham not found with id=" + id));
	}

	private DonViEntity getDonViOrThrow(Long id) {
		return donViRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("DonVi not found with id=" + id));
	}

	private NhomVatPhamEntity getNhomVatPhamOrThrow(Long id) {
		return nhomVatPhamRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("NhomVatPham not found with id=" + id));
	}

	private TepTinEntity getTepTinOrThrow(Long id) {
		return tepTinRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("TepTin not found with id=" + id));
	}

	private void applyRequest(VatPhamEntity entity, VatPhamUpsertRequest request) {
		entity.setTenVatPham(request.getTenVatPham().trim());
		entity.setSoLuong(request.getSoLuong());
		entity.setDonVi(getDonViOrThrow(request.getDonViId()));
		entity.setNhomVatPham(getNhomVatPhamOrThrow(request.getNhomVatPhamId()));
		entity.setTepTin(request.getTepTinId() == null ? null : getTepTinOrThrow(request.getTepTinId()));
		entity.setTrangThai(request.getTrangThai() == null ? Boolean.TRUE : request.getTrangThai());
	}
}


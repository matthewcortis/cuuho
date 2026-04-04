package com.backend.cuutro.services;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.backend.cuutro.constant.enums.RoleType;
import com.backend.cuutro.constant.enums.TrangThaiDoiNhom;
import com.backend.cuutro.constant.enums.TrangThaiPhieuHoTro;
import com.backend.cuutro.dto.request.CapNhatTrangThaiPhieuRequest;
import com.backend.cuutro.dto.request.DieuPhoiPhieuRequest;
import com.backend.cuutro.dto.request.PhieuCuuTroFilterRequest;
import com.backend.cuutro.dto.request.TaoChiTietCuuTroRequest;
import com.backend.cuutro.dto.request.TaoPhieuHoTroRequest;
import com.backend.cuutro.dto.response.entities.PhanCongDto;
import com.backend.cuutro.dto.response.entities.PhieuCuuTroChiTietDto;
import com.backend.cuutro.dto.response.entities.PhieuCuuTroDto;
import com.backend.cuutro.dto.response.entities.TinNhanDto;
import com.backend.cuutro.dto.response.entities.TrangThaiPhieuResponse;
import com.backend.cuutro.entities.ChiTietCuuTroEntity;
import com.backend.cuutro.entities.DoiNhomEntity;
import com.backend.cuutro.entities.NguoiDungEntity;
import com.backend.cuutro.entities.PhanCongEntity;
import com.backend.cuutro.entities.PhieuCuuTroEntity;
import com.backend.cuutro.entities.TinNhanEntity;
import com.backend.cuutro.exception.customize.InvalidFieldException;
import com.backend.cuutro.mapper.PhanCongMapper;
import com.backend.cuutro.mapper.PhieuCuuTroMapper;
import com.backend.cuutro.mapper.TinNhanMapper;
import com.backend.cuutro.repository.ChiTietCuuTroRepository;
import com.backend.cuutro.repository.DoiNhomRepository;
import com.backend.cuutro.repository.DoiNhomTinhNguyenVienRepository;
import com.backend.cuutro.repository.LoaiSuCoRepository;
import com.backend.cuutro.repository.NguoiDungRepository;
import com.backend.cuutro.repository.PhanCongRepository;
import com.backend.cuutro.repository.PhieuCuuTroRepository;
import com.backend.cuutro.repository.TepTinRepository;
import com.backend.cuutro.repository.TinNhanRepository;
import com.backend.cuutro.repository.VatPhamRepository;
import com.backend.cuutro.repository.ViTriRepository;
import com.backend.cuutro.repository.specification.PhieuCuuTroSpecifications;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PhieuCuuTroService {

	private static final String DOI_TRUONG_VAI_TRO = "truong_nhom";
	private static final String DOI_NHOM_TRANG_THAI_RANH_DB = "idle";
	private static final String DOI_NHOM_TRANG_THAI_HOAT_DONG_DB = "busy";
	private static final String PHIEU_TRANG_THAI_PENDING_DB = "pending";
	private static final String PHIEU_TRANG_THAI_ASSIGNED_DB = "assigned";
	private static final String PHIEU_TRANG_THAI_PROCESSING_DB = "processing";
	private static final String PHIEU_TRANG_THAI_DONE_DB = "done";
	private static final List<String> TRANG_THAI_PHIEU_KET_THUC = List.of(
			TrangThaiPhieuHoTro.HOAN_THANH.name(),
			TrangThaiPhieuHoTro.HUY.name());

	private final PhieuCuuTroRepository phieuCuuTroRepository;
	private final ChiTietCuuTroRepository chiTietCuuTroRepository;
	private final PhanCongRepository phanCongRepository;
	private final DoiNhomRepository doiNhomRepository;
	private final DoiNhomTinhNguyenVienRepository doiNhomTinhNguyenVienRepository;
	private final LoaiSuCoRepository loaiSuCoRepository;
	private final ViTriRepository viTriRepository;
	private final TepTinRepository tepTinRepository;
	private final VatPhamRepository vatPhamRepository;
	private final NguoiDungRepository nguoiDungRepository;
	private final TinNhanRepository tinNhanRepository;
	private final PhieuCuuTroMapper phieuCuuTroMapper;
	private final PhanCongMapper phanCongMapper;
	private final TinNhanMapper tinNhanMapper;

	@Transactional
	public PhieuCuuTroDto taoPhieuCongKhai(TaoPhieuHoTroRequest request) {
		PhieuCuuTroEntity entity = new PhieuCuuTroEntity();
		entity.setLoaiSuCo(loaiSuCoRepository.findById(request.getLoaiSuCoId())
				.orElseThrow(() -> new EntityNotFoundException("LoaiSuCo not found with id=" + request.getLoaiSuCoId())));
		entity.setViTri(viTriRepository.findById(request.getViTriId())
				.orElseThrow(() -> new EntityNotFoundException("ViTri not found with id=" + request.getViTriId())));
		entity.setTepTin(tepTinRepository.findById(request.getTepTinId())
				.orElseThrow(() -> new EntityNotFoundException("TepTin not found with id=" + request.getTepTinId())));
		entity.setNguoiDung(resolveNguoiDung(request.getNguoiDungId()));
		entity.setHoTen(request.getHoTen().trim());
		entity.setSdt(request.getSdt().trim());
		entity.setGhiChu(request.getGhiChu().trim());
		entity.setTrangThai(PHIEU_TRANG_THAI_PENDING_DB);
		PhieuCuuTroEntity savedPhieu = phieuCuuTroRepository.save(entity);
		List<ChiTietCuuTroEntity> chiTietCuuTro = buildChiTietCuuTro(savedPhieu, request.getChiTietCuuTro());
		chiTietCuuTroRepository.saveAll(chiTietCuuTro);
		PhieuCuuTroDto dto = phieuCuuTroMapper.toDto(savedPhieu);
		dto.setChiTietCuuTro(toChiTietDtos(chiTietCuuTro));
		return dto;
	}

	@Transactional
	public PhanCongDto dieuPhoi(Long phieuId, DieuPhoiPhieuRequest request) {
		PhieuCuuTroEntity phieu = getPhieuOrThrow(phieuId);
		TrangThaiPhieuHoTro trangThaiPhieu = getTrangThaiNghiepVu(
				phieu,
				phanCongRepository.findByPhieuCuuTro_Id(phieuId).orElse(null));
		if (trangThaiPhieu != TrangThaiPhieuHoTro.CHO_DIEU_PHOI) {
			throw new InvalidFieldException("Chi duoc dieu phoi phieu o trang thai CHO_DIEU_PHOI");
		}
		if (phanCongRepository.existsByPhieuCuuTro_Id(phieuId)) {
			throw new InvalidFieldException("Phieu da duoc dieu phoi");
		}

		DoiNhomEntity doiNhom = doiNhomRepository.findById(request.getDoiNhomId())
				.orElseThrow(() -> new EntityNotFoundException("DoiNhom not found with id=" + request.getDoiNhomId()));
		if (resolveTrangThaiDoiNhom(doiNhom) != TrangThaiDoiNhom.DANG_RANH) {
			throw new InvalidFieldException("Chi duoc dieu phoi doi nhom co trang thai DANG_RANH");
		}
		if (phanCongRepository.existsByDoiNhom_IdAndTrangThaiNotIn(
				doiNhom.getId(),
				TRANG_THAI_PHIEU_KET_THUC)) {
			throw new InvalidFieldException("Doi nhom dang co nhiem vu dang xu ly");
		}

		PhanCongEntity phanCong = new PhanCongEntity();
		phanCong.setPhieuCuuTro(phieu);
		phanCong.setDoiNhom(doiNhom);
		phanCong.setTrangThai(TrangThaiPhieuHoTro.CHO_DIEU_PHOI.name());

		doiNhom.setTrangThai(DOI_NHOM_TRANG_THAI_HOAT_DONG_DB);
		doiNhomRepository.save(doiNhom);
		return phanCongMapper.toDto(phanCongRepository.save(phanCong));
	}

	@Transactional
	public TrangThaiPhieuResponse nhanNhiemVu(Long phieuId) {
		PhanCongEntity phanCong = getPhanCongOrThrow(phieuId);
		validateCurrentUserLaDoiTruong(phanCong.getDoiNhom().getId());

		PhieuCuuTroEntity phieu = phanCong.getPhieuCuuTro();
		TrangThaiPhieuHoTro trangThaiHienTai = getTrangThaiNghiepVu(phieu, phanCong);
		if (trangThaiHienTai != TrangThaiPhieuHoTro.CHO_DIEU_PHOI) {
			throw new InvalidFieldException("Chi duoc nhan nhiem vu khi phieu dang CHO_DIEU_PHOI");
		}

		phieu.setTrangThai(PHIEU_TRANG_THAI_ASSIGNED_DB);
		phieuCuuTroRepository.save(phieu);
		phanCong.setTrangThai(TrangThaiPhieuHoTro.DA_NHAN.name());
		phanCongRepository.save(phanCong);
		return buildTrangThaiResponse(phieu, phanCong);
	}

	@Transactional
	public TrangThaiPhieuResponse capNhatTrangThai(Long phieuId, CapNhatTrangThaiPhieuRequest request) {
		PhanCongEntity phanCong = getPhanCongOrThrow(phieuId);
		validateCurrentUserLaDoiTruong(phanCong.getDoiNhom().getId());

		PhieuCuuTroEntity phieu = phanCong.getPhieuCuuTro();
		TrangThaiPhieuHoTro hienTai = getTrangThaiNghiepVu(phieu, phanCong);
		TrangThaiPhieuHoTro moi = parseTrangThai(request.getTrangThai());

		if (hienTai == moi) {
			return buildTrangThaiResponse(phieu, phanCong);
		}
		validateLuotTrangThai(hienTai, moi);

		phieu.setTrangThai(toDbTrangThai(moi));
		phieuCuuTroRepository.save(phieu);
		phanCong.setTrangThai(moi.name());
		phanCongRepository.save(phanCong);
		if (moi == TrangThaiPhieuHoTro.HOAN_THANH || moi == TrangThaiPhieuHoTro.HUY) {
			setDoiNhomDangRanh(phanCong.getDoiNhom());
		}
		return buildTrangThaiResponse(phieu, phanCong);
	}

	public TrangThaiPhieuResponse getTrangThai(Long phieuId) {
		PhieuCuuTroEntity phieu = getPhieuOrThrow(phieuId);
		return buildTrangThaiResponse(phieu, phanCongRepository.findByPhieuCuuTro_Id(phieuId).orElse(null));
	}

	public PhieuCuuTroDto getById(Long phieuId) {
		PhieuCuuTroEntity phieu = getPhieuOrThrow(phieuId);
		PhieuCuuTroDto dto = phieuCuuTroMapper.toDto(phieu);
		dto.setTrangThai(getTrangThaiNghiepVu(phieu, phanCongRepository.findByPhieuCuuTro_Id(phieuId).orElse(null)).name());
		dto.setChiTietCuuTro(toChiTietDtos(chiTietCuuTroRepository.findByPhieuCuuTro_IdOrderByIdAsc(phieuId)));
		return dto;
	}

	public List<PhieuCuuTroDto> getDanhSach() {
		List<PhieuCuuTroEntity> entities = phieuCuuTroRepository.findAllByOrderByCreatedAtDesc();
		List<PhieuCuuTroDto> dtoList = phieuCuuTroMapper.toDtoList(entities);
		for (int i = 0; i < entities.size(); i++) {
			PhieuCuuTroEntity entity = entities.get(i);
			TrangThaiPhieuHoTro trangThai = getTrangThaiNghiepVu(
					entity,
					phanCongRepository.findByPhieuCuuTro_Id(entity.getId()).orElse(null));
			dtoList.get(i).setTrangThai(trangThai.name());
		}
		return dtoList;
	}

	@Transactional
	public TinNhanDto guiTinNhan(Long phieuId, String noiDung) {
		if (!StringUtils.hasText(noiDung)) {
			throw new InvalidFieldException("noiDung is required");
		}

		PhieuCuuTroEntity phieu = getPhieuOrThrow(phieuId);
		Long taiKhoanId = getCurrentTaiKhoanIdOrThrow();
		NguoiDungEntity sender = getNguoiDungByTaiKhoanIdOrThrow(taiKhoanId);
		validateCoQuyenChat(phieu, sender);

		TinNhanEntity entity = new TinNhanEntity();
		entity.setPhieuCuuTro(phieu);
		entity.setSender(sender);
		entity.setNoiDung(noiDung.trim());
		entity.setLoaiTinNhan("text");
		entity.setMediaUrl(null);
		entity.setMediaType(null);
		return tinNhanMapper.toDto(tinNhanRepository.save(entity));
	}

	public List<TinNhanDto> getDanhSachTinNhan(Long phieuId) {
		PhieuCuuTroEntity phieu = getPhieuOrThrow(phieuId);
		Long taiKhoanId = getCurrentTaiKhoanIdOrThrow();
		NguoiDungEntity sender = getNguoiDungByTaiKhoanIdOrThrow(taiKhoanId);
		validateCoQuyenChat(phieu, sender);
		return tinNhanMapper.toDtoList(tinNhanRepository.findByPhieuCuuTro_IdOrderByCreatedAtAsc(phieuId));
	}

	public Page<PhieuCuuTroEntity> search(PhieuCuuTroFilterRequest filter, Pageable pageable) {
		return phieuCuuTroRepository.findAll(PhieuCuuTroSpecifications.withFilter(filter), pageable);
	}

	public List<PhieuCuuTroEntity> search(PhieuCuuTroFilterRequest filter) {
		return phieuCuuTroRepository.findAll(PhieuCuuTroSpecifications.withFilter(filter));
	}

	private PhieuCuuTroEntity getPhieuOrThrow(Long phieuId) {
		return phieuCuuTroRepository.findById(phieuId)
				.orElseThrow(() -> new EntityNotFoundException("PhieuCuuTro not found with id=" + phieuId));
	}

	private PhanCongEntity getPhanCongOrThrow(Long phieuId) {
		return phanCongRepository.findByPhieuCuuTro_Id(phieuId)
				.orElseThrow(() -> new InvalidFieldException("Phieu chua duoc dieu phoi doi nhom"));
	}

	private List<ChiTietCuuTroEntity> buildChiTietCuuTro(PhieuCuuTroEntity phieu, List<TaoChiTietCuuTroRequest> chiTietRequests) {
		Set<Long> vatPhamDaThem = new HashSet<>();
		List<ChiTietCuuTroEntity> chiTietEntities = new ArrayList<>(chiTietRequests.size());

		for (TaoChiTietCuuTroRequest chiTietRequest : chiTietRequests) {
			Long vatPhamId = chiTietRequest.getVatPhamId();
			if (!vatPhamDaThem.add(vatPhamId)) {
				throw new InvalidFieldException("Khong duoc trung vatPhamId trong chiTietCuuTro");
			}

			ChiTietCuuTroEntity chiTietEntity = new ChiTietCuuTroEntity();
			chiTietEntity.setPhieuCuuTro(phieu);
			chiTietEntity.setVatPham(vatPhamRepository.findById(vatPhamId)
					.orElseThrow(() -> new EntityNotFoundException("VatPham not found with id=" + vatPhamId)));
			chiTietEntity.setSoLuong(chiTietRequest.getSoLuong());
			chiTietEntity.setGhiChu(StringUtils.hasText(chiTietRequest.getGhiChu()) ? chiTietRequest.getGhiChu().trim() : null);
			chiTietEntities.add(chiTietEntity);
		}

		return chiTietEntities;
	}

	private List<PhieuCuuTroChiTietDto> toChiTietDtos(List<ChiTietCuuTroEntity> chiTietEntities) {
		return chiTietEntities.stream()
				.map(chiTiet -> PhieuCuuTroChiTietDto.builder()
						.id(chiTiet.getId())
						.vatPhamId(chiTiet.getVatPham() != null ? chiTiet.getVatPham().getId() : null)
						.tenVatPham(chiTiet.getVatPham() != null ? chiTiet.getVatPham().getTenVatPham() : null)
						.soLuong(chiTiet.getSoLuong())
						.ghiChu(chiTiet.getGhiChu())
						.createdAt(chiTiet.getCreatedAt())
						.build())
				.toList();
	}

	private void validateCurrentUserLaDoiTruong(Long doiNhomId) {
		NguoiDungEntity nguoiDung = getNguoiDungByTaiKhoanIdOrThrow(getCurrentTaiKhoanIdOrThrow());
		boolean laDoiTruong = doiNhomTinhNguyenVienRepository.existsByDoiNhom_IdAndTinhNguyenVien_NguoiDung_IdAndVaiTro(
				doiNhomId,
				nguoiDung.getId(),
				DOI_TRUONG_VAI_TRO);
		if (!laDoiTruong) {
			throw new InvalidFieldException("Ban khong phai truong nhom cua doi duoc dieu phoi");
		}
	}

	private NguoiDungEntity resolveNguoiDung(UUID nguoiDungIdFromRequest) {
		if (nguoiDungIdFromRequest != null) {
			return nguoiDungRepository.findById(nguoiDungIdFromRequest)
					.orElseThrow(() -> new EntityNotFoundException("NguoiDung not found with id=" + nguoiDungIdFromRequest));
		}

		Long taiKhoanId = getCurrentTaiKhoanId();
		if (taiKhoanId == null) {
			return null;
		}
		return nguoiDungRepository.findByTaiKhoan_Id(taiKhoanId).orElse(null);
	}

	private NguoiDungEntity getNguoiDungByTaiKhoanIdOrThrow(Long taiKhoanId) {
		return nguoiDungRepository.findByTaiKhoan_Id(taiKhoanId)
				.orElseThrow(() -> new InvalidFieldException("Khong tim thay thong tin nguoi dung tu tai khoan dang nhap"));
	}

	private Long getCurrentTaiKhoanIdOrThrow() {
		Long taiKhoanId = getCurrentTaiKhoanId();
		if (taiKhoanId == null) {
			throw new BadCredentialsException("Unauthorized");
		}
		return taiKhoanId;
	}

	private Long getCurrentTaiKhoanId() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || !(authentication.getPrincipal() instanceof Jwt jwt)) {
			return null;
		}
		Object claim = jwt.getClaim("taiKhoanId");
		if (claim == null) {
			return null;
		}
		if (claim instanceof Number number) {
			return number.longValue();
		}
		if (claim instanceof String text && StringUtils.hasText(text)) {
			return Long.parseLong(text);
		}
		return null;
	}

	private void validateCoQuyenChat(PhieuCuuTroEntity phieu, NguoiDungEntity sender) {
		if (isCurrentRole(RoleType.ADMIN)) {
			return;
		}

		if (isCurrentRole(RoleType.NGUOI_DAN)) {
			if (phieu.getNguoiDung() == null || !Objects.equals(phieu.getNguoiDung().getId(), sender.getId())) {
				throw new InvalidFieldException("Nguoi dan chi duoc chat tren phieu cua chinh minh");
			}
			return;
		}

		if (isCurrentRole(RoleType.TRUONG_NHOM_TNV)) {
			PhanCongEntity phanCong = getPhanCongOrThrow(phieu.getId());
			boolean laDoiTruong = doiNhomTinhNguyenVienRepository.existsByDoiNhom_IdAndTinhNguyenVien_NguoiDung_IdAndVaiTro(
					phanCong.getDoiNhom().getId(),
					sender.getId(),
					DOI_TRUONG_VAI_TRO);
			if (!laDoiTruong) {
				throw new InvalidFieldException("Ban khong duoc phep chat tren phieu nay");
			}
			return;
		}

		throw new InvalidFieldException("Role hien tai khong duoc phep chat");
	}

	private boolean isCurrentRole(RoleType roleType) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		return authentication != null && authentication.getAuthorities()
				.stream()
				.anyMatch(grantedAuthority -> roleType.name().equals(grantedAuthority.getAuthority()));
	}

	private void validateLuotTrangThai(TrangThaiPhieuHoTro hienTai, TrangThaiPhieuHoTro moi) {
		if (hienTai == TrangThaiPhieuHoTro.CHO_DIEU_PHOI) {
			throw new InvalidFieldException("Can nhan nhiem vu truoc khi cap nhat trang thai");
		}
		if (hienTai == TrangThaiPhieuHoTro.HOAN_THANH || hienTai == TrangThaiPhieuHoTro.HUY) {
			throw new InvalidFieldException("Phieu da ket thuc, khong the cap nhat trang thai");
		}
		if (moi == TrangThaiPhieuHoTro.CHO_DIEU_PHOI || moi == TrangThaiPhieuHoTro.DA_NHAN) {
			throw new InvalidFieldException("Trang thai khong hop le de cap nhat tu nghiep vu doi nhom");
		}
		if (moi == TrangThaiPhieuHoTro.HUY) {
			return;
		}
		if (hienTai == TrangThaiPhieuHoTro.DA_NHAN && moi == TrangThaiPhieuHoTro.DANG_TREN_DUONG_TOI) {
			return;
		}
		if (hienTai == TrangThaiPhieuHoTro.DANG_TREN_DUONG_TOI && moi == TrangThaiPhieuHoTro.DANG_XU_LY) {
			return;
		}
		if (hienTai == TrangThaiPhieuHoTro.DANG_XU_LY && moi == TrangThaiPhieuHoTro.HOAN_THANH) {
			return;
		}
		throw new InvalidFieldException("Khong the cap nhat trang thai tu " + hienTai.name() + " sang " + moi.name());
	}

	private TrangThaiPhieuHoTro parseTrangThai(String raw) {
		if (!StringUtils.hasText(raw)) {
			throw new InvalidFieldException("trangThai phieu khong hop le");
		}
		String normalized = raw.trim().toUpperCase();
		return switch (normalized) {
			case "PENDING" -> TrangThaiPhieuHoTro.CHO_DIEU_PHOI;
			case "ASSIGNED", "ACCEPTED" -> TrangThaiPhieuHoTro.DA_NHAN;
			case "PROCESSING" -> TrangThaiPhieuHoTro.DANG_XU_LY;
			case "DONE" -> TrangThaiPhieuHoTro.HOAN_THANH;
			case "CHO_DIEU_PHOI", "DA_NHAN", "DANG_TREN_DUONG_TOI", "DANG_XU_LY", "HOAN_THANH", "HUY" ->
				TrangThaiPhieuHoTro.valueOf(normalized);
			default -> throw new InvalidFieldException(
					"trangThai must be one of: CHO_DIEU_PHOI, DA_NHAN, DANG_TREN_DUONG_TOI, DANG_XU_LY, HOAN_THANH, HUY");
		};
	}

	private TrangThaiDoiNhom resolveTrangThaiDoiNhom(DoiNhomEntity doiNhom) {
		if (!Boolean.TRUE.equals(doiNhom.getTrangThaiHoatDong()) || !Boolean.TRUE.equals(doiNhom.getActive())) {
			return TrangThaiDoiNhom.NGUNG_HOAT_DONG;
		}
		String rawStatus = doiNhom.getTrangThai();
		if (DOI_NHOM_TRANG_THAI_HOAT_DONG_DB.equalsIgnoreCase(rawStatus)) {
			return TrangThaiDoiNhom.DANG_HOAT_DONG;
		}
		return TrangThaiDoiNhom.DANG_RANH;
	}

	private void setDoiNhomDangRanh(DoiNhomEntity doiNhom) {
		doiNhom.setTrangThai(DOI_NHOM_TRANG_THAI_RANH_DB);
		doiNhomRepository.save(doiNhom);
	}

	private TrangThaiPhieuHoTro getTrangThaiNghiepVu(PhieuCuuTroEntity phieu, PhanCongEntity phanCong) {
		if (phanCong != null && StringUtils.hasText(phanCong.getTrangThai())) {
			return parseTrangThai(phanCong.getTrangThai());
		}
		return parseTrangThai(phieu.getTrangThai());
	}

	private String toDbTrangThai(TrangThaiPhieuHoTro trangThai) {
		return switch (trangThai) {
			case CHO_DIEU_PHOI -> PHIEU_TRANG_THAI_PENDING_DB;
			case DA_NHAN -> PHIEU_TRANG_THAI_ASSIGNED_DB;
			case DANG_TREN_DUONG_TOI, DANG_XU_LY -> PHIEU_TRANG_THAI_PROCESSING_DB;
			case HOAN_THANH, HUY -> PHIEU_TRANG_THAI_DONE_DB;
		};
	}

	private TrangThaiPhieuResponse buildTrangThaiResponse(PhieuCuuTroEntity phieu, PhanCongEntity phanCong) {
		return TrangThaiPhieuResponse.builder()
				.phieuId(phieu.getId())
				.trangThai(getTrangThaiNghiepVu(phieu, phanCong).name())
				.build();
	}
}

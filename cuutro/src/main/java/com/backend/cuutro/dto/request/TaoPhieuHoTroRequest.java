package com.backend.cuutro.dto.request;

import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TaoPhieuHoTroRequest {

	@NotNull(message = "loaiSuCoId is required")
	Long loaiSuCoId;

	@NotNull(message = "viTriId is required")
	Long viTriId;

	@NotNull(message = "tepTinId is required")
	Long tepTinId;

	@NotBlank(message = "hoTen is required")
	String hoTen;

	@NotBlank(message = "sdt is required")
	String sdt;

	@NotBlank(message = "ghiChu is required")
	String ghiChu;

	@NotEmpty(message = "chiTietCuuTro is required")
	List<@Valid TaoChiTietCuuTroRequest> chiTietCuuTro;

	UUID nguoiDungId;
}

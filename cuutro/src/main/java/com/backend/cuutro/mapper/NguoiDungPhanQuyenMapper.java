package com.backend.cuutro.mapper;

import com.backend.cuutro.dto.response.entities.NguoiDungPhanQuyenDto;
import com.backend.cuutro.entities.NguoiDungPhanQuyenEntity;
import org.mapstruct.Mapper;
import org.springframework.data.domain.Page;

import java.util.List;

@Mapper(componentModel = "spring", uses = {PhanQuyenMapper.class})
public interface NguoiDungPhanQuyenMapper {

    NguoiDungPhanQuyenDto toDto(NguoiDungPhanQuyenEntity entity);

    NguoiDungPhanQuyenEntity toEntity(NguoiDungPhanQuyenDto dto);

    List<NguoiDungPhanQuyenDto> toDtoList(List<NguoiDungPhanQuyenEntity> entityList);

    List<NguoiDungPhanQuyenEntity> toEntityList(List<NguoiDungPhanQuyenDto> dtoList);

    default Page<NguoiDungPhanQuyenDto> toDtoPage(Page<NguoiDungPhanQuyenEntity> entityPage){
        if(entityPage == null){
            return Page.empty();
        }
        return entityPage.map(this::toDto);
    }
}

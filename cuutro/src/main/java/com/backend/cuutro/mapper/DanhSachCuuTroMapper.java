package com.backend.cuutro.mapper;

import com.backend.cuutro.dto.response.entities.DanhSachCuuTroDto;
import com.backend.cuutro.entities.DanhSachCuuTroEntity;
import org.mapstruct.Mapper;
import org.springframework.data.domain.Page;

import java.util.List;

@Mapper(componentModel = "spring", uses = {VatPhamMapper.class})
public interface DanhSachCuuTroMapper {

    DanhSachCuuTroDto toDto(DanhSachCuuTroEntity entity);

    DanhSachCuuTroEntity toEntity(DanhSachCuuTroDto dto);

    List<DanhSachCuuTroDto> toDtoList(List<DanhSachCuuTroEntity> entityList);

    List<DanhSachCuuTroEntity> toEntityList(List<DanhSachCuuTroDto> dtoList);

    default Page<DanhSachCuuTroDto> toDtoPage(Page<DanhSachCuuTroEntity> entityPage){
        if(entityPage == null){
            return Page.empty();
        }
        return entityPage.map(this::toDto);
    }
}

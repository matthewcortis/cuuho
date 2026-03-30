package com.backend.cuutro.mapper;

import com.backend.cuutro.dto.response.entities.PhanQuyenDto;
import com.backend.cuutro.entities.PhanQuyenEntity;
import org.mapstruct.Mapper;
import org.springframework.data.domain.Page;

import java.util.List;


@Mapper(componentModel = "spring")
public interface PhanQuyenMapper {

    PhanQuyenDto toDto(PhanQuyenEntity entity);

    PhanQuyenEntity toEntity(PhanQuyenDto dto);

    List<PhanQuyenDto> toDtoList(List<PhanQuyenEntity> entityList);

    List<PhanQuyenEntity> toEntityList(List<PhanQuyenDto> dtoList);

    default Page<PhanQuyenDto> toDtoPage(Page<PhanQuyenEntity> entityPage){
        if(entityPage == null){
            return Page.empty();
        }
        return entityPage.map(this::toDto);
    }
}

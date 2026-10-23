package id.my.agungdh.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;

import id.my.agungdh.domain.Diklat;
import id.my.agungdh.dto.DiklatRequest;
import id.my.agungdh.dto.DiklatResponse;

@Mapper(componentModel = MappingConstants.ComponentModel.CDI, uses = { PegawaiMapper.class, JenisPelatihanMapper.class })
public interface DiklatMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "uuid", ignore = true)
    @Mapping(target = "pegawai", ignore = true)
    @Mapping(target = "jenisPelatihan", ignore = true)
    @Mapping(target = "filename", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    Diklat toEntity(DiklatRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "uuid", ignore = true)
    @Mapping(target = "pegawai", ignore = true)
    @Mapping(target = "jenisPelatihan", ignore = true)
    @Mapping(target = "filename", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    void updateEntity(DiklatRequest request, @MappingTarget Diklat entity);

    DiklatResponse toResponse(Diklat entity);
}

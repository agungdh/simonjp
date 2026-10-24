package id.my.agungdh.simonjp.mapper;

import id.my.agungdh.simonjp.domain.Pegawai;
import id.my.agungdh.simonjp.dto.PegawaiRequest;
import id.my.agungdh.simonjp.dto.PegawaiResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface PegawaiMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "uuid", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    @Mapping(target = "bidang", ignore = true)
    Pegawai toEntity(PegawaiRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "uuid", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    @Mapping(target = "bidang", ignore = true)
    void updateEntity(PegawaiRequest request, @MappingTarget Pegawai entity);

    @Mapping(source = "bidang.uuid", target = "bidangUuid")
    PegawaiResponse toResponse(Pegawai entity);
}
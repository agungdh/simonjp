package id.my.agungdh.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;

import id.my.agungdh.domain.Pegawai;
import id.my.agungdh.dto.PegawaiRequest;
import id.my.agungdh.dto.PegawaiResponse;

@Mapper(componentModel = MappingConstants.ComponentModel.CDI, uses = { BidangMapper.class, PangkatGolonganMapper.class })
public interface PegawaiMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "uuid", ignore = true)
    @Mapping(target = "bidang", ignore = true)
    @Mapping(target = "pangkatGolongan", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    Pegawai toEntity(PegawaiRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "uuid", ignore = true)
    @Mapping(target = "bidang", ignore = true)
    @Mapping(target = "pangkatGolongan", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    void updateEntity(PegawaiRequest request, @MappingTarget Pegawai entity);

    PegawaiResponse toResponse(Pegawai entity);
}

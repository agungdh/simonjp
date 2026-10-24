package id.my.agungdh.simonjp.mapper;

import id.my.agungdh.simonjp.domain.Pegawai;
import id.my.agungdh.simonjp.dto.PegawaiRequest;
import id.my.agungdh.simonjp.dto.PegawaiResponse;
import id.my.agungdh.simonjp.dto.ProfileUpdateRequest;
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
    @Mapping(target = "pangkatGolongan", ignore = true)
    @Mapping(target = "account", ignore = true)
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
    @Mapping(target = "pangkatGolongan", ignore = true)
    @Mapping(target = "account", ignore = true)
    void updateEntity(PegawaiRequest request, @MappingTarget Pegawai entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "uuid", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    @Mapping(target = "bidang", ignore = true)
    @Mapping(target = "pangkatGolongan", ignore = true)
    @Mapping(target = "account", ignore = true)
    @Mapping(target = "status", ignore = true)
    void updateEntity(ProfileUpdateRequest request, @MappingTarget Pegawai entity);

    @Mapping(source = "bidang.uuid", target = "bidangUuid")
    @Mapping(source = "pangkatGolongan.uuid", target = "pangkatGolonganUuid")
    @Mapping(source = "account.username", target = "username")
    @Mapping(source = "account.role", target = "role")
    PegawaiResponse toResponse(Pegawai entity);
}

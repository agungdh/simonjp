package id.my.agungdh.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;

import id.my.agungdh.domain.Coaching;
import id.my.agungdh.dto.CoachingRequest;
import id.my.agungdh.dto.CoachingResponse;

@Mapper(componentModel = MappingConstants.ComponentModel.CDI, uses = { PegawaiMapper.class })
public interface CoachingMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "uuid", ignore = true)
    @Mapping(target = "pegawai", ignore = true)
    @Mapping(target = "filename", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    Coaching toEntity(CoachingRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "uuid", ignore = true)
    @Mapping(target = "pegawai", ignore = true)
    @Mapping(target = "filename", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    void updateEntity(CoachingRequest request, @MappingTarget Coaching entity);

    CoachingResponse toResponse(Coaching entity);
}

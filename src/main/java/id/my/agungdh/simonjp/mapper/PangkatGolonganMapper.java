package id.my.agungdh.simonjp.mapper;

import id.my.agungdh.simonjp.domain.PangkatGolongan;
import id.my.agungdh.simonjp.dto.PangkatGolonganRequest;
import id.my.agungdh.simonjp.dto.PangkatGolonganResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface PangkatGolonganMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "uuid", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    PangkatGolongan toEntity(PangkatGolonganRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "uuid", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    void updateEntity(PangkatGolonganRequest request, @MappingTarget PangkatGolongan entity);

    PangkatGolonganResponse toResponse(PangkatGolongan entity);
}

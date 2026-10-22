package id.my.agungdh.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import id.my.agungdh.domain.Salam;
import id.my.agungdh.dto.SalamDTO;
import id.my.agungdh.dto.SalamRequest;

/**
 * Mapper MapStruct untuk mengubah entity Lombok ke DTO record dan sebaliknya.
 *
 * <p>{@code componentModel = "cdi"} membuat implementasinya menjadi bean CDI, jadi bisa langsung
 * di-{@code @Inject} (atau lewat constructor injection) seperti service biasa.
 */
@Mapper(componentModel = "cdi")
public interface SalamMapper {

    SalamDTO toDto(Salam salam);

    List<SalamDTO> toDtoList(List<Salam> salamList);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "catatanInternal", ignore = true)
    @Mapping(target = "dibuatPada", ignore = true)
    Salam toEntity(SalamRequest request);
}

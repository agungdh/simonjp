package id.my.agungdh.simonjp.mapper;

import id.my.agungdh.simonjp.domain.UserAccount;
import id.my.agungdh.simonjp.dto.UserResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserResponse toResponse(UserAccount entity);
}

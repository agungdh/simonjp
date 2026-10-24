package id.my.agungdh.simonjp.mapper;

import id.my.agungdh.simonjp.domain.Pegawai;
import id.my.agungdh.simonjp.domain.UserAccount;
import id.my.agungdh.simonjp.dto.UserPegawaiResponse;
import id.my.agungdh.simonjp.dto.UserResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = BidangMapper.class)
public interface UserMapper {

    UserResponse toResponse(UserAccount entity);

    UserPegawaiResponse toPegawaiResponse(Pegawai entity);
}

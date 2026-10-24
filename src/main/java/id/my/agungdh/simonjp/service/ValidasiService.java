package id.my.agungdh.simonjp.service;

import id.my.agungdh.simonjp.dto.ValidasiRequest;
import id.my.agungdh.simonjp.dto.ValidasiResponse;
import org.springframework.stereotype.Service;

@Service
public class ValidasiService {

    public ValidasiResponse create(ValidasiRequest request) {
        return new ValidasiResponse(
                "Validasi berhasil",
                request.nama(),
                request.email(),
                request.umur(),
                request.harga(),
                request.stok()
        );
    }
}
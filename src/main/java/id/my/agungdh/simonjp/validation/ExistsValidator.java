package id.my.agungdh.simonjp.validation;

import id.my.agungdh.simonjp.domain.Bidang;
import id.my.agungdh.simonjp.domain.Kategori;
import id.my.agungdh.simonjp.domain.PangkatGolongan;
import id.my.agungdh.simonjp.domain.Pegawai;
import id.my.agungdh.simonjp.repository.BidangRepository;
import id.my.agungdh.simonjp.repository.KategoriRepository;
import id.my.agungdh.simonjp.repository.PangkatGolonganRepository;
import id.my.agungdh.simonjp.repository.PegawaiRepository;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class ExistsValidator implements ConstraintValidator<Exists, UUID> {

    private Class<?> entity;

    @Autowired
    private BidangRepository bidangRepository;

    @Autowired
    private KategoriRepository kategoriRepository;

    @Autowired
    private PegawaiRepository pegawaiRepository;

    @Autowired
    private PangkatGolonganRepository pangkatGolonganRepository;

    @Override
    public void initialize(Exists constraintAnnotation) {
        this.entity = constraintAnnotation.value();
    }

    @Override
    public boolean isValid(UUID value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        if (entity == Bidang.class) {
            return bidangRepository.findByUuid(value).isPresent();
        }
        if (entity == Kategori.class) {
            return kategoriRepository.findByUuid(value).isPresent();
        }
        if (entity == Pegawai.class) {
            return pegawaiRepository.findByUuid(value).isPresent();
        }
        if (entity == PangkatGolongan.class) {
            return pangkatGolonganRepository.findByUuid(value).isPresent();
        }
        return false;
    }
}

package id.my.agungdh.domain.enums;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Kategori kebutuhan jam pelatihan pegawai.")
public enum KategoriKebutuhanJamPelatihan {
    ADMIN,
    PEJABAT,
    AUDITOR
}

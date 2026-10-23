package id.my.agungdh.domain.enums;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Kategori jabatan pegawai.")
public enum KategoriJabatan {
    STRUKTURAL,
    FUNGSIONAL_AUDITOR,
    FUNGSIONAL_TERTENTU
}

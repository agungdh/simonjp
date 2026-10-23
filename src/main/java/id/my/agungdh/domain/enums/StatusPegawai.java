package id.my.agungdh.domain.enums;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Status keaktifan pegawai.")
public enum StatusPegawai {
    AKTIF,
    NON_AKTIF
}

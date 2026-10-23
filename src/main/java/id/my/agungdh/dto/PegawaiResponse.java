package id.my.agungdh.dto;

import java.util.UUID;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import id.my.agungdh.domain.enums.KategoriJabatan;
import id.my.agungdh.domain.enums.KategoriKebutuhanJamPelatihan;
import id.my.agungdh.domain.enums.StatusPegawai;
import id.my.agungdh.domain.enums.TipePegawai;

@Schema(description = "Data pegawai.")
public record PegawaiResponse(
        @Schema(description = "Identitas publik pegawai.") UUID uuid,
        @Schema(description = "NIP pegawai.", example = "198001012005011001") String nip,
        @Schema(description = "Nama pegawai.", example = "Budi Santoso") String nama,
        @Schema(description = "Tipe pegawai.", example = "PEGAWAI") TipePegawai tipe,
        @Schema(description = "Status pegawai.", example = "AKTIF") StatusPegawai status,
        @Schema(description = "Bidang tempat pegawai bertugas.") BidangResponse bidang,
        @Schema(description = "Pangkat golongan pegawai; null jika tidak ada.") PangkatGolonganResponse pangkatGolongan,
        @Schema(description = "Jabatan pegawai.", example = "Auditor") String jabatan,
        @Schema(description = "Peran pegawai.") String peran,
        @Schema(description = "Kategori jabatan.") KategoriJabatan kategoriJabatan,
        @Schema(description = "Kategori kebutuhan jam pelatihan.") KategoriKebutuhanJamPelatihan kategoriKebutuhanJamPelatihan) {
}

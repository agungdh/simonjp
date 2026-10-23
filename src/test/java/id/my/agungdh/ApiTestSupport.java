package id.my.agungdh;

import static io.restassured.RestAssured.given;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import io.restassured.http.ContentType;

abstract class ApiTestSupport {

    static String unique(String prefix) {
        return prefix + "-" + UUID.randomUUID();
    }

    static String shortToken(int length) {
        return UUID.randomUUID().toString().replace("-", "").substring(0, length);
    }

    static String createBidang(String nama) {
        return given()
                .contentType(ContentType.JSON)
                .body(Map.of("bidang", nama))
                .when().post("/bidang")
                .then().statusCode(201)
                .extract().path("uuid");
    }

    static String createPangkatGolongan(String golongan, String ruang) {
        return given()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "jenjang", "Jenjang " + golongan,
                        "pangkat", "Pangkat " + golongan + "/" + ruang,
                        "golongan", golongan,
                        "ruang", ruang))
                .when().post("/pangkat-golongan")
                .then().statusCode(201)
                .extract().path("uuid");
    }

    static Map<String, Object> pegawaiPayload(String nip, String nama, String bidangUuid, String pangkatGolonganUuid) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("nip", nip);
        payload.put("nama", nama);
        payload.put("tipe", "PEGAWAI");
        payload.put("status", "AKTIF");
        payload.put("bidangUuid", bidangUuid);
        payload.put("pangkatGolonganUuid", pangkatGolonganUuid);
        payload.put("jabatan", "Auditor");
        payload.put("peran", "Anggota");
        payload.put("kategoriJabatan", "FUNGSIONAL_AUDITOR");
        payload.put("kategoriKebutuhanJamPelatihan", "AUDITOR");
        return payload;
    }

    static String createPegawai(String bidangUuid, String pangkatGolonganUuid, String nama) {
        return given()
                .contentType(ContentType.JSON)
                .body(pegawaiPayload(nip(), nama, bidangUuid, pangkatGolonganUuid))
                .when().post("/pegawai")
                .then().statusCode(201)
                .extract().path("uuid");
    }

    static String nip() {
        return String.valueOf(System.nanoTime()) + (int) (Math.random() * 10);
    }
}

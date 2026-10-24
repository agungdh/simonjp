package id.my.agungdh.simonjp.domain;

/**
 * Role sederhana yang disimpan sebagai kolom {@code user_account.role}.
 * Nilainya harus konsisten dengan constraint {@code ck_user_account_role}.
 */
public enum Role {
    ADMIN,
    PEGAWAI
}

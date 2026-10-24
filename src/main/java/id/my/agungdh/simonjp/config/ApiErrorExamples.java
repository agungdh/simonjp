package id.my.agungdh.simonjp.config;

/**
 * Contoh body error untuk dokumentasi OpenAPI.
 * Nilainya wajib berupa konstanta compile-time agar bisa dipakai di {@code @ExampleObject}.
 */
public final class ApiErrorExamples {

    public static final String VALIDATION_ERROR = """
            {
              "message": "Validasi gagal",
              "fields": {
                "nama": ["must not be blank"],
                "nip": ["must not be blank", "size must be between 0 and 50"],
                "email": ["must be a well-formed email address"],
                "password": ["size must be between 8 and 72"],
                "status": ["must not be null"]
              }
            }""";

    public static final String DUPLICATE_NAME = """
            {"message": "Nama <resource> sudah digunakan"}""";

    public static final String INVALID_BODY = """
            {"message": "Body request tidak valid"}""";

    public static final String INVALID_PARAMETER = """
            {"message": "Parameter tidak valid"}""";

    public static final String INVALID_CURSOR = """
            {"message": "Cursor tidak valid"}""";

    public static final String NOT_FOUND = """
            {"message": "<resource> tidak ditemukan"}""";

    public static final String UNAUTHORIZED = """
            {"message": "Autentikasi diperlukan"}""";

    public static final String BAD_CREDENTIALS = """
            {"message": "Username atau password salah"}""";

    public static final String FORBIDDEN = """
            {"message": "Akses ditolak"}""";

    public static final String INVALID_CSRF = """
            {"message": "CSRF token tidak valid"}""";

    public static final String INTERNAL_ERROR = """
            {"message": "Terjadi kesalahan internal"}""";

    private ApiErrorExamples() {
    }
}

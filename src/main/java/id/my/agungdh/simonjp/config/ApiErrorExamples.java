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
                "nama": ["must not be blank", "size must be between 2 and 50"],
                "email": ["must be a well-formed email address"],
                "umur": ["must be greater than or equal to 17"],
                "harga": ["must be greater than 0"],
                "stok": ["must be greater than or equal to 0"]
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

    public static final String INTERNAL_ERROR = """
            {"message": "Terjadi kesalahan internal"}""";

    private ApiErrorExamples() {
    }
}

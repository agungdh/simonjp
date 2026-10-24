package id.my.agungdh.simonjp.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Ukuran halaman untuk semua endpoint list (cursor pagination).
 *
 * @param pageSize jumlah baris per halaman; client tidak bisa mengubahnya
 */
@ConfigurationProperties(prefix = "app.pagination")
public record PageProperties(int pageSize) {

    public PageProperties {
        if (pageSize < 1) {
            throw new IllegalStateException("app.pagination.page-size minimal 1");
        }
    }
}

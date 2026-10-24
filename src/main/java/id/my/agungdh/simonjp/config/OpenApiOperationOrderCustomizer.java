package id.my.agungdh.simonjp.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Menjaga urutan endpoint di Swagger UI: list, get one, create, update, delete, lalu fitur
 * tambahan di bawah delete.
 *
 * <p>springdoc menyusun {@code paths} berdasarkan urutan {@code RequestMappingInfo} (terbalik
 * secara leksikografis), bukan urutan method di controller, sedangkan Swagger UI menampilkan
 * operasi mengikuti urutan {@code paths} apa adanya (operationsSorter tidak di-set). Karena itu
 * urutan di atas perlu ditegakkan ulang di sini.
 */
@Component
public class OpenApiOperationOrderCustomizer implements OpenApiCustomizer {

    @Override
    public void customise(OpenAPI openApi) {
        Paths paths = openApi.getPaths();
        if (paths == null || paths.isEmpty()) {
            return;
        }

        List<Map.Entry<String, PathItem>> ordered = new ArrayList<>(paths.entrySet());
        // List.sort stabil: path dengan peringkat sama tetap mempertahankan urutan aslinya.
        ordered.sort(Comparator.comparingInt(entry -> rank(entry.getKey(), entry.getValue())));

        Paths sorted = new Paths();
        ordered.forEach(entry -> sorted.addPathItem(entry.getKey(), entry.getValue()));
        openApi.setPaths(sorted);
    }

    private static int rank(String path, PathItem pathItem) {
        boolean itemPath = path.contains("{");

        if (pathItem.getGet() != null) {
            return itemPath ? 1 : 0;      // get one, list
        }
        if (!itemPath && pathItem.getPost() != null) {
            return 2;                     // create
        }
        if (itemPath && pathItem.getPut() != null) {
            return 3;                     // update
        }
        if (itemPath && pathItem.getDelete() != null) {
            return 4;                     // delete
        }
        return 5;                         // fitur tambahan di bawah delete
    }
}

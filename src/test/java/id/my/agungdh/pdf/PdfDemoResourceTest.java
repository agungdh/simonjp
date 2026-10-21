package id.my.agungdh.pdf;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;

@QuarkusTest
class PdfDemoResourceTest {

    @Test
    void generatesPdfWithImagesChartsAndStyles() throws Exception {
        byte[] pdf = RestAssured.given()
                .when().get("/pdf/contoh")
                .then()
                .statusCode(200)
                .contentType("application/pdf")
                .extract().asByteArray();

        assertEquals("%PDF-", new String(pdf, 0, 5));

        try (PDDocument document = Loader.loadPDF(pdf)) {
            assertTrue(document.getNumberOfPages() >= 3);

            String text = new PDFTextStripper().getText(document);
            assertTrue(text.contains("Contoh Generate PDF"));
            assertTrue(text.contains("Helvetica Bold"));
            assertTrue(text.contains("Times New Roman"));
            assertTrue(text.contains("Ringkasan Fitur"));
            assertTrue(text.contains("Chart Batang"));
        }
    }
}

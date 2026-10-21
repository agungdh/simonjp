package id.my.agungdh.office;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;

import org.apache.poi.xwpf.usermodel.UnderlinePatterns;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;

@QuarkusTest
class WordDemoResourceTest {

    @Test
    void generatesDocxWithImagesChartsAndStyles() throws Exception {
        byte[] docx = RestAssured.given()
                .when().get("/word/contoh")
                .then()
                .statusCode(200)
                .contentType(
                        "application/vnd.openxmlformats-officedocument.wordprocessingml.document")
                .extract().asByteArray();

        try (XWPFDocument document = new XWPFDocument(new ByteArrayInputStream(docx))) {
            StringBuilder text = new StringBuilder();
            for (XWPFParagraph paragraph : document.getParagraphs()) {
                text.append(paragraph.getText()).append('\n');
            }

            assertTrue(text.toString().contains("Contoh Generate Word"));
            assertTrue(text.toString().contains("Times New Roman"));
            assertTrue(text.toString().contains("Ringkasan Fitur"));
            assertEquals(1, document.getTables().size());
            assertTrue(document.getAllPictures().size() >= 3);

            boolean boldItalicUnderline = document.getParagraphs().stream()
                    .flatMap(paragraph -> paragraph.getRuns().stream())
                    .anyMatch(run -> run.isBold() && run.isItalic()
                            && run.getUnderline() == UnderlinePatterns.SINGLE);
            assertTrue(boldItalicUnderline);
        }
    }
}

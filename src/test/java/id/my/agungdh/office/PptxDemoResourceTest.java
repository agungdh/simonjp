package id.my.agungdh.office;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;

import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.apache.poi.xslf.usermodel.XSLFTable;
import org.apache.poi.xslf.usermodel.XSLFTextParagraph;
import org.apache.poi.xslf.usermodel.XSLFTextRun;
import org.apache.poi.xslf.usermodel.XSLFTextShape;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;

@QuarkusTest
class PptxDemoResourceTest {

    @Test
    void generatesPptxWithImagesChartsAndTable() throws Exception {
        byte[] pptx = RestAssured.given()
                .when().get("/ppt/contoh")
                .then()
                .statusCode(200)
                .contentType(
                        "application/vnd.openxmlformats-officedocument.presentationml.presentation")
                .extract().asByteArray();

        try (XMLSlideShow show = new XMLSlideShow(new ByteArrayInputStream(pptx))) {
            assertEquals(8, show.getSlides().size());
            assertTrue(show.getPictureData().size() >= 3);

            StringBuilder text = new StringBuilder();
            for (XSLFSlide slide : show.getSlides()) {
                for (XSLFTextShape shape : slide.getShapes().stream()
                        .filter(XSLFTextShape.class::isInstance)
                        .map(XSLFTextShape.class::cast)
                        .toList()) {
                    text.append(shape.getText()).append('\n');
                }
            }

            assertTrue(text.toString().contains("Contoh"));
            assertTrue(text.toString().contains("Times New Roman"));
            assertTrue(text.toString().contains("Ringkasan Fitur"));
            assertTrue(text.toString().contains("Terima kasih"));

            boolean boldItalicUnderline = show.getSlides().stream()
                    .flatMap(slide -> slide.getShapes().stream())
                    .filter(XSLFTextShape.class::isInstance)
                    .map(XSLFTextShape.class::cast)
                    .flatMap(shape -> shape.getTextParagraphs().stream())
                    .map(XSLFTextParagraph::getTextRuns)
                    .flatMap(java.util.List::stream)
                    .anyMatch(run -> run.isBold() && run.isItalic() && run.isUnderlined());
            assertTrue(boldItalicUnderline);

            assertEquals(1, show.getSlides().stream()
                    .flatMap(slide -> slide.getShapes().stream())
                    .filter(XSLFTable.class::isInstance)
                    .count());

            XSLFTable table = show.getSlides().stream()
                    .flatMap(slide -> slide.getShapes().stream())
                    .filter(XSLFTable.class::isInstance)
                    .map(XSLFTable.class::cast)
                    .findFirst()
                    .orElseThrow();
            assertEquals(7, table.getNumberOfRows());
            assertEquals(3, table.getNumberOfColumns());

            XSLFTextRun header = table.getCell(0, 0).getTextParagraphs().get(0)
                    .getTextRuns().get(0);
            assertTrue(header.isBold());
        }
    }
}

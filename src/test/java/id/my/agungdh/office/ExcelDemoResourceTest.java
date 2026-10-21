package id.my.agungdh.office;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;

import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;

@QuarkusTest
class ExcelDemoResourceTest {

    @Test
    void generatesXlsxWithImagesChartsAndStyles() throws Exception {
        byte[] xlsx = RestAssured.given()
                .when().get("/excel/contoh")
                .then()
                .statusCode(200)
                .contentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                .extract().asByteArray();

        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(xlsx))) {
            assertEquals(3, workbook.getNumberOfSheets());

            XSSFSheet styles = workbook.getSheet("Gaya Teks");
            assertEquals("Contoh Variasi Font dan Gaya Teks",
                    styles.getRow(0).getCell(0).getStringCellValue());

            boolean boldItalicUnderline = false;
            boolean size36 = false;
            for (int rowIndex = 0; rowIndex <= styles.getLastRowNum(); rowIndex++) {
                XSSFRow row = styles.getRow(rowIndex);
                if (row == null) {
                    continue;
                }
                for (int column = 0; column < row.getLastCellNum(); column++) {
                    XSSFCell cell = row.getCell(column);
                    if (cell == null || cell.getCellType() != CellType.STRING) {
                        continue;
                    }
                    XSSFFont font = cell.getCellStyle().getFont();
                    size36 |= font.getFontHeightInPoints() == 36;
                    boldItalicUnderline |= font.getBold() && font.getItalic()
                            && font.getUnderline() == Font.U_SINGLE;
                }
            }
            assertTrue(boldItalicUnderline);
            assertTrue(size36);

            XSSFSheet data = workbook.getSheet("Data");
            assertEquals("Jan", data.getRow(ExcelDemoGenerator.DATA_FIRST_ROW)
                    .getCell(0).getStringCellValue());
            assertEquals("Des", data.getRow(ExcelDemoGenerator.DATA_LAST_ROW)
                    .getCell(0).getStringCellValue());
            assertEquals(1, data.getDrawingPatriarch().getShapes().size());

            XSSFSheet chart = workbook.getSheet("Chart");
            assertEquals(2, chart.getDrawingPatriarch().getCharts().size());
        }
    }
}

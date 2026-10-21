package id.my.agungdh.office;

import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.List;

import org.apache.poi.common.usermodel.PictureType;
import org.apache.poi.util.Units;
import org.apache.poi.xwpf.model.XWPFHeaderFooterPolicy;
import org.apache.poi.xwpf.usermodel.BreakType;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.UnderlinePatterns;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFFooter;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTPBdr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTPPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STBorder;

public final class WordDemoGenerator {

    private static final String BRAND = "2563EB";
    private static final String DARK = "1C212E";
    private static final String MUTED = "6B7384";
    private static final String RULE = "DEE3EC";
    private static final String PANEL = "F7F9FC";

    private static final String[] MONTHS = {"Jan", "Feb", "Mar", "Apr", "Mei", "Jun",
            "Jul", "Agu", "Sep", "Okt", "Nov", "Des"};
    private static final double[] REVENUE = {42, 58, 51, 73, 66, 88, 79, 92, 84, 97, 90, 105};

    private WordDemoGenerator() {
    }

    public static byte[] build() throws IOException {
        try (XWPFDocument document = new XWPFDocument()) {
            document.getProperties().getCoreProperties().setTitle("Contoh Generate Word");
            document.getProperties().getCoreProperties().setCreator("simonjp");
            document.getProperties().getCoreProperties()
                    .setDescription("Demo Apache POI: gambar, chart, variasi font dan gaya teks");

            drawCover(document);
            drawFontSection(document);
            drawStyleSection(document);
            drawSizeSection(document);
            drawImageSection(document);
            drawChartSection(document);
            drawTableSection(document);
            drawFooter(document);

            ByteArrayOutputStream output = new ByteArrayOutputStream();
            document.write(output);
            return output.toByteArray();
        }
    }

    private static void drawCover(XWPFDocument document) {
        XWPFParagraph title = document.createParagraph();
        title.setSpacingAfter(40);
        run(title, "Contoh Generate Word", "Calibri", 30, true, false, false, DARK);

        XWPFParagraph subtitle = document.createParagraph();
        subtitle.setSpacingAfter(80);
        run(subtitle, "Gambar, chart, variasi font, bold/italic/underline, dan ukuran berbeda",
                "Georgia", 12, false, true, false, MUTED);

        XWPFParagraph meta = document.createParagraph();
        meta.setSpacingAfter(160);
        run(meta, "Quarkus REST  \u2022  Apache POI 5.5.1  \u2022  A4",
                "Consolas", 9, false, false, false, BRAND);
        bottomBorder(meta, RULE, 6);
    }

    private static void drawFontSection(XWPFDocument document) {
        heading(document, "1", "Berbagai Jenis Font");

        fontLine(document, "Calibri Regular - sans-serif, 12 pt", "Calibri", 12);
        fontLine(document, "Arial Bold - sans-serif tebal, 12 pt", "Arial", 12);
        fontLine(document, "Times New Roman Italic - serif miring, 13 pt", "Times New Roman",
                13);
        fontLine(document, "Georgia - serif, 12 pt", "Georgia", 12);
        fontLine(document, "Courier New - monospace, 11 pt", "Courier New", 11);
        fontLine(document, "Consolas - monospace, 11 pt", "Consolas", 11);
        fontLine(document, "Verdana - sans-serif lebar, 11 pt", "Verdana", 11);
    }

    private static void fontLine(XWPFDocument document, String sample, String font, int size) {
        XWPFParagraph paragraph = document.createParagraph();
        paragraph.setSpacingAfter(70);
        run(paragraph, String.format("%-16s", font.substring(0, Math.min(font.length(), 16))),
                "Consolas", 8, false, false, false, MUTED);
        run(paragraph, sample, font, size, false, false, false, DARK);
    }

    private static void drawStyleSection(XWPFDocument document) {
        heading(document, "2", "Gaya Teks: Bold, Italic, dan Underline");

        XWPFParagraph first = document.createParagraph();
        first.setSpacingAfter(80);
        run(first, "Teks ", "Calibri", 11, false, false, false, DARK);
        run(first, "normal", "Calibri", 11, false, false, false, DARK);
        run(first, ", ", "Calibri", 11, false, false, false, DARK);
        run(first, "tebal", "Calibri", 11, true, false, false, DARK);
        run(first, ", ", "Calibri", 11, false, false, false, DARK);
        run(first, "miring", "Calibri", 11, false, true, false, DARK);
        run(first, ", ", "Calibri", 11, false, false, false, DARK);
        run(first, "tebal miring", "Calibri", 11, true, true, false, DARK);
        run(first, ", dan ", "Calibri", 11, false, false, false, DARK);
        run(first, "bergaris bawah", "Calibri", 11, false, false, true, BRAND);

        XWPFParagraph second = document.createParagraph();
        second.setSpacingAfter(80);
        run(second, "Times New Roman: ", "Times New Roman", 12, false, false, false, DARK);
        run(second, "bold", "Times New Roman", 12, true, false, false, DARK);
        run(second, ", ", "Times New Roman", 12, false, false, false, DARK);
        run(second, "italic", "Times New Roman", 12, false, true, false, DARK);
        run(second, ", ", "Times New Roman", 12, false, false, false, DARK);
        run(second, "bold italic", "Times New Roman", 12, true, true, false, DARK);
        run(second, ", ", "Times New Roman", 12, false, false, false, DARK);
        run(second, "underline", "Times New Roman", 12, false, false, true, DARK);

        XWPFParagraph third = document.createParagraph();
        third.setSpacingAfter(80);
        run(third, "Teks berwarna: ", "Calibri", 10, false, false, false, DARK);
        run(third, "merah", "Calibri", 10, true, false, false, "DC2626");
        run(third, ", ", "Calibri", 10, false, false, false, DARK);
        run(third, "hijau", "Calibri", 10, true, false, false, "059669");
        run(third, ", ", "Calibri", 10, false, false, false, DARK);
        run(third, "biru", "Calibri", 10, true, false, false, "2563EB");
        run(third, ", dan ", "Calibri", 10, false, false, false, DARK);
        run(third, "ungu bergaris bawah", "Consolas", 10, true, false, true, "7C3AED");

        XWPFParagraph fourth = document.createParagraph();
        fourth.setSpacingAfter(80);
        run(fourth, "Kombinasi lengkap: ", "Calibri", 10, false, false, false, DARK);
        run(fourth, "bold italic underline", "Calibri", 10, true, true, true, "7C3AED");
    }

    private static void drawSizeSection(XWPFDocument document) {
        heading(document, "3", "Ukuran Font Berbeda");

        int[] sizes = {9, 11, 14, 18, 24, 30, 36};
        for (int size : sizes) {
            XWPFParagraph paragraph = document.createParagraph();
            paragraph.setSpacingAfter(50);
            run(paragraph, size + " pt", "Consolas", 8, false, false, false, MUTED);
            run(paragraph, "  Contoh ukuran " + size + " pt", "Calibri", size,
                    false, false, false, DARK);
        }
    }

    private static void drawImageSection(XWPFDocument document) throws IOException {
        byte[] jpeg = ImageAssets.read("/images/contoh.jpg");
        int[] dimensions = ImageAssets.jpegSize(jpeg);

        heading(document, "4", "Gambar (JPEG di-embed ke dokumen)");
        insertPicture(document, jpeg, PictureType.JPEG, "contoh.jpg",
                dimensions[0], dimensions[1], 460, 230);

        XWPFParagraph caption = document.createParagraph();
        caption.setAlignment(ParagraphAlignment.CENTER);
        caption.setSpacingAfter(160);
        run(caption, "Gambar 1. Banner contoh, di-embed langsung sebagai JPEG.",
                "Georgia", 9, false, true, false, MUTED);
    }

    private static void drawChartSection(XWPFDocument document) throws IOException {
        heading(document, "5", "Chart (dirender menjadi gambar PNG)");

        byte[] bar = ChartImageRenderer.barChart("Pendapatan Bulanan 2025",
                "dalam juta rupiah", MONTHS, REVENUE, new Color(0x25, 0x63, 0xEB));
        insertPicture(document, bar, PictureType.PNG, "chart-batang.png",
                ChartImageRenderer.WIDTH, ChartImageRenderer.HEIGHT, 470, 230);

        byte[] line = ChartImageRenderer.lineChart("Perbandingan Pendapatan",
                "dalam juta rupiah", new String[]{"Jan", "Feb", "Mar", "Apr", "Mei", "Jun"},
                List.of(
                        new ChartImageRenderer.Series("2024", new Color(0xF5, 0x9E, 0x0B),
                                new double[]{42, 58, 51, 73, 66, 88}),
                        new ChartImageRenderer.Series("2025", new Color(0x25, 0x63, 0xEB),
                                new double[]{55, 71, 68, 91, 84, 105})));
        insertPicture(document, line, PictureType.PNG, "chart-garis.png",
                ChartImageRenderer.WIDTH, ChartImageRenderer.HEIGHT, 470, 230);
    }

    private static void drawTableSection(XWPFDocument document) {
        pageBreak(document);
        heading(document, "6", "Ringkasan Fitur");

        String[][] rows = {
                {"Fitur", "Implementasi", "Status"},
                {"Font family", "Calibri, Arial, Times New Roman, Georgia", "OK"},
                {"Font monospace", "Courier New, Consolas", "OK"},
                {"Gaya teks", "Bold, italic, bold-italic, underline", "OK"},
                {"Ukuran font", "9 pt sampai 36 pt", "OK"},
                {"Gambar", "JPEG di-embed ke dokumen", "OK"},
                {"Chart", "Bar chart dan line chart (PNG)", "OK"}
        };

        XWPFTable table = document.createTable(rows.length, 3);
        table.setWidth("100%");
        table.setCellMargins(80, 120, 80, 120);

        for (int rowIndex = 0; rowIndex < rows.length; rowIndex++) {
            XWPFTableRow row = table.getRow(rowIndex);
            for (int columnIndex = 0; columnIndex < rows[rowIndex].length; columnIndex++) {
                XWPFTableCell cell = row.getCell(columnIndex);
                cell.setVerticalAlignment(XWPFTableCell.XWPFVertAlign.CENTER);
                cell.setColor(rowIndex == 0 ? BRAND
                        : rowIndex % 2 == 0 ? PANEL : "FFFFFF");

                XWPFParagraph paragraph = cell.getParagraphs().get(0);
                paragraph.setSpacingAfter(0);
                run(paragraph, rows[rowIndex][columnIndex], "Calibri", 9,
                        rowIndex == 0, false, false, rowIndex == 0 ? "FFFFFF" : DARK);
            }
        }
    }

    private static void drawFooter(XWPFDocument document) {
        XWPFHeaderFooterPolicy policy = document.createHeaderFooterPolicy();
        XWPFFooter footer = policy.createFooter(XWPFHeaderFooterPolicy.DEFAULT);

        XWPFParagraph paragraph = footer.createParagraph();
        paragraph.setAlignment(ParagraphAlignment.CENTER);
        run(paragraph, "Quarkus + Apache POI 5.5.1  |  dibuat otomatis",
                "Consolas", 8, false, false, false, MUTED);
    }

    private static void pageBreak(XWPFDocument document) {
        XWPFParagraph paragraph = document.createParagraph();
        paragraph.setSpacingAfter(0);
        paragraph.createRun().addBreak(BreakType.PAGE);
    }

    private static void heading(XWPFDocument document, String number, String title) {
        XWPFParagraph paragraph = document.createParagraph();
        paragraph.setSpacingBefore(240);
        paragraph.setSpacingAfter(120);
        run(paragraph, number + ". " + title, "Calibri", 15, true, false, false, BRAND);
        bottomBorder(paragraph, RULE, 6);
    }

    private static XWPFRun run(XWPFParagraph paragraph, String text, String font, int size,
                               boolean bold, boolean italic, boolean underline, String color) {
        XWPFRun run = paragraph.createRun();
        run.setText(text);
        run.setFontFamily(font);
        run.setFontSize(size);
        run.setBold(bold);
        run.setItalic(italic);
        if (underline) {
            run.setUnderline(UnderlinePatterns.SINGLE);
        }
        run.setColor(color);
        return run;
    }

    private static void insertPicture(XWPFDocument document, byte[] data, PictureType pictureType,
                                      String name, int pixelWidth, int pixelHeight,
                                      float maxWidth, float maxHeight) throws IOException {
        float scale = Math.min(1f, Math.min(maxWidth / pixelWidth, maxHeight / pixelHeight));
        int width = Math.round(pixelWidth * scale);
        int height = Math.round(pixelHeight * scale);

        XWPFParagraph paragraph = document.createParagraph();
        paragraph.setAlignment(ParagraphAlignment.CENTER);
        paragraph.setSpacingBefore(120);

        XWPFRun run = paragraph.createRun();
        try {
            run.addPicture(new ByteArrayInputStream(data), pictureType, name,
                    Units.pixelToEMU(width), Units.pixelToEMU(height));
        } catch (org.apache.poi.openxml4j.exceptions.InvalidFormatException e) {
            throw new IOException("Gagal menambahkan gambar " + name, e);
        }
    }

    private static void bottomBorder(XWPFParagraph paragraph, String color, int size) {
        CTPPr properties = paragraph.getCTP().isSetPPr()
                ? paragraph.getCTP().getPPr()
                : paragraph.getCTP().addNewPPr();
        CTPBdr borders = properties.isSetPBdr()
                ? properties.getPBdr()
                : properties.addNewPBdr();
        var bottom = borders.isSetBottom() ? borders.getBottom() : borders.addNewBottom();
        bottom.setVal(STBorder.SINGLE);
        bottom.setSz(BigInteger.valueOf(size));
        bottom.setSpace(BigInteger.valueOf(4));
        bottom.setColor(color);
    }
}

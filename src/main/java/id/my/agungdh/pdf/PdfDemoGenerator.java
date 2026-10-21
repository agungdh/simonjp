package id.my.agungdh.pdf;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDPageContentStream.AppendMode;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceRGB;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

public final class PdfDemoGenerator {

    private static final float PAGE_WIDTH = PDRectangle.A4.getWidth();
    private static final float PAGE_HEIGHT = PDRectangle.A4.getHeight();
    private static final float MARGIN = 50f;
    private static final float CONTENT_WIDTH = PAGE_WIDTH - 2 * MARGIN;

    private static final float BRAND_R = 0.145f;
    private static final float BRAND_G = 0.388f;
    private static final float BRAND_B = 0.922f;
    private static final float DARK_R = 0.11f;
    private static final float DARK_G = 0.13f;
    private static final float DARK_B = 0.18f;
    private static final float MUTED = 0.42f;
    private static final float RULE = 0.87f;

    private final PDFont helvetica = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
    private final PDFont helveticaBold = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
    private final PDFont helveticaOblique =
            new PDType1Font(Standard14Fonts.FontName.HELVETICA_OBLIQUE);
    private final PDFont helveticaBoldOblique =
            new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD_OBLIQUE);
    private final PDFont times = new PDType1Font(Standard14Fonts.FontName.TIMES_ROMAN);
    private final PDFont timesBold = new PDType1Font(Standard14Fonts.FontName.TIMES_BOLD);
    private final PDFont timesItalic = new PDType1Font(Standard14Fonts.FontName.TIMES_ITALIC);
    private final PDFont timesBoldItalic =
            new PDType1Font(Standard14Fonts.FontName.TIMES_BOLD_ITALIC);
    private final PDFont courier = new PDType1Font(Standard14Fonts.FontName.COURIER);
    private final PDFont courierBold = new PDType1Font(Standard14Fonts.FontName.COURIER_BOLD);

    private record Run(String text, PDFont font, float size, float red, float green, float blue,
                       boolean underline) {
    }

    private final PDDocument document = new PDDocument();
    private final List<PDPage> pages = new ArrayList<>();
    private final ChartPainter charts = new ChartPainter(helvetica, helveticaBold, courier);
    private PDPageContentStream content;
    private float cursorY;
    private int rowIndex;

    private PdfDemoGenerator() {
    }

    public static byte[] build() throws IOException {
        return new PdfDemoGenerator().render();
    }

    private byte[] render() throws IOException {
        PDDocumentInformation info = document.getDocumentInformation();
        info.setTitle("Contoh Generate PDF");
        info.setSubject("Demo Apache PDFBox: gambar, chart, variasi font dan gaya teks");
        info.setAuthor("simonjp");
        info.setCreator("Quarkus + Quarkus PDFBox 1.2.3");
        info.setProducer("Apache PDFBox");

        newPage();
        drawCover();
        drawFontSection();
        drawStyleSection();
        drawSizeSection();
        drawImageSection();
        drawBarChartSection();
        drawLineChartSection();
        drawSummarySection();
        closeContent();
        drawFooters();

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        document.save(output);
        document.close();
        return output.toByteArray();
    }

    private void newPage() throws IOException {
        closeContent();
        PDPage page = new PDPage(PDRectangle.A4);
        pages.add(page);
        document.addPage(page);
        content = new PDPageContentStream(document, page);
        cursorY = PAGE_HEIGHT - MARGIN;
    }

    private void closeContent() throws IOException {
        if (content != null) {
            content.close();
            content = null;
        }
    }

    private void ensureSpace(float needed) throws IOException {
        if (cursorY - needed < MARGIN + 34) {
            newPage();
        }
    }

    private void drawCover() throws IOException {
        float top = cursorY;
        float panelHeight = 96;

        content.setNonStrokingColor(0.945f, 0.968f, 1f);
        content.addRect(MARGIN, top - panelHeight, CONTENT_WIDTH, panelHeight);
        content.fill();

        content.setNonStrokingColor(BRAND_R, BRAND_G, BRAND_B);
        content.addRect(MARGIN, top - panelHeight, 5, panelHeight);
        content.fill();

        text("Contoh Generate PDF", helveticaBold, 25, DARK_R, DARK_G, DARK_B,
                MARGIN + 20, top - 38);
        text("Gambar, chart vektor, variasi font, bold/italic/underline, dan ukuran berbeda",
                timesItalic, 11.5f, MUTED, MUTED, MUTED, MARGIN + 20, top - 61);
        text("Quarkus REST  \u2022  Quarkus PDFBox 1.2.3  \u2022  A4",
                courier, 8.5f, BRAND_R, BRAND_G, BRAND_B, MARGIN + 20, top - 81);

        cursorY = top - panelHeight - 26;
    }

    private void section(String number, String title) throws IOException {
        section(number, title, 0);
    }

    private void section(String number, String title, float minFollowingHeight) throws IOException {
        ensureSpace(52 + minFollowingHeight);
        cursorY -= 4;

        content.setNonStrokingColor(BRAND_R, BRAND_G, BRAND_B);
        content.addRect(MARGIN, cursorY - 3, 4, 15);
        content.fill();
        text(number + ". " + title, helveticaBold, 14, DARK_R, DARK_G, DARK_B,
                MARGIN + 12, cursorY);

        cursorY -= 10;
        line(MARGIN, cursorY, MARGIN + CONTENT_WIDTH, cursorY, RULE, RULE, RULE, 0.7f);
        cursorY -= 18;
    }

    private void drawFontSection() throws IOException {
        section("1", "Berbagai Jenis Font");

        fontRow("helvetica", "Helvetica Regular - sans-serif, 12 pt", helvetica, 12);
        fontRow("helvetica BOLD", "Helvetica Bold - sans-serif tebal, 12 pt", helveticaBold, 12);
        fontRow("HELV. OBLIQUE", "Helvetica Italic - sans-serif miring, 12 pt",
                helveticaOblique, 12);
        fontRow("times", "Times New Roman Regular - serif, 13 pt", times, 13);
        fontRow("times BOLD", "Times New Roman Bold - serif tebal, 13 pt", timesBold, 13);
        fontRow("times ITALIC", "Times New Roman Italic - serif miring, 13 pt", timesItalic, 13);
        fontRow("courier", "Courier - monospace, 11 pt", courier, 11);
    }

    private void fontRow(String label, String sample, PDFont font, float size) throws IOException {
        ensureSpace(30);
        if (rowIndex % 2 == 0) {
            content.setNonStrokingColor(0.965f, 0.973f, 0.985f);
            content.addRect(MARGIN, cursorY - 8, CONTENT_WIDTH, 28);
            content.fill();
        }
        text(label, courier, 7.5f, MUTED, MUTED, MUTED, MARGIN + 8, cursorY);
        text(sample, font, size, DARK_R, DARK_G, DARK_B, MARGIN + 118, cursorY);
        cursorY -= 30;
        rowIndex++;
    }

    private void drawStyleSection() throws IOException {
        section("2", "Gaya Teks: Bold, Italic, dan Underline");
        cursorY -= 4;

        drawRuns(List.of(
                new Run("Teks ", helvetica, 11, DARK_R, DARK_G, DARK_B, false),
                new Run("normal", helvetica, 11, DARK_R, DARK_G, DARK_B, false),
                new Run(", ", helvetica, 11, DARK_R, DARK_G, DARK_B, false),
                new Run("tebal", helveticaBold, 11, DARK_R, DARK_G, DARK_B, false),
                new Run(", ", helvetica, 11, DARK_R, DARK_G, DARK_B, false),
                new Run("miring", helveticaOblique, 11, DARK_R, DARK_G, DARK_B, false),
                new Run(", ", helvetica, 11, DARK_R, DARK_G, DARK_B, false),
                new Run("tebal miring", helveticaBoldOblique, 11, DARK_R, DARK_G, DARK_B, false),
                new Run(", dan ", helvetica, 11, DARK_R, DARK_G, DARK_B, false),
                new Run("bergaris bawah", helvetica, 11, BRAND_R, BRAND_G, BRAND_B, true),
                new Run(".", helvetica, 11, DARK_R, DARK_G, DARK_B, false)
        ), 19);

        drawRuns(List.of(
                new Run("Times New Roman: ", times, 12, DARK_R, DARK_G, DARK_B, false),
                new Run("bold", timesBold, 12, DARK_R, DARK_G, DARK_B, false),
                new Run(", ", times, 12, DARK_R, DARK_G, DARK_B, false),
                new Run("italic", timesItalic, 12, DARK_R, DARK_G, DARK_B, false),
                new Run(", ", times, 12, DARK_R, DARK_G, DARK_B, false),
                new Run("bold italic", timesBoldItalic, 12, DARK_R, DARK_G, DARK_B, false),
                new Run(", ", times, 12, DARK_R, DARK_G, DARK_B, false),
                new Run("underline", times, 12, DARK_R, DARK_G, DARK_B, true),
                new Run(".", times, 12, DARK_R, DARK_G, DARK_B, false)
        ), 19);

        drawRuns(List.of(
                new Run("Teks berwarna: ", helvetica, 10.5f, DARK_R, DARK_G, DARK_B, false),
                new Run("merah", helveticaBold, 10.5f, 0.85f, 0.2f, 0.24f, false),
                new Run(", ", helvetica, 10.5f, DARK_R, DARK_G, DARK_B, false),
                new Run("hijau", helveticaBold, 10.5f, 0.06f, 0.55f, 0.33f, false),
                new Run(", ", helvetica, 10.5f, DARK_R, DARK_G, DARK_B, false),
                new Run("biru", helveticaBold, 10.5f, 0.12f, 0.35f, 0.85f, false),
                new Run(", dan ", helvetica, 10.5f, DARK_R, DARK_G, DARK_B, false),
                new Run("ungu bergaris bawah", courierBold, 10, 0.48f, 0.25f, 0.75f, true),
                new Run(".", helvetica, 10.5f, DARK_R, DARK_G, DARK_B, false)
        ), 19);

        cursorY -= 4;
    }

    private void drawRuns(List<Run> runs, float lineHeight) throws IOException {
        ensureSpace(lineHeight);
        float x = MARGIN;

        for (Run run : runs) {
            for (String part : run.text().split("(?<= )", -1)) {
                if (part.isEmpty()) {
                    continue;
                }
                float partWidth = width(run.font(), run.size(), part);
                if (x + partWidth > MARGIN + CONTENT_WIDTH && x > MARGIN) {
                    cursorY -= lineHeight;
                    x = MARGIN;
                    ensureSpace(lineHeight);
                }
                text(part, run.font(), run.size(), run.red(), run.green(), run.blue(), x, cursorY);
                if (run.underline()) {
                    String visible = part.stripTrailing();
                    if (!visible.isEmpty()) {
                        float underlineWidth = width(run.font(), run.size(), visible);
                        line(x, cursorY - 2.2f, x + underlineWidth, cursorY - 2.2f,
                                run.red(), run.green(), run.blue(), 0.55f);
                    }
                }
                x += partWidth;
            }
        }
        cursorY -= lineHeight;
    }

    private void drawSizeSection() throws IOException {
        section("3", "Ukuran Font Berbeda");

        float[] sizes = {9, 11, 14, 18, 24, 30, 36};
        for (float size : sizes) {
            ensureSpace(size + 10);
            text((int) size + " pt", courier, 8, MUTED, MUTED, MUTED, MARGIN, cursorY - 3);
            text("Contoh ukuran " + (int) size + " pt", helvetica, size,
                    DARK_R, DARK_G, DARK_B, MARGIN + 64, cursorY);
            cursorY -= size + 8;
        }
        cursorY -= 4;
    }

    private void drawImageSection() throws IOException {
        byte[] jpeg = resource("/images/contoh.jpg");
        int[] dimensions = jpegDimensions(jpeg);

        float maxHeight = 205;
        float scale = Math.min(CONTENT_WIDTH / dimensions[0], maxHeight / dimensions[1]);
        float width = dimensions[0] * scale;
        float height = dimensions[1] * scale;

        section("4", "Gambar (JPEG di-embed tanpa ImageIO)", height + 34);

        PDImageXObject image = new PDImageXObject(document, new ByteArrayInputStream(jpeg),
                COSName.DCT_DECODE, dimensions[0], dimensions[1], 8, PDDeviceRGB.INSTANCE);

        cursorY -= height;
        content.drawImage(image, MARGIN + (CONTENT_WIDTH - width) / 2, cursorY, width, height);
        cursorY -= 14;
        text("Gambar 1. Banner contoh, di-embed langsung sebagai JPEG (DCTDecode).",
                helveticaOblique, 8.5f, MUTED, MUTED, MUTED, MARGIN, cursorY);
        cursorY -= 20;
    }

    private void drawBarChartSection() throws IOException {
        String[] labels = {"Jan", "Feb", "Mar", "Apr", "Mei", "Jun",
                "Jul", "Agu", "Sep", "Okt", "Nov", "Des"};
        double[] values = {42, 58, 51, 73, 66, 88, 79, 92, 84, 97, 90, 105};

        float chartHeight = 230;
        section("5", "Chart Batang", chartHeight + 12);
        float bottom = cursorY - chartHeight;
        charts.barChart(content, MARGIN, bottom, CONTENT_WIDTH, chartHeight,
                "Pendapatan Bulanan 2025", "dalam juta rupiah", labels, values,
                BRAND_R, BRAND_G, BRAND_B);
        cursorY = bottom - 24;
    }

    private void drawLineChartSection() throws IOException {
        String[] labels = {"Jan", "Feb", "Mar", "Apr", "Mei", "Jun"};

        float chartHeight = 240;
        section("6", "Chart Garis (dua seri)", chartHeight + 12);
        float bottom = cursorY - chartHeight;
        charts.lineChart(content, MARGIN, bottom, CONTENT_WIDTH, chartHeight,
                "Perbandingan Pendapatan 2024 vs 2025", "dalam juta rupiah", labels,
                List.of(
                        new ChartPainter.Series("2024", 0.98f, 0.57f, 0.24f,
                                new double[]{42, 58, 51, 73, 66, 88}),
                        new ChartPainter.Series("2025", BRAND_R, BRAND_G, BRAND_B,
                                new double[]{55, 71, 68, 91, 84, 105})
                ));
        cursorY = bottom - 24;
    }

    private void drawSummarySection() throws IOException {
        String[][] rows = {
                {"Font family", "Helvetica, Times, Courier (standard 14)"},
                {"Gaya teks", "Bold, italic, bold-italic, underline"},
                {"Ukuran font", "9 pt sampai 36 pt"},
                {"Gambar", "JPEG di-embed sebagai DCTDecode"},
                {"Chart", "Bar chart dan line chart, digambar vektor"},
                {"Dependensi", "quarkus-pdfbox 1.2.3 (PDFBox 3.0.8)"}
        };

        float rowHeight = 20;
        float firstColumn = CONTENT_WIDTH * 0.34f;

        section("7", "Ringkasan Fitur", rowHeight * (rows.length + 1) + 8);
        float tableTop = cursorY;

        content.setNonStrokingColor(BRAND_R, BRAND_G, BRAND_B);
        content.addRect(MARGIN, tableTop - rowHeight, CONTENT_WIDTH, rowHeight);
        content.fill();
        text("Fitur", helveticaBold, 8.5f, 1f, 1f, 1f, MARGIN + 8, tableTop - 13);
        text("Implementasi", helveticaBold, 8.5f, 1f, 1f, 1f,
                MARGIN + firstColumn + 8, tableTop - 13);
        cursorY = tableTop - rowHeight;

        for (int i = 0; i < rows.length; i++) {
            if (i % 2 == 0) {
                content.setNonStrokingColor(0.965f, 0.973f, 0.985f);
                content.addRect(MARGIN, cursorY - rowHeight, CONTENT_WIDTH, rowHeight);
                content.fill();
            }
            text(rows[i][0], helveticaBold, 8.5f, DARK_R, DARK_G, DARK_B,
                    MARGIN + 8, cursorY - 13);
            text(rows[i][1], helvetica, 8.5f, MUTED, MUTED, MUTED,
                    MARGIN + firstColumn + 8, cursorY - 13);
            cursorY -= rowHeight;
        }

        line(MARGIN, cursorY, MARGIN + CONTENT_WIDTH, cursorY, RULE, RULE, RULE, 0.7f);
        line(MARGIN + firstColumn, tableTop, MARGIN + firstColumn, cursorY,
                RULE, RULE, RULE, 0.55f);

        cursorY -= 18;
        float boxHeight = 62;
        ensureSpace(boxHeight);
        float boxTop = cursorY;

        content.setNonStrokingColor(0.945f, 0.968f, 1f);
        content.addRect(MARGIN, boxTop - boxHeight, CONTENT_WIDTH, boxHeight);
        content.fill();
        content.setNonStrokingColor(BRAND_R, BRAND_G, BRAND_B);
        content.addRect(MARGIN, boxTop - boxHeight, 3, boxHeight);
        content.fill();

        text("Native image (GraalVM)", helveticaBold, 9, BRAND_R, BRAND_G, BRAND_B,
                MARGIN + 12, boxTop - 16);
        text("Ekstensi quarkus-pdfbox 1.2.3 mendaftarkan reflection, resource AFM/glyphlist,",
                helvetica, 8.3f, MUTED, MUTED, MUTED, MARGIN + 12, boxTop - 31);
        text("charset, dan quarkus-awt. Standard 14 font tidak butuh font sistem, jadi aman",
                helvetica, 8.3f, MUTED, MUTED, MUTED, MARGIN + 12, boxTop - 43);
        text("dijalankan sebagai native image.",
                helvetica, 8.3f, MUTED, MUTED, MUTED, MARGIN + 12, boxTop - 55);
        cursorY = boxTop - boxHeight - 10;
    }

    private void drawFooters() throws IOException {
        int total = pages.size();
        for (int i = 0; i < total; i++) {
            try (PDPageContentStream footer = new PDPageContentStream(document, pages.get(i),
                    AppendMode.APPEND, true)) {
                footer.moveTo(MARGIN, MARGIN - 12);
                footer.lineTo(MARGIN + CONTENT_WIDTH, MARGIN - 12);
                footer.setStrokingColor(RULE, RULE, RULE);
                footer.setLineWidth(0.6f);
                footer.stroke();

                write(footer, "Quarkus + Quarkus PDFBox 1.2.3", courier, 8,
                        MUTED, MUTED, MUTED, MARGIN, MARGIN - 26);

                String pageLabel = "Halaman " + (i + 1) + " dari " + total;
                write(footer, pageLabel, courier, 8, MUTED, MUTED, MUTED,
                        MARGIN + CONTENT_WIDTH - width(courier, 8, pageLabel), MARGIN - 26);
            }
        }
    }

    private void text(String value, PDFont font, float size,
                      float red, float green, float blue, float x, float baseline)
            throws IOException {
        write(content, value, font, size, red, green, blue, x, baseline);
    }

    private static void write(PDPageContentStream stream, String value, PDFont font, float size,
                              float red, float green, float blue, float x, float baseline)
            throws IOException {
        stream.beginText();
        stream.setFont(font, size);
        stream.setNonStrokingColor(red, green, blue);
        stream.newLineAtOffset(x, baseline);
        stream.showText(value);
        stream.endText();
    }

    private void line(float x1, float y1, float x2, float y2,
                      float red, float green, float blue, float lineWidth) throws IOException {
        content.setStrokingColor(red, green, blue);
        content.setLineWidth(lineWidth);
        content.moveTo(x1, y1);
        content.lineTo(x2, y2);
        content.stroke();
    }

    private static float width(PDFont font, float size, String value) throws IOException {
        return font.getStringWidth(value) / 1000f * size;
    }

    private static byte[] resource(String path) throws IOException {
        try (InputStream input = PdfDemoGenerator.class.getResourceAsStream(path)) {
            if (input == null) {
                throw new IOException("Resource tidak ditemukan: " + path);
            }
            return input.readAllBytes();
        }
    }

    private static int[] jpegDimensions(byte[] data) throws IOException {
        int index = 2;
        while (index + 9 < data.length) {
            if ((data[index] & 0xFF) != 0xFF) {
                index++;
                continue;
            }
            int marker = data[index + 1] & 0xFF;
            index += 2;
            if (marker == 0xD8 || marker == 0x01 || (marker >= 0xD0 && marker <= 0xD7)) {
                continue;
            }
            if (marker == 0xD9 || marker == 0xDA) {
                break;
            }
            int length = ((data[index] & 0xFF) << 8) | (data[index + 1] & 0xFF);
            boolean startOfFrame = marker >= 0xC0 && marker <= 0xCF
                    && marker != 0xC4 && marker != 0xC8 && marker != 0xCC;
            if (startOfFrame) {
                int height = ((data[index + 3] & 0xFF) << 8) | (data[index + 4] & 0xFF);
                int width = ((data[index + 5] & 0xFF) << 8) | (data[index + 6] & 0xFF);
                return new int[]{width, height};
            }
            index += length;
        }
        throw new IOException("Tidak dapat membaca dimensi JPEG");
    }
}

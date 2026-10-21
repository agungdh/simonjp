package id.my.agungdh.office;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.geom.Rectangle2D;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import org.apache.poi.sl.usermodel.Insets2D;
import org.apache.poi.sl.usermodel.PictureData;
import org.apache.poi.sl.usermodel.ShapeType;
import org.apache.poi.sl.usermodel.TableCell;
import org.apache.poi.sl.usermodel.TextParagraph.TextAlign;
import org.apache.poi.sl.usermodel.TextShape.TextAutofit;
import org.apache.poi.sl.usermodel.VerticalAlignment;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFAutoShape;
import org.apache.poi.xslf.usermodel.XSLFPictureData;
import org.apache.poi.xslf.usermodel.XSLFPictureShape;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.apache.poi.xslf.usermodel.XSLFTable;
import org.apache.poi.xslf.usermodel.XSLFTableCell;
import org.apache.poi.xslf.usermodel.XSLFTableRow;
import org.apache.poi.xslf.usermodel.XSLFTextBox;
import org.apache.poi.xslf.usermodel.XSLFTextParagraph;
import org.apache.poi.xslf.usermodel.XSLFTextRun;
import org.apache.poi.xslf.usermodel.XSLFTextShape;

public final class PptxDemoGenerator {

    private static final int WIDTH = 960;
    private static final int HEIGHT = 540;

    // Kept as RGB constants: static java.awt.Color fields end up in the native image heap,
    // which GraalVM rejects for run-time initialized AWT classes.
    private static final int BRAND = 0x2563EB;
    private static final int DARK = 0x1C212E;
    private static final int MUTED = 0x6B7384;
    private static final int RULE = 0xDEE3EC;
    private static final int PANEL = 0xF7F9FC;
    private static final int ON_BRAND = 0xE0E7FF;
    private static final int WHITE = 0xFFFFFF;
    private static final int RED = 0xDC2626;
    private static final int GREEN = 0x059669;
    private static final int PURPLE = 0x7C3AED;
    private static final int AMBER = 0xF59E0B;

    private static final String[] MONTHS = {"Jan", "Feb", "Mar", "Apr", "Mei", "Jun",
            "Jul", "Agu", "Sep", "Okt", "Nov", "Des"};
    private static final double[] REVENUE = {42, 58, 51, 73, 66, 88, 79, 92, 84, 97, 90, 105};

    private static final List<FontSample> FONT_SAMPLES = List.of(
            new FontSample("Calibri", "Calibri Regular - sans-serif, 20 pt", false, false),
            new FontSample("Arial", "Arial Bold - sans-serif tebal, 20 pt", true, false),
            new FontSample("Times New Roman", "Times New Roman Italic - serif miring", false, true),
            new FontSample("Georgia", "Georgia - serif, 20 pt", false, false),
            new FontSample("Courier New", "Courier New - monospace, 18 pt", false, false),
            new FontSample("Consolas", "Consolas - monospace, 18 pt", false, false),
            new FontSample("Verdana", "Verdana - sans-serif lebar, 18 pt", false, false));

    private PptxDemoGenerator() {
    }

    public static byte[] build() throws IOException {
        try (XMLSlideShow show = new XMLSlideShow()) {
            show.setPageSize(new Dimension(WIDTH, HEIGHT));
            show.getProperties().getCoreProperties().setTitle("Contoh Generate PowerPoint");
            show.getProperties().getCoreProperties().setCreator("simonjp");
            show.getProperties().getCoreProperties()
                    .setDescription("Demo Apache POI: slide, font, gambar, chart dan tabel");

            drawCover(show);
            drawFontSlide(show);
            drawStyleSlide(show);
            drawSizeSlide(show);
            drawImageSlide(show);
            drawChartSlide(show);
            drawTableSlide(show);
            drawClosingSlide(show);

            ByteArrayOutputStream output = new ByteArrayOutputStream();
            show.write(output);
            return output.toByteArray();
        }
    }

    private static void drawCover(XMLSlideShow show) {
        XSLFSlide slide = show.createSlide();

        rectangle(slide, 0, 0, 360, HEIGHT, color(BRAND));

        XSLFTextBox title = box(slide, 52, 150, 280, 220);
        for (String line : List.of("Contoh", "Generate", "PowerPoint")) {
            run(paragraph(title), line, "Calibri", 40, true, false, false, color(WHITE));
        }

        XSLFTextBox subtitle = box(slide, 52, 392, 270, 90);
        run(paragraph(subtitle), "Demo Apache POI 5.5.1", "Calibri", 12, false, false, false,
                color(ON_BRAND));
        run(subtitle.addNewTextParagraph(), "XMLSlideShow / XSLF", "Consolas", 11,
                false, false, false, color(ON_BRAND));

        XSLFTextBox heading = box(slide, 424, 150, 480, 60);
        run(paragraph(heading), "Slide Otomatis dari Quarkus", "Calibri", 26, true, false, false,
                color(DARK));

        XSLFTextBox bullets = box(slide, 424, 224, 480, 180);
        for (String bullet : List.of(
                "Variasi font dan gaya teks",
                "Gambar JPEG di-embed ke slide",
                "Chart batang dan garis (PNG)",
                "Tabel ringkasan fitur")) {
            run(paragraph(bullets), "\u2022  " + bullet, "Calibri", 16, false, false, false, color(MUTED));
        }

        XSLFTextBox meta = box(slide, 424, 430, 480, 30);
        run(paragraph(meta), "16:9  \u2022  Quarkus REST  \u2022  Apache POI 5.5.1",
                "Consolas", 11, false, false, false, color(BRAND));
    }

    private static void drawFontSlide(XMLSlideShow show) {
        XSLFSlide slide = show.createSlide();
        heading(slide, "1", "Berbagai Jenis Font");

        double y = 112;
        for (FontSample sample : FONT_SAMPLES) {
            XSLFTextBox label = box(slide, 56, y + 8, 130, 22);
            run(paragraph(label), sample.family(), "Consolas", 9, false, false, false, color(MUTED));

            XSLFTextBox text = box(slide, 190, y, 714, 34);
            run(paragraph(text), sample.sample(), sample.family(), 20, sample.bold(),
                    sample.italic(), false, color(DARK));
            y += 52;
        }
    }

    private static void drawStyleSlide(XMLSlideShow show) {
        XSLFSlide slide = show.createSlide();
        heading(slide, "2", "Gaya Teks: Bold, Italic, dan Underline");

        XSLFTextBox first = box(slide, 56, 112, 848, 40);
        XSLFTextParagraph firstParagraph = paragraph(first);
        run(firstParagraph, "Teks ", "Calibri", 18, false, false, false, color(DARK));
        run(firstParagraph, "normal", "Calibri", 18, false, false, false, color(DARK));
        run(firstParagraph, ", ", "Calibri", 18, false, false, false, color(DARK));
        run(firstParagraph, "tebal", "Calibri", 18, true, false, false, color(DARK));
        run(firstParagraph, ", ", "Calibri", 18, false, false, false, color(DARK));
        run(firstParagraph, "miring", "Calibri", 18, false, true, false, color(DARK));
        run(firstParagraph, ", ", "Calibri", 18, false, false, false, color(DARK));
        run(firstParagraph, "tebal miring", "Calibri", 18, true, true, false, color(DARK));
        run(firstParagraph, ", dan ", "Calibri", 18, false, false, false, color(DARK));
        run(firstParagraph, "bergaris bawah", "Calibri", 18, false, false, true, color(BRAND));

        XSLFTextBox second = box(slide, 56, 176, 848, 40);
        XSLFTextParagraph secondParagraph = paragraph(second);
        run(secondParagraph, "Times New Roman: ", "Times New Roman", 18, false, false, false,
                color(DARK));
        run(secondParagraph, "bold", "Times New Roman", 18, true, false, false, color(DARK));
        run(secondParagraph, ", ", "Times New Roman", 18, false, false, false, color(DARK));
        run(secondParagraph, "italic", "Times New Roman", 18, false, true, false, color(DARK));
        run(secondParagraph, ", ", "Times New Roman", 18, false, false, false, color(DARK));
        run(secondParagraph, "bold italic", "Times New Roman", 18, true, true, false, color(DARK));
        run(secondParagraph, ", ", "Times New Roman", 18, false, false, false, color(DARK));
        run(secondParagraph, "underline", "Times New Roman", 18, false, false, true, color(DARK));

        XSLFTextBox third = box(slide, 56, 240, 848, 40);
        XSLFTextParagraph thirdParagraph = paragraph(third);
        run(thirdParagraph, "Teks berwarna: ", "Calibri", 18, false, false, false, color(DARK));
        run(thirdParagraph, "merah", "Calibri", 18, true, false, false, color(RED));
        run(thirdParagraph, ", ", "Calibri", 18, false, false, false, color(DARK));
        run(thirdParagraph, "hijau", "Calibri", 18, true, false, false, color(GREEN));
        run(thirdParagraph, ", ", "Calibri", 18, false, false, false, color(DARK));
        run(thirdParagraph, "biru", "Calibri", 18, true, false, false, color(BRAND));
        run(thirdParagraph, ", dan ", "Calibri", 18, false, false, false, color(DARK));
        run(thirdParagraph, "ungu bergaris bawah", "Consolas", 17, true, false, true, color(PURPLE));

        XSLFTextBox fourth = box(slide, 56, 304, 848, 40);
        XSLFTextParagraph fourthParagraph = paragraph(fourth);
        run(fourthParagraph, "Kombinasi lengkap: ", "Calibri", 18, false, false, false, color(DARK));
        run(fourthParagraph, "bold italic underline", "Calibri", 18, true, true, true, color(PURPLE));

        rectangle(slide, 56, 380, 848, 90, color(PANEL));
        XSLFTextBox note = box(slide, 76, 396, 808, 58);
        run(paragraph(note),
                "Ukuran font, ketebalan, kemiringan, garis bawah dan warna diatur per run "
                        + "di dalam satu paragraf, sama seperti pada demo Word.",
                "Calibri", 13, false, true, false, color(MUTED));
    }

    private static void drawSizeSlide(XMLSlideShow show) {
        XSLFSlide slide = show.createSlide();
        heading(slide, "3", "Ukuran Font Berbeda");

        int[] sizes = {12, 16, 20, 24, 28, 32, 36};
        double y = 110;
        for (int size : sizes) {
            XSLFTextBox label = box(slide, 56, y + 6, 90, 24);
            run(paragraph(label), size + " pt", "Consolas", 10, false, false, false, color(MUTED));

            XSLFTextBox text = box(slide, 150, y - 4, 754, size + 16);
            run(paragraph(text), "Contoh ukuran " + size + " pt", "Calibri", size,
                    false, false, false, color(DARK));
            y += 54;
        }
    }

    private static void drawImageSlide(XMLSlideShow show) throws IOException {
        XSLFSlide slide = show.createSlide();
        heading(slide, "4", "Gambar (JPEG di-embed ke slide)");

        byte[] jpeg = ImageAssets.read("/images/contoh.jpg");
        int[] dimensions = ImageAssets.jpegSize(jpeg);
        picture(show, slide, jpeg, PictureData.PictureType.JPEG,
                56, 106, 848, 336, dimensions[0], dimensions[1]);

        XSLFTextBox caption = box(slide, 56, 452, 848, 28);
        paragraph(caption).setTextAlign(TextAlign.CENTER);
        run(paragraph(caption), "Gambar 1. Banner contoh, di-embed langsung sebagai JPEG.",
                "Georgia", 11, false, true, false, color(MUTED));
    }

    private static void drawChartSlide(XMLSlideShow show) throws IOException {
        XSLFSlide slide = show.createSlide();
        heading(slide, "5", "Chart (dirender menjadi gambar PNG)");

        byte[] bar = ChartImageRenderer.barChart("Pendapatan Bulanan 2025",
                "dalam juta rupiah", MONTHS, REVENUE, color(BRAND));
        picture(show, slide, bar, PictureData.PictureType.PNG,
                56, 108, 414, 200, ChartImageRenderer.WIDTH, ChartImageRenderer.HEIGHT);

        byte[] line = ChartImageRenderer.lineChart("Perbandingan Pendapatan",
                "dalam juta rupiah", new String[]{"Jan", "Feb", "Mar", "Apr", "Mei", "Jun"},
                List.of(
                        new ChartImageRenderer.Series("2024", color(AMBER),
                                new double[]{42, 58, 51, 73, 66, 88}),
                        new ChartImageRenderer.Series("2025", color(BRAND),
                                new double[]{55, 71, 68, 91, 84, 105})));
        picture(show, slide, line, PictureData.PictureType.PNG,
                490, 108, 414, 200, ChartImageRenderer.WIDTH, ChartImageRenderer.HEIGHT);

        XSLFTextBox barCaption = box(slide, 56, 322, 414, 28);
        paragraph(barCaption).setTextAlign(TextAlign.CENTER);
        run(paragraph(barCaption), "Grafik batang pendapatan 2025", "Calibri", 12,
                false, true, false, color(MUTED));

        XSLFTextBox lineCaption = box(slide, 490, 322, 414, 28);
        paragraph(lineCaption).setTextAlign(TextAlign.CENTER);
        run(paragraph(lineCaption), "Grafik garis perbandingan 2024 vs 2025", "Calibri", 12,
                false, true, false, color(MUTED));

        rectangle(slide, 56, 386, 848, 80, color(PANEL));
        XSLFTextBox note = box(slide, 76, 402, 808, 50);
        run(paragraph(note),
                "Chart dibuat dengan Java2D lalu di-embed sebagai PNG, sama seperti "
                        + "demo Word dan Excel.",
                "Calibri", 13, false, true, false, color(MUTED));
    }

    private static void drawTableSlide(XMLSlideShow show) {
        XSLFSlide slide = show.createSlide();
        heading(slide, "6", "Ringkasan Fitur");

        String[][] rows = {
                {"Fitur", "Implementasi", "Status"},
                {"Font family", "Calibri, Arial, Times New Roman, Georgia", "OK"},
                {"Font monospace", "Courier New, Consolas", "OK"},
                {"Gaya teks", "Bold, italic, bold-italic, underline", "OK"},
                {"Ukuran font", "12 pt sampai 36 pt", "OK"},
                {"Gambar", "JPEG di-embed ke slide", "OK"},
                {"Chart", "Bar chart dan line chart (PNG)", "OK"}
        };
        double[] columnWidths = {230, 470, 148};

        XSLFTable table = slide.createTable(rows.length, rows[0].length);
        table.setAnchor(new Rectangle2D.Double(56, 110, 848, 330));
        for (int column = 0; column < columnWidths.length; column++) {
            table.setColumnWidth(column, columnWidths[column]);
        }

        for (int rowIndex = 0; rowIndex < rows.length; rowIndex++) {
            XSLFTableRow row = table.getRows().get(rowIndex);
            row.setHeight(44);
            for (int columnIndex = 0; columnIndex < rows[rowIndex].length; columnIndex++) {
                XSLFTableCell cell = table.getCell(rowIndex, columnIndex);
                cell.setText(rows[rowIndex][columnIndex]);
                cell.setVerticalAlignment(VerticalAlignment.MIDDLE);
                cell.setFillColor(rowIndex == 0 ? color(BRAND) : rowIndex % 2 == 0 ? color(PANEL) : color(WHITE));
                for (TableCell.BorderEdge edge : TableCell.BorderEdge.values()) {
                    cell.setBorderColor(edge, color(RULE));
                    cell.setBorderWidth(edge, 1.0);
                }

                XSLFTextParagraph paragraph = cell.getTextParagraphs().get(0);
                paragraph.setTextAlign(columnIndex == 2 ? TextAlign.CENTER : TextAlign.LEFT);
                XSLFTextRun run = paragraph.getTextRuns().get(0);
                run.setFontFamily("Calibri");
                run.setFontSize(11.0);
                run.setBold(rowIndex == 0);
                run.setFontColor(rowIndex == 0 ? color(WHITE) : color(DARK));
            }
        }
    }

    private static void drawClosingSlide(XMLSlideShow show) {
        XSLFSlide slide = show.createSlide();
        rectangle(slide, 0, 0, WIDTH, HEIGHT, color(BRAND));

        XSLFTextBox title = box(slide, 96, 208, 768, 70);
        paragraph(title).setTextAlign(TextAlign.CENTER);
        run(paragraph(title), "Terima kasih", "Calibri", 40, true, false, false, color(WHITE));

        XSLFTextBox subtitle = box(slide, 96, 284, 768, 40);
        paragraph(subtitle).setTextAlign(TextAlign.CENTER);
        run(paragraph(subtitle), "Dibuat otomatis dengan Quarkus + Apache POI",
                "Calibri", 16, false, false, false, color(ON_BRAND));

        XSLFTextBox endpoint = box(slide, 96, 356, 768, 30);
        paragraph(endpoint).setTextAlign(TextAlign.CENTER);
        run(paragraph(endpoint), "GET /ppt/contoh", "Consolas", 13, false, false, false, color(WHITE));
    }

    private static void heading(XSLFSlide slide, String number, String title) {
        XSLFTextBox box = box(slide, 56, 34, 848, 46);
        run(paragraph(box), number + ". " + title, "Calibri", 26, true, false, false, color(BRAND));
        rectangle(slide, 56, 84, 848, 3, color(RULE));
    }

    private static XSLFTextBox box(XSLFSlide slide, double x, double y, double width,
                                   double height) {
        XSLFTextBox box = slide.createTextBox();
        box.setAnchor(new Rectangle2D.Double(x, y, width, height));
        box.setInsets(new Insets2D(0, 0, 0, 0));
        box.setWordWrap(true);
        box.setTextAutofit(TextAutofit.NONE);
        return box;
    }

    /**
     * Returns the empty paragraph a freshly created text shape already has, or a new
     * paragraph once the current one holds runs.
     */
    private static XSLFTextParagraph paragraph(XSLFTextShape shape) {
        List<XSLFTextParagraph> paragraphs = shape.getTextParagraphs();
        XSLFTextParagraph last = paragraphs.get(paragraphs.size() - 1);
        if (last.getTextRuns().isEmpty()) {
            return last;
        }
        return shape.addNewTextParagraph();
    }

    private static XSLFTextRun run(XSLFTextParagraph paragraph, String text, String font,
                                   double size, boolean bold, boolean italic, boolean underline,
                                   Color color) {
        XSLFTextRun run = paragraph.addNewTextRun();
        run.setText(text);
        run.setFontFamily(font);
        run.setFontSize(size);
        run.setBold(bold);
        run.setItalic(italic);
        run.setUnderlined(underline);
        run.setFontColor(color);
        return run;
    }

    private static Color color(int rgb) {
        return new Color(rgb);
    }

    private static XSLFAutoShape rectangle(XSLFSlide slide, double x, double y, double width,
                                           double height, Color color) {
        XSLFAutoShape shape = slide.createAutoShape();
        shape.setShapeType(ShapeType.RECT);
        shape.setAnchor(new Rectangle2D.Double(x, y, width, height));
        shape.setFillColor(color);
        shape.setLineColor(null);
        return shape;
    }

    private static void picture(XMLSlideShow show, XSLFSlide slide, byte[] data,
                                PictureData.PictureType type, double x, double y,
                                double maxWidth, double maxHeight, int pixelWidth,
                                int pixelHeight) {
        double scale = Math.min(1.0, Math.min(maxWidth / pixelWidth, maxHeight / pixelHeight));
        double width = pixelWidth * scale;
        double height = pixelHeight * scale;
        double left = x + (maxWidth - width) / 2;
        double top = y + (maxHeight - height) / 2;

        XSLFPictureData picture = show.addPicture(data, type);
        XSLFPictureShape shape = slide.createPicture(picture);
        shape.setAnchor(new Rectangle2D.Double(left, top, width, height));
    }

    private record FontSample(String family, String sample, boolean bold, boolean italic) {
    }
}

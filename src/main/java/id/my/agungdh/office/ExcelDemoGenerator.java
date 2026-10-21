package id.my.agungdh.office;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.FontUnderline;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xddf.usermodel.XDDFColor;
import org.apache.poi.xddf.usermodel.XDDFLineProperties;
import org.apache.poi.xddf.usermodel.XDDFShapeProperties;
import org.apache.poi.xddf.usermodel.XDDFSolidFillProperties;
import org.apache.poi.xddf.usermodel.chart.AxisCrosses;
import org.apache.poi.xddf.usermodel.chart.AxisPosition;
import org.apache.poi.xddf.usermodel.chart.BarDirection;
import org.apache.poi.xddf.usermodel.chart.BarGrouping;
import org.apache.poi.xddf.usermodel.chart.ChartTypes;
import org.apache.poi.xddf.usermodel.chart.LegendPosition;
import org.apache.poi.xddf.usermodel.chart.XDDFBarChartData;
import org.apache.poi.xddf.usermodel.chart.XDDFCategoryAxis;
import org.apache.poi.xddf.usermodel.chart.XDDFChartData;
import org.apache.poi.xddf.usermodel.chart.XDDFChartLegend;
import org.apache.poi.xddf.usermodel.chart.XDDFDataSource;
import org.apache.poi.xddf.usermodel.chart.XDDFDataSourcesFactory;
import org.apache.poi.xddf.usermodel.chart.XDDFLineChartData;
import org.apache.poi.xddf.usermodel.chart.XDDFNumericalDataSource;
import org.apache.poi.xddf.usermodel.chart.XDDFValueAxis;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFChart;
import org.apache.poi.xssf.usermodel.XSSFClientAnchor;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFDrawing;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFPicture;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public final class ExcelDemoGenerator {

    static final int DATA_HEADER_ROW = 3;
    static final int DATA_FIRST_ROW = 4;
    static final int DATA_LAST_ROW = DATA_FIRST_ROW + 11;

    private static final String[] MONTHS = {"Jan", "Feb", "Mar", "Apr", "Mei", "Jun",
            "Jul", "Agu", "Sep", "Okt", "Nov", "Des"};
    private static final double[] REVENUE_2024 = {42, 58, 51, 73, 66, 88,
            79, 92, 84, 97, 90, 105};
    private static final double[] REVENUE_2025 = {55, 71, 68, 91, 84, 105,
            98, 116, 108, 124, 117, 138};

    private record FontRow(String font, int size, boolean bold, boolean italic,
                           boolean underline, IndexedColors color, String sample) {
    }

    private static final List<FontRow> FONT_ROWS = List.of(
            new FontRow("Calibri", 11, false, false, false, null, "Calibri regular 11 pt"),
            new FontRow("Calibri", 11, true, false, false, null, "Calibri bold 11 pt"),
            new FontRow("Calibri", 11, false, true, false, null, "Calibri italic 11 pt"),
            new FontRow("Calibri", 11, true, true, false, null, "Calibri bold italic 11 pt"),
            new FontRow("Calibri", 11, false, false, true, null, "Calibri underline 11 pt"),
            new FontRow("Calibri", 11, true, true, true, null, "Calibri bold italic underline"),
            new FontRow("Arial", 14, false, false, false, null, "Arial regular 14 pt"),
            new FontRow("Times New Roman", 14, false, true, false, null,
                    "Times New Roman italic 14 pt"),
            new FontRow("Georgia", 16, true, false, false, IndexedColors.DARK_RED,
                    "Georgia bold merah 16 pt"),
            new FontRow("Courier New", 11, false, false, true, IndexedColors.DARK_GREEN,
                    "Courier New underline hijau"),
            new FontRow("Verdana", 18, true, false, false, IndexedColors.DARK_BLUE,
                    "Verdana bold biru 18 pt"),
            new FontRow("Consolas", 20, false, false, false, IndexedColors.VIOLET,
                    "Consolas ungu 20 pt"),
            new FontRow("Calibri", 28, true, false, false, null, "Calibri bold 28 pt"),
            new FontRow("Calibri", 36, true, true, true, IndexedColors.DARK_RED,
                    "Calibri 36 pt lengkap"));

    private ExcelDemoGenerator() {
    }

    public static byte[] build() throws IOException {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            workbook.setForceFormulaRecalculation(true);

            buildStyleSheet(workbook);
            XSSFSheet dataSheet = buildDataSheet(workbook);
            buildChartSheet(workbook, dataSheet);

            workbook.setActiveSheet(0);

            ByteArrayOutputStream output = new ByteArrayOutputStream();
            workbook.write(output);
            return output.toByteArray();
        }
    }

    private static void buildStyleSheet(XSSFWorkbook workbook) {
        XSSFSheet sheet = workbook.createSheet("Gaya Teks");
        sheet.setTabColor(new XSSFColor(IndexedColors.BLUE, null));

        int[] widths = {24, 10, 9, 9, 11, 48};
        for (int column = 0; column < widths.length; column++) {
            sheet.setColumnWidth(column, widths[column] * 256);
        }

        XSSFRow titleRow = sheet.createRow(0);
        titleRow.setHeightInPoints(36);
        cell(titleRow, 0, "Contoh Variasi Font dan Gaya Teks",
                titleStyle(workbook, 20, HorizontalAlignment.LEFT));
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 5));

        XSSFRow subtitleRow = sheet.createRow(1);
        subtitleRow.setHeightInPoints(22);
        cell(subtitleRow, 0, "Setiap baris memakai font, ukuran, dan gaya yang berbeda",
                subtitleStyle(workbook));
        sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 5));

        XSSFRow headerRow = sheet.createRow(3);
        headerRow.setHeightInPoints(24);
        String[] headers = {"Font", "Ukuran", "Bold", "Italic", "Underline", "Contoh Teks"};
        for (int column = 0; column < headers.length; column++) {
            cell(headerRow, column, headers[column], headerStyle(workbook));
        }

        XSSFCellStyle metaCellStyle = metaStyle(workbook);
        for (int index = 0; index < FONT_ROWS.size(); index++) {
            FontRow spec = FONT_ROWS.get(index);
            XSSFRow row = sheet.createRow(4 + index);
            row.setHeightInPoints(Math.max(21, spec.size() * 1.65f));

            cell(row, 0, spec.font(), fontStyle(workbook, spec, HorizontalAlignment.LEFT));
            cell(row, 1, String.valueOf(spec.size()), metaCellStyle);
            cell(row, 2, spec.bold() ? "Ya" : "Tidak", metaCellStyle);
            cell(row, 3, spec.italic() ? "Ya" : "Tidak", metaCellStyle);
            cell(row, 4, spec.underline() ? "Ya" : "Tidak", metaCellStyle);
            cell(row, 5, spec.sample(), fontStyle(workbook, spec, HorizontalAlignment.LEFT));
        }

        sheet.createFreezePane(0, 4);
        printSetup(sheet);
    }

    private static XSSFSheet buildDataSheet(XSSFWorkbook workbook) throws IOException {
        XSSFSheet sheet = workbook.createSheet("Data");
        sheet.setTabColor(new XSSFColor(IndexedColors.GREEN, null));

        int[] widths = {14, 20, 20, 16};
        for (int column = 0; column < widths.length; column++) {
            sheet.setColumnWidth(column, widths[column] * 256);
        }

        XSSFRow titleRow = sheet.createRow(0);
        titleRow.setHeightInPoints(32);
        cell(titleRow, 0, "Data Pendapatan Bulanan", titleStyle(workbook, 18,
                HorizontalAlignment.LEFT));
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 3));

        XSSFRow subtitleRow = sheet.createRow(1);
        subtitleRow.setHeightInPoints(20);
        cell(subtitleRow, 0, "Nilai dalam juta rupiah. Kolom pertumbuhan memakai formula.",
                subtitleStyle(workbook));
        sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 3));

        XSSFRow headerRow = sheet.createRow(DATA_HEADER_ROW);
        headerRow.setHeightInPoints(24);
        String[] headers = {"Bulan", "Pendapatan 2024", "Pendapatan 2025", "Pertumbuhan"};
        XSSFCellStyle headerCellStyle = headerStyle(workbook);
        for (int column = 0; column < headers.length; column++) {
            cell(headerRow, column, headers[column], headerCellStyle);
        }

        XSSFCellStyle bandCellStyle = bandStyle(workbook);
        XSSFCellStyle revenueStyle = numberStyle(workbook, "#,##0");
        XSSFCellStyle growthStyle = numberStyle(workbook, "0.0%");

        for (int index = 0; index < MONTHS.length; index++) {
            int rowIndex = DATA_FIRST_ROW + index;
            XSSFRow row = sheet.createRow(rowIndex);
            row.setHeightInPoints(20);

            cell(row, 0, MONTHS[index], bandCellStyle);
            number(row, 1, REVENUE_2024[index], revenueStyle);
            number(row, 2, REVENUE_2025[index], revenueStyle);
            XSSFCell growthCell = row.createCell(3);
            growthCell.setCellFormula("C" + (rowIndex + 1) + "/B" + (rowIndex + 1) + "-1");
            growthCell.setCellStyle(growthStyle);
        }

        sheet.setAutoFilter(new CellRangeAddress(DATA_HEADER_ROW, DATA_LAST_ROW, 0, 3));
        sheet.createFreezePane(1, DATA_FIRST_ROW);

        insertPicture(workbook, sheet);

        XSSFRow captionRow = sheet.createRow(DATA_LAST_ROW + 12);
        cell(captionRow, 0, "Gambar 1. Banner contoh, di-embed ke sheet.",
                captionStyle(workbook));

        printSetup(sheet);
        return sheet;
    }

    private static void buildChartSheet(XSSFWorkbook workbook, XSSFSheet dataSheet) {
        XSSFSheet sheet = workbook.createSheet("Chart");
        sheet.setTabColor(new XSSFColor(IndexedColors.ORANGE, null));

        for (int column = 0; column < 10; column++) {
            sheet.setColumnWidth(column, 12 * 256);
        }

        XSSFRow titleRow = sheet.createRow(0);
        titleRow.setHeightInPoints(30);
        cell(titleRow, 0, "Chart Pendapatan", titleStyle(workbook, 18,
                HorizontalAlignment.LEFT));
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 9));

        XSSFDrawing drawing = sheet.createDrawingPatriarch();

        XSSFChart barChart = drawing.createChart(drawing.createAnchor(0, 0, 0, 0, 0, 2, 10, 21));
        barChart.setTitleText("Pendapatan Bulanan 2025");
        barChart.setTitleOverlay(false);
        XDDFChartLegend barLegend = barChart.getOrAddLegend();
        barLegend.setPosition(LegendPosition.TOP_RIGHT);

        XDDFCategoryAxis barCategoryAxis = barChart.createCategoryAxis(AxisPosition.BOTTOM);
        barCategoryAxis.setTitle("Bulan");
        XDDFValueAxis barValueAxis = barChart.createValueAxis(AxisPosition.LEFT);
        barValueAxis.setTitle("Juta Rupiah");
        barValueAxis.setCrosses(AxisCrosses.AUTO_ZERO);
        barValueAxis.setMinimum(0);
        barValueAxis.setMaximum(160);

        XDDFDataSource<String> months = XDDFDataSourcesFactory.fromStringCellRange(dataSheet,
                new CellRangeAddress(DATA_FIRST_ROW, DATA_LAST_ROW, 0, 0));
        XDDFNumericalDataSource<Double> revenue2025 = XDDFDataSourcesFactory.fromNumericCellRange(
                dataSheet, new CellRangeAddress(DATA_FIRST_ROW, DATA_LAST_ROW, 2, 2));

        XDDFBarChartData barData = (XDDFBarChartData) barChart.createData(ChartTypes.BAR,
                barCategoryAxis, barValueAxis);
        barData.setBarDirection(BarDirection.COL);
        barData.setBarGrouping(BarGrouping.CLUSTERED);
        barData.setVaryColors(false);
        XDDFChartData.Series barSeries = barData.addSeries(months, revenue2025);
        barSeries.setTitle("Pendapatan 2025", null);
        style(barSeries, 0x25, 0x63, 0xEB, false);
        barChart.plot(barData);

        XSSFChart lineChart = drawing.createChart(
                drawing.createAnchor(0, 0, 0, 0, 0, 22, 10, 41));
        lineChart.setTitleText("Perbandingan Pendapatan 2024 vs 2025");
        lineChart.setTitleOverlay(false);
        XDDFChartLegend lineLegend = lineChart.getOrAddLegend();
        lineLegend.setPosition(LegendPosition.TOP_RIGHT);

        XDDFCategoryAxis lineCategoryAxis = lineChart.createCategoryAxis(AxisPosition.BOTTOM);
        lineCategoryAxis.setTitle("Bulan");
        XDDFValueAxis lineValueAxis = lineChart.createValueAxis(AxisPosition.LEFT);
        lineValueAxis.setTitle("Juta Rupiah");
        lineValueAxis.setCrosses(AxisCrosses.AUTO_ZERO);
        lineValueAxis.setMinimum(0);
        lineValueAxis.setMaximum(160);

        XDDFNumericalDataSource<Double> revenue2024 = XDDFDataSourcesFactory.fromNumericCellRange(
                dataSheet, new CellRangeAddress(DATA_FIRST_ROW, DATA_LAST_ROW, 1, 1));

        XDDFLineChartData lineData = (XDDFLineChartData) lineChart.createData(ChartTypes.LINE,
                lineCategoryAxis, lineValueAxis);
        lineData.setVaryColors(false);
        XDDFChartData.Series series2024 = lineData.addSeries(months, revenue2024);
        series2024.setTitle("2024", null);
        style(series2024, 0xF5, 0x9E, 0x0B, true);
        XDDFChartData.Series series2025 = lineData.addSeries(months, revenue2025);
        series2025.setTitle("2025", null);
        style(series2025, 0x25, 0x63, 0xEB, true);
        lineChart.plot(lineData);

        printSetup(sheet);
    }

    private static void style(XDDFChartData.Series series, int red, int green, int blue,
                              boolean line) {
        XDDFColor color = XDDFColor.from(new byte[]{(byte) red, (byte) green, (byte) blue});
        XDDFShapeProperties properties = new XDDFShapeProperties();
        properties.setFillProperties(new XDDFSolidFillProperties(color));
        if (line) {
            properties.setLineProperties(
                    new XDDFLineProperties(new XDDFSolidFillProperties(color)));
        }
        series.setShapeProperties(properties);
    }

    private static void printSetup(XSSFSheet sheet) {
        sheet.setFitToPage(true);
        sheet.getPrintSetup().setLandscape(true);
        sheet.getPrintSetup().setFitWidth((short) 1);
        sheet.getPrintSetup().setFitHeight((short) 1);
    }

    private static void insertPicture(XSSFWorkbook workbook, XSSFSheet sheet) throws IOException {
        byte[] jpeg = resource("/images/contoh.jpg");
        int pictureIndex = workbook.addPicture(jpeg, Workbook.PICTURE_TYPE_JPEG);

        XSSFDrawing drawing = sheet.createDrawingPatriarch();
        XSSFClientAnchor anchor = drawing.createAnchor(0, 0, 0, 0, 0, DATA_LAST_ROW + 2,
                6, DATA_LAST_ROW + 11);
        XSSFPicture picture = drawing.createPicture(anchor, pictureIndex);
        picture.resize(0.42);
    }

    private static XSSFCell cell(XSSFRow row, int column, String value, XSSFCellStyle style) {
        XSSFCell cell = row.createCell(column);
        cell.setCellValue(value);
        cell.setCellStyle(style);
        return cell;
    }

    private static XSSFCell number(XSSFRow row, int column, double value,
                                   XSSFCellStyle style) {
        XSSFCell cell = row.createCell(column);
        cell.setCellValue(value);
        cell.setCellStyle(style);
        return cell;
    }

    private static XSSFCellStyle metaStyle(XSSFWorkbook workbook) {
        XSSFFont font = workbook.createFont();
        font.setFontName("Calibri");
        font.setFontHeightInPoints((short) 11);

        XSSFCellStyle style = workbook.createCellStyle();
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.HAIR);
        style.setBottomBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
        return style;
    }

    private static XSSFFont boldFont(XSSFWorkbook workbook, String name, int size) {
        XSSFFont font = workbook.createFont();
        font.setFontName(name);
        font.setFontHeightInPoints((short) size);
        font.setBold(true);
        return font;
    }

    private static XSSFCellStyle fontStyle(XSSFWorkbook workbook, FontRow spec,
                                            HorizontalAlignment alignment) {
        XSSFFont font = workbook.createFont();
        font.setFontName(spec.font());
        font.setFontHeightInPoints((short) spec.size());
        font.setBold(spec.bold());
        font.setItalic(spec.italic());
        if (spec.underline()) {
            font.setUnderline(FontUnderline.SINGLE);
        }
        if (spec.color() != null) {
            font.setColor(new XSSFColor(spec.color(), null));
        }

        XSSFCellStyle style = workbook.createCellStyle();
        style.setFont(font);
        style.setAlignment(alignment);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    private static XSSFCellStyle titleStyle(XSSFWorkbook workbook, int size,
                                            HorizontalAlignment alignment) {
        XSSFFont font = workbook.createFont();
        font.setFontName("Calibri");
        font.setFontHeightInPoints((short) size);
        font.setBold(true);
        font.setColor(new XSSFColor(IndexedColors.WHITE, null));

        XSSFCellStyle style = workbook.createCellStyle();
        style.setFont(font);
        style.setAlignment(alignment);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setIndention((short) 1);
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    private static XSSFCellStyle subtitleStyle(XSSFWorkbook workbook) {
        XSSFFont font = workbook.createFont();
        font.setFontName("Georgia");
        font.setFontHeightInPoints((short) 10);
        font.setItalic(true);
        font.setColor(new XSSFColor(IndexedColors.GREY_50_PERCENT, null));

        XSSFCellStyle style = workbook.createCellStyle();
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setIndention((short) 1);
        style.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    private static XSSFCellStyle captionStyle(XSSFWorkbook workbook) {
        XSSFFont font = workbook.createFont();
        font.setFontName("Georgia");
        font.setFontHeightInPoints((short) 10);
        font.setItalic(true);
        font.setColor(new XSSFColor(IndexedColors.GREY_50_PERCENT, null));

        XSSFCellStyle style = workbook.createCellStyle();
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setIndention((short) 1);
        return style;
    }

    private static XSSFCellStyle headerStyle(XSSFWorkbook workbook) {
        XSSFFont font = workbook.createFont();
        font.setFontName("Calibri");
        font.setFontHeightInPoints((short) 11);
        font.setBold(true);
        font.setColor(new XSSFColor(IndexedColors.WHITE, null));

        XSSFCellStyle style = workbook.createCellStyle();
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBottomBorderColor(IndexedColors.WHITE.getIndex());
        return style;
    }

    private static XSSFCellStyle bandStyle(XSSFWorkbook workbook) {
        XSSFCellStyle style = workbook.createCellStyle();
        style.setFont(boldFont(workbook, "Calibri", 11));
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.HAIR);
        style.setBottomBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
        return style;
    }

    private static XSSFCellStyle numberStyle(XSSFWorkbook workbook, String format) {
        XSSFCellStyle style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.RIGHT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setDataFormat(workbook.createDataFormat().getFormat(format));
        style.setBorderBottom(BorderStyle.HAIR);
        style.setBottomBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
        return style;
    }

    private static byte[] resource(String path) throws IOException {
        try (InputStream input = ExcelDemoGenerator.class.getResourceAsStream(path)) {
            if (input == null) {
                throw new IOException("Resource tidak ditemukan: " + path);
            }
            return input.readAllBytes();
        }
    }
}

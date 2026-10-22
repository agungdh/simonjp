package id.my.agungdh.pdf;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
final class ChartPainter {

    record Series(String name, float red, float green, float blue, double[] values) {
    }

    private static final float TEXT_R = 0.11f;
    private static final float TEXT_G = 0.13f;
    private static final float TEXT_B = 0.18f;
    private static final float MUTED = 0.42f;

    private final PDFont helvetica;
    private final PDFont helveticaBold;
    private final PDFont courier;

    void barChart(PDPageContentStream cs, float x, float y, float width, float height,
                  String title, String unit, String[] labels, double[] values,
                  float red, float green, float blue) throws IOException {
        float plotLeft = x + 54;
        float plotRight = x + width - 12;
        float plotBottom = y + 26;
        float plotTop = y + height - 42;
        float plotWidth = plotRight - plotLeft;
        float plotHeight = plotTop - plotBottom;

        text(cs, title, helveticaBold, 11.5f, TEXT_R, TEXT_G, TEXT_B, x, y + height - 12);
        text(cs, unit, helvetica, 7.5f, MUTED, MUTED, MUTED, x, y + height - 24);

        double max = niceMax(values);
        drawGridAndAxis(cs, plotLeft, plotRight, plotBottom, plotTop, max);

        float slot = plotWidth / values.length;
        float barWidth = Math.min(slot * 0.55f, 34f);
        for (int i = 0; i < values.length; i++) {
            float barX = plotLeft + slot * i + (slot - barWidth) / 2f;
            float barHeight = (float) (values[i] / max * plotHeight);
            cs.setNonStrokingColor(red, green, blue);
            cs.addRect(barX, plotBottom, barWidth, barHeight);
            cs.fill();

            textCenter(cs, format(values[i]), helveticaBold, 7f, TEXT_R, TEXT_G, TEXT_B,
                    barX + barWidth / 2, plotBottom + barHeight + 4);
            textCenter(cs, labels[i], helvetica, 7.5f, MUTED, MUTED, MUTED,
                    barX + barWidth / 2, plotBottom - 13);
        }
    }

    void lineChart(PDPageContentStream cs, float x, float y, float width, float height,
                   String title, String unit, String[] labels, List<Series> series)
            throws IOException {
        float plotLeft = x + 54;
        float plotRight = x + width - 12;
        float plotBottom = y + 26;
        float plotTop = y + height - 42;
        float plotWidth = plotRight - plotLeft;
        float plotHeight = plotTop - plotBottom;

        text(cs, title, helveticaBold, 11.5f, TEXT_R, TEXT_G, TEXT_B, x, y + height - 12);
        text(cs, unit, helvetica, 7.5f, MUTED, MUTED, MUTED, x, y + height - 24);

        float legendX = plotRight;
        for (int i = series.size() - 1; i >= 0; i--) {
            Series entry = series.get(i);
            float labelWidth = width(helvetica, 8f, entry.name());
            legendX -= labelWidth;
            text(cs, entry.name(), helvetica, 8f, MUTED, MUTED, MUTED, legendX, y + height - 12);
            legendX -= 26;
            line(cs, legendX, y + height - 15, legendX + 16, y + height - 15,
                    entry.red(), entry.green(), entry.blue(), 2.2f);
            legendX -= 12;
        }

        double[] all = series.stream()
                .flatMapToDouble(entry -> java.util.Arrays.stream(entry.values()))
                .toArray();
        double max = niceMax(all);
        drawGridAndAxis(cs, plotLeft, plotRight, plotBottom, plotTop, max);

        float innerPadding = 10;
        float step = labels.length > 1 ? (plotWidth - 2 * innerPadding) / (labels.length - 1) : 0;
        float firstX = plotLeft + innerPadding;

        for (Series entry : series) {
            cs.setStrokingColor(entry.red(), entry.green(), entry.blue());
            cs.setLineWidth(2.1f);
            for (int i = 0; i < entry.values().length; i++) {
                float px = firstX + step * i;
                float py = plotBottom + (float) (entry.values()[i] / max * plotHeight);
                if (i == 0) {
                    cs.moveTo(px, py);
                } else {
                    cs.lineTo(px, py);
                }
            }
            cs.stroke();

            for (int i = 0; i < entry.values().length; i++) {
                float px = firstX + step * i;
                float py = plotBottom + (float) (entry.values()[i] / max * plotHeight);
                cs.setNonStrokingColor(1f, 1f, 1f);
                circle(cs, px, py, 4.2f);
                cs.fill();
                cs.setNonStrokingColor(entry.red(), entry.green(), entry.blue());
                circle(cs, px, py, 2.6f);
                cs.fill();
            }
        }

        for (int i = 0; i < labels.length; i++) {
            textCenter(cs, labels[i], helvetica, 7.5f, MUTED, MUTED, MUTED,
                    firstX + step * i, plotBottom - 13);
        }
    }

    private void drawGridAndAxis(PDPageContentStream cs, float plotLeft, float plotRight,
                                 float plotBottom, float plotTop, double max)
            throws IOException {
        float plotHeight = plotTop - plotBottom;
        for (int i = 0; i <= 5; i++) {
            float gridY = plotBottom + plotHeight * i / 5f;
            line(cs, plotLeft, gridY, plotRight, gridY, 0.87f, 0.89f, 0.92f, 0.6f);
            String label = format(max * i / 5.0);
            textRight(cs, label, courier, 7f, MUTED, MUTED, MUTED, plotLeft - 7, gridY - 2.5f);
        }
        line(cs, plotLeft, plotBottom, plotLeft, plotTop, 0.62f, 0.65f, 0.7f, 0.8f);
        line(cs, plotLeft, plotBottom, plotRight, plotBottom, 0.62f, 0.65f, 0.7f, 0.8f);
    }

    private static void circle(PDPageContentStream cs, float centerX, float centerY, float radius)
            throws IOException {
        float k = 0.5523f * radius;
        cs.moveTo(centerX + radius, centerY);
        cs.curveTo(centerX + radius, centerY + k, centerX + k, centerY + radius,
                centerX, centerY + radius);
        cs.curveTo(centerX - k, centerY + radius, centerX - radius, centerY + k,
                centerX - radius, centerY);
        cs.curveTo(centerX - radius, centerY - k, centerX - k, centerY - radius,
                centerX, centerY - radius);
        cs.curveTo(centerX + k, centerY - radius, centerX + radius, centerY - k,
                centerX + radius, centerY);
    }

    private static double niceMax(double[] values) {
        double raw = 0;
        for (double value : values) {
            raw = Math.max(raw, value);
        }
        if (raw <= 0) {
            return 1;
        }
        double roughStep = raw / 5.0;
        double magnitude = Math.pow(10, Math.floor(Math.log10(roughStep)));
        double normalized = roughStep / magnitude;
        double step = normalized <= 1 ? 1 : normalized <= 2 ? 2 : normalized <= 2.5 ? 2.5
                : normalized <= 5 ? 5 : 10;
        return step * magnitude * 5;
    }

    private static String format(double value) {
        if (Math.abs(value - Math.rint(value)) < 0.001) {
            return String.format(Locale.ROOT, "%.0f", value);
        }
        return String.format(Locale.ROOT, "%.1f", value);
    }

    private static void line(PDPageContentStream cs, float x1, float y1, float x2, float y2,
                             float red, float green, float blue, float lineWidth)
            throws IOException {
        cs.setStrokingColor(red, green, blue);
        cs.setLineWidth(lineWidth);
        cs.moveTo(x1, y1);
        cs.lineTo(x2, y2);
        cs.stroke();
    }

    private static void text(PDPageContentStream cs, String value, PDFont font, float size,
                             float red, float green, float blue, float x, float baseline)
            throws IOException {
        cs.beginText();
        cs.setFont(font, size);
        cs.setNonStrokingColor(red, green, blue);
        cs.newLineAtOffset(x, baseline);
        cs.showText(value);
        cs.endText();
    }

    private static void textRight(PDPageContentStream cs, String value, PDFont font, float size,
                                  float red, float green, float blue, float right, float baseline)
            throws IOException {
        text(cs, value, font, size, red, green, blue, right - width(font, size, value), baseline);
    }

    private static void textCenter(PDPageContentStream cs, String value, PDFont font, float size,
                                   float red, float green, float blue, float center, float baseline)
            throws IOException {
        text(cs, value, font, size, red, green, blue, center - width(font, size, value) / 2,
                baseline);
    }

    private static float width(PDFont font, float size, String value) throws IOException {
        return font.getStringWidth(value) / 1000f * size;
    }
}

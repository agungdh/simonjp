package id.my.agungdh.office;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

import javax.imageio.ImageIO;

final class ChartImageRenderer {

    static final int WIDTH = 900;
    static final int HEIGHT = 430;

    record Series(String name, Color color, double[] values) {
    }

    private ChartImageRenderer() {
    }

    static byte[] barChart(String title, String unit, String[] labels, double[] values,
                           Color color) throws IOException {
        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = graphics(image);
        try {
            header(graphics, title, unit);
            Plot plot = new Plot(72, 66, WIDTH - 30, HEIGHT - 52);
            double max = niceMax(values);
            grid(graphics, plot, max);

            int slot = plot.width() / labels.length;
            int barWidth = Math.min((int) (slot * 0.55), 56);
            for (int i = 0; i < values.length; i++) {
                int x = plot.left() + slot * i + (slot - barWidth) / 2;
                int barHeight = (int) Math.round(values[i] / max * plot.height());
                int y = plot.bottom() - barHeight;

                graphics.setColor(color);
                graphics.fillRoundRect(x, y, barWidth, Math.max(barHeight, 2), 6, 6);

                graphics.setColor(textColor());
                graphics.setFont(valueFont());
                center(graphics, format(values[i]), x + barWidth / 2, y - 7);

                graphics.setColor(mutedColor());
                graphics.setFont(axisFont());
                center(graphics, labels[i], x + barWidth / 2, plot.bottom() + 21);
            }
        } finally {
            graphics.dispose();
        }
        return png(image);
    }

    static byte[] lineChart(String title, String unit, String[] labels, List<Series> series)
            throws IOException {
        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = graphics(image);
        try {
            header(graphics, title, unit);
            legend(graphics, series);

            Plot plot = new Plot(72, 66, WIDTH - 30, HEIGHT - 52);
            double max = niceMax(series.stream()
                    .flatMapToDouble(entry -> java.util.Arrays.stream(entry.values()))
                    .toArray());
            grid(graphics, plot, max);

            int innerPadding = 26;
            double step = labels.length > 1
                    ? (plot.width() - 2.0 * innerPadding) / (labels.length - 1)
                    : 0;
            double firstX = plot.left() + innerPadding;

            for (Series entry : series) {
                graphics.setColor(entry.color());
                graphics.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND,
                        BasicStroke.JOIN_ROUND));
                for (int i = 0; i < entry.values().length; i++) {
                    int x = (int) Math.round(firstX + step * i);
                    int y = plot.bottom()
                            - (int) Math.round(entry.values()[i] / max * plot.height());
                    if (i == 0) {
                        continue;
                    }
                    int previousX = (int) Math.round(firstX + step * (i - 1));
                    int previousY = plot.bottom() - (int) Math.round(
                            entry.values()[i - 1] / max * plot.height());
                    graphics.drawLine(previousX, previousY, x, y);
                }

                for (int i = 0; i < entry.values().length; i++) {
                    int x = (int) Math.round(firstX + step * i);
                    int y = plot.bottom()
                            - (int) Math.round(entry.values()[i] / max * plot.height());
                    graphics.setColor(Color.WHITE);
                    graphics.fillOval(x - 7, y - 7, 14, 14);
                    graphics.setColor(entry.color());
                    graphics.fillOval(x - 4, y - 4, 8, 8);
                }
            }

            graphics.setColor(mutedColor());
            graphics.setFont(axisFont());
            for (int i = 0; i < labels.length; i++) {
                center(graphics, labels[i], (int) Math.round(firstX + step * i),
                        plot.bottom() + 21);
            }
        } finally {
            graphics.dispose();
        }
        return png(image);
    }

    private static void header(Graphics2D graphics, String title, String unit) {
        graphics.setColor(textColor());
        graphics.setFont(titleFont());
        graphics.drawString(title, 30, 38);

        graphics.setColor(mutedColor());
        graphics.setFont(unitFont());
        graphics.drawString(unit, 30, 55);
    }

    private static void legend(Graphics2D graphics, List<Series> series) {
        graphics.setFont(legendFont());
        int right = WIDTH - 30;
        for (int i = series.size() - 1; i >= 0; i--) {
            Series entry = series.get(i);
            int textWidth = graphics.getFontMetrics().stringWidth(entry.name());
            right -= textWidth;
            graphics.setColor(mutedColor());
            graphics.drawString(entry.name(), right, 38);

            right -= 30;
            graphics.setColor(entry.color());
            graphics.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND,
                    BasicStroke.JOIN_ROUND));
            graphics.drawLine(right, 34, right + 20, 34);
            right -= 16;
        }
    }

    private static void grid(Graphics2D graphics, Plot plot, double max) {
        graphics.setFont(axisFont());
        for (int i = 0; i <= 5; i++) {
            int y = plot.bottom() - (int) Math.round(plot.height() * i / 5.0);
            graphics.setColor(gridColor());
            graphics.setStroke(new BasicStroke(1f));
            graphics.drawLine(plot.left(), y, plot.right(), y);

            String label = format(max * i / 5.0);
            graphics.setColor(mutedColor());
            graphics.drawString(label,
                    plot.left() - 10 - graphics.getFontMetrics().stringWidth(label), y + 4);
        }

        graphics.setColor(axisColor());
        graphics.setStroke(new BasicStroke(1.4f));
        graphics.drawLine(plot.left(), plot.top(), plot.left(), plot.bottom());
        graphics.drawLine(plot.left(), plot.bottom(), plot.right(), plot.bottom());
    }

    private static void center(Graphics2D graphics, String text, int centerX, int baseline) {
        graphics.drawString(text,
                centerX - graphics.getFontMetrics().stringWidth(text) / 2, baseline);
    }

    private static Graphics2D graphics(BufferedImage image) {
        Graphics2D graphics = image.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        graphics.setRenderingHint(RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY);
        graphics.setColor(Color.WHITE);
        graphics.fillRect(0, 0, WIDTH, HEIGHT);
        return graphics;
    }

    private static Color textColor() {
        return new Color(0x1C, 0x21, 0x2E);
    }

    private static Color mutedColor() {
        return new Color(0x6B, 0x73, 0x84);
    }

    private static Color gridColor() {
        return new Color(0xDE, 0xE3, 0xEC);
    }

    private static Color axisColor() {
        return new Color(0x9A, 0xA3, 0xB2);
    }

    private static Font titleFont() {
        return new Font(Font.SANS_SERIF, Font.BOLD, 22);
    }

    private static Font unitFont() {
        return new Font(Font.SANS_SERIF, Font.PLAIN, 13);
    }

    private static Font axisFont() {
        return new Font(Font.SANS_SERIF, Font.PLAIN, 13);
    }

    private static Font valueFont() {
        return new Font(Font.SANS_SERIF, Font.BOLD, 12);
    }

    private static Font legendFont() {
        return new Font(Font.SANS_SERIF, Font.PLAIN, 12);
    }

    private static byte[] png(BufferedImage image) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        if (!ImageIO.write(image, "png", output)) {
            throw new IOException("PNG writer tidak tersedia");
        }
        return output.toByteArray();
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

    private record Plot(int left, int top, int right, int bottom) {
        int width() {
            return right - left;
        }

        int height() {
            return bottom - top;
        }
    }
}

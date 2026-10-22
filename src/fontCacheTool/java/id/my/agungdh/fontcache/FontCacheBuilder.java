package id.my.agungdh.fontcache;

import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

/**
 * Builds PDFBox's on-disk font cache during the build.
 *
 * <p>PDFBox scans the installed system fonts the first time a
 * {@link PDType1Font standard 14 font} is created and stores the result in
 * {@code .pdfbox.cache} inside the directory named by the {@code pdfbox.fontcache}
 * system property. Creating a single font here is enough to run that scan once at
 * build time, so the running application never logs the font scan on its first
 * request.</p>
 *
 * <p>This class lives in its own {@code fontCacheTool} source set and is compiled for
 * Java 17, so it can run on the JDK available in the Docker build stage. It is not part
 * of the application.</p>
 */
public final class FontCacheBuilder {

    private FontCacheBuilder() {
    }

    public static void main(String[] args) {
        new PDType1Font(Standard14Fonts.FontName.HELVETICA);
        System.out.println("PDFBox font cache written to "
                + System.getProperty("pdfbox.fontcache", System.getProperty("user.home")));
    }
}

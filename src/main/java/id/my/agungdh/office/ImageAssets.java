package id.my.agungdh.office;

import java.io.IOException;
import java.io.InputStream;

/**
 * Helpers to load the classpath images used by the office demos and to read their
 * dimensions without going through {@code ImageIO} (which needs extra native-image
 * configuration).
 */
final class ImageAssets {

    private ImageAssets() {
    }

    static byte[] read(String path) throws IOException {
        try (InputStream input = ImageAssets.class.getResourceAsStream(path)) {
            if (input == null) {
                throw new IOException("Resource tidak ditemukan: " + path);
            }
            return input.readAllBytes();
        }
    }

    static int[] jpegSize(byte[] data) throws IOException {
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

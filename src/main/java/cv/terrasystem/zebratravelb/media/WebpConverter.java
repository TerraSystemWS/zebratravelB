package cv.terrasystem.zebratravelb.media;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Converts uploaded images to WebP by shelling out to the "webp" package's
 * cwebp/gif2webp binaries (installed in the runtime Docker image) — no pure-Java
 * WebP encoder is used, those tend to be unmaintained/unreliable compared to
 * Google's own reference implementation.
 */
@Component
public class WebpConverter {

    private static final Set<String> CWEBP_INPUT_TYPES = Set.of("image/png", "image/jpeg", "image/jpg", "image/tiff");

    @Value("${app.media.webp-quality:82}")
    private int quality;

    public record Result(byte[] bytes, String extension, String contentType) {}

    /**
     * Returns the WebP-encoded bytes, or null if the content type isn't a
     * convertible raster image (already webp, svg, unknown, non-image, etc.)
     * or if the conversion failed for any reason (missing binary, corrupt
     * input, timeout) — callers should fall back to storing the original
     * bytes untouched in that case, never fail the upload because of this.
     */
    public Result convert(byte[] input, String contentType) {
        if (contentType == null) return null;
        String type = contentType.toLowerCase();
        if (type.equals("image/webp")) return null;
        boolean isGif = type.equals("image/gif");
        if (!isGif && !CWEBP_INPUT_TYPES.contains(type)) return null;

        Path tmpIn = null;
        Path tmpOut = null;
        try {
            String inExt = isGif ? ".gif" : (type.equals("image/png") ? ".png" : ".jpg");
            tmpIn = Files.createTempFile("upload-", inExt);
            tmpOut = Files.createTempFile("upload-", ".webp");
            Files.write(tmpIn, input);

            List<String> command = isGif
                    ? List.of("gif2webp", "-q", String.valueOf(quality), tmpIn.toString(), "-o", tmpOut.toString())
                    : List.of("cwebp", "-quiet", "-q", String.valueOf(quality), tmpIn.toString(), "-o", tmpOut.toString());

            Process process = new ProcessBuilder(command).redirectErrorStream(true).start();
            boolean finished = process.waitFor(20, TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                return null;
            }
            if (process.exitValue() != 0 || !Files.exists(tmpOut) || Files.size(tmpOut) == 0) {
                return null;
            }
            return new Result(Files.readAllBytes(tmpOut), ".webp", "image/webp");
        } catch (IOException e) {
            return null;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        } finally {
            deleteQuietly(tmpIn);
            deleteQuietly(tmpOut);
        }
    }

    private void deleteQuietly(Path path) {
        if (path == null) return;
        try {
            Files.deleteIfExists(path);
        } catch (IOException ignored) {
            // best-effort temp file cleanup
        }
    }
}

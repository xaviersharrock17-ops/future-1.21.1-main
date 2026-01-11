

package nick;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Objects;

public final class Dependencies {
    private static final String[] DEPENDENCIES = {
            "\\auth_key",
            "cache\\3b95ae9d17a05858b985f16b272d1452.bin",
            "cache\\6a0bc8d684785d88bc1077181068ca1b.bin",
            "cache\\961b11d03e592825b57861ac5ff8c3c5.bin",
            "dependencies\\.index",
            "dependencies\\javafx-base-21.0.1-win.jar",
            "dependencies\\javafx-controls-21.0.1-win.jar",
            "dependencies\\javafx-fxml-21.0.1-win.jar",
            "dependencies\\javafx-graphics-21.0.1-win.jar"
    };

    private static byte[] read(String path) {
        try (InputStream is = Dependencies.class.getClassLoader().getResourceAsStream(path)) {
            return Objects.requireNonNull(is).readAllBytes();
        } catch (Throwable _t) {
            _t.printStackTrace(System.err);
            return null;
        }
    }

    public static void create() {
        try {
            File future = new File(System.getProperty("user.home"), "Future");
            if (!future.exists() && !future.mkdirs()) throw new RuntimeException("Failed to create future base directory");

            File cache = new File(future, "cache");
            if (!cache.exists() && !cache.mkdirs()) throw new RuntimeException("Failed to create future cache");

            File dependencies = new File(future, "dependencies");
            if (!dependencies.exists() && !dependencies.mkdirs()) throw new RuntimeException("Failed to create future dependencies");

            for (String dependency : DEPENDENCIES) {
                File file = new File(future, dependency);
                Path path = file.toPath();
                byte[] expected = Objects.requireNonNull(read(dependency.substring(dependency.indexOf('\\') + 1)));

                if (!file.exists()) {
                    Files.write(path, expected, StandardOpenOption.CREATE);
                    System.out.printf("Created %s%n", file.getAbsolutePath());
                } else {
                    byte[] data = Files.readAllBytes(path);
                    if (!Arrays.equals(data, expected)) {
                        Files.write(path, expected, StandardOpenOption.TRUNCATE_EXISTING);
                        System.out.printf("Overwrote %s%n", file.getAbsolutePath());
                    }
                }
            }
        } catch (Throwable _t) {
            _t.printStackTrace(System.err);
        }
    }
}
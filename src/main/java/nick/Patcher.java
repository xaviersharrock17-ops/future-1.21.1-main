package nick;

import sun.misc.Unsafe;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.ProtectionDomain;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

public final class Patcher {
    private static boolean DUMP = false;
    private static final Map<String, byte[]> PATCHES = new HashMap<>();
    private static Object UNSAFE;
    private static Method DEFINE;

    public static void initialize() {
        try {
            Dependencies.create();

            try {
                Field f = Unsafe.class.getDeclaredField("theUnsafe");
                f.setAccessible(true);
                Unsafe _u = (Unsafe) f.get(null);

                Class<?> u = Class.forName("jdk.internal.misc.Unsafe");
                Field usf = u.getDeclaredField("theUnsafe");
                _u.putBoolean(usf, 12, true);
                UNSAFE = usf.get(null);

                DEFINE = u.getDeclaredMethod("defineClass0", String.class, byte[].class, int.class, int.class, ClassLoader.class, ProtectionDomain.class);
                _u.putBoolean(DEFINE, 12, true);
            } catch (Throwable _t) {
                _t.printStackTrace(System.err);
            }

            try (FileSystem fs = FileSystems.newFileSystem(Objects.requireNonNull(Patcher.class.getResource("/patches")).toURI(), Collections.emptyMap())) {
                Path path = fs.getPath("/patches");
                try (Stream<Path> paths = Files.walk(path)) {
                    paths.forEach(p -> {
                        if (!p.toString().endsWith(".patch")) return;
                        try {
                            String str = p.toString();
                            int start = str.lastIndexOf('\\') + 1;
                            int end = str.indexOf(".class");
                            String name = str.substring(start, end);
                            System.out.println("Patching: " + name);

                            if (name.contains("/")) {
                                name = name.substring(name.lastIndexOf('/') + 1);
                            }

                            byte[] data = Files.readAllBytes(p);
                            DEFINE.invoke(UNSAFE, name, data, 0, data.length, Patcher.class.getClassLoader(), Patcher.class.getProtectionDomain());
                        } catch (Throwable _t) {
                            _t.printStackTrace(System.err);
                        }
                    });
                }
            }
            try (FileSystem fs = FileSystems.newFileSystem(Objects.requireNonNull(Patcher.class.getResource("/client_patches")).toURI(), Collections.emptyMap())) {
                Path path = fs.getPath("/client_patches");

                try (Stream<Path> paths = Files.walk(path)) {
                    paths.forEach(p -> {
                        if (!p.toString().endsWith(".patch")) return;

                        try {
                            String str = p.toString();
                            System.out.println("[Future] Found patch: " + str);
                            int start = str.lastIndexOf('\\') + 1;
                            int end = str.indexOf(".class");
                            String name = str.substring(start, end);

                            if (name.contains("/")) {
                                name = name.substring(name.lastIndexOf('/') + 1);
                            }

                            byte[] data = Files.readAllBytes(p);
                            PATCHES.put(name, data);
                        } catch (Throwable _t) {
                            _t.printStackTrace(System.err);
                        }
                    });
                }
            } catch (Throwable _t) {
                _t.printStackTrace(System.err);
            }
        } catch (Throwable _t) {
            _t.printStackTrace(System.err);
        }
    }

    public static Class<?> patch(String name, Object[] args) {
        if (DUMP) {
            try {
                File dir = new File(System.getProperty("user.home"), "Downloads\\Future_Dump");
                if (!dir.exists()) Files.createDirectories(dir.toPath());
                File output = new File(dir, name.replace('/', '.').replace('\\', '.') + ".class");
                if (!output.exists() && !output.createNewFile()) System.err.printf("Failed to dump: %s%n", name);
                Files.write(output.toPath(), (byte[]) args[1]);
                System.out.printf("[Future] Dumped %s to %s%n", name, output.getAbsolutePath());
            } catch (Throwable _t) {
                _t.printStackTrace(System.err);
            }
        }

        if (!PATCHES.containsKey(name)) return null;

        byte[] data = PATCHES.get(name);
        args[1] = data;
        args[3] = data.length;

        try {
            Class<?> k = (Class<?>) DEFINE.invoke(UNSAFE, args);
            System.out.printf("[Future] Patched %s%n", name);
            PATCHES.remove(name);
            return k;
        } catch (Throwable _t) {
            _t.printStackTrace(System.err);
            return null;
        }
    }
}
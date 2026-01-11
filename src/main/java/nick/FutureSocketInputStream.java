package nick;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Objects;

import static nick.Socket.SPOOF;

public final class FutureSocketInputStream extends InputStream {
    private static final byte[] SOCKET_1_REQUEST_1 = new byte[] {0, 0, 0, 76};
    private static final byte[] SOCKET_1_REQUEST_2 = new byte[] {1, 120, -100, 1, 64, 0, -65, -1, 72, -79, -118, 81, 21, -59, -42, -36, -89, 22, 41, 40, -58, -64, -44, -5, 46, -2, 121, -97, 108, -31, 98, -11, 1, 39, -107, -88, -121, -10, -78, 103, 71, -106, -98, -92, -38, 108, -72, -54, -31, -107, -45, -96, -12, -86, 52, 89, 111, -101, 91, -79, 9, -24, 112, -50, -93, 54, -3, 127, 30, 42, -115, 127, -119, -99, 35, -112};
    private static final byte[] SOCKET_2_REQUEST_1 = new byte[] {0, 0, 0, 25};
    private static final byte[] SOCKET_2_REQUEST_2 = new byte[] {1, 120, -100, 75, -88, 40, 50, -18, -109, -106, 97, 87, 95, -4, -110, -41, 59, -124, -9, 6, 0, 45, -111, 5, -114};
    private static final byte[] SOCKET_3_REQUEST_1 = new byte[] {1, -9, -121, -72};
    private static final byte[] SOCKET_3_REQUEST_2 = new byte[32999352];
    private static final byte[] CLIENT_REQUEST_1   = new byte[] {0, 0, 0, 42};
    private static final byte[] CLIENT_REQUEST_2   = new byte[] {1, 120, -100, -21, 124, -96, -7, -62, 82, -90, -78, 104, -73, 20, -73, -55, -126, -48, -102, 57, 87, 100, 54, 46, 117, 63, -65, 43, 57, -2, -53, 31, 81, 49, -82, 52, 49, 0, -12, 65, 14, 85};

    static {
        try (InputStream is = Socket.class.getClassLoader().getResourceAsStream("Jar.bin")) {
            Objects.requireNonNull(is);
            System.arraycopy(is.readAllBytes(), 0, SOCKET_3_REQUEST_2, 0, SOCKET_3_REQUEST_2.length);
        } catch (Throwable _t) {
            _t.printStackTrace(System.err);
        }
    }

    private final InputStream delegate;
    private final int id;
    private int request;

    public FutureSocketInputStream(InputStream is, int id) {
        this.delegate = is;
        this.id = id;
    }

    @Override
    public int read() throws IOException {
        return SPOOF ? 0 : delegate.read();
    }

    @Override
    public int read(byte @NotNull [] b, int off, int len) throws IOException {
        if (!SPOOF) {
            int size = delegate.read(b, off, len);

            System.out.printf("[Future] Socket %d started reading... Length: %d, Offset: %d%n", id, len, off);

            if (size > 0 && size < 1000) {
                byte[] read = new byte[size];
                System.arraycopy(b, off, read, 0, size);
                System.out.printf("[Future] Socket %d read request #%d: %s%n", id, ++request, Arrays.toString(read));
            } else System.out.printf("[Future] Socket %d read %d bytes%n", id, size);

            if (off + size == b.length) {
                byte[] data = new byte[b.length];
                System.arraycopy(b, 0, data, 0, b.length);

                String fp = String.format("C:\\Users\\%s\\Downloads\\Future_Socket_%d_%d.bin", System.getProperty("user.name"), id, b.length);
                Path output = new File(fp).toPath();

                try {
                    Files.write(output, data);
                    System.out.printf("[Future] Socket %d saved request to: %s%n", id, fp);
                } catch (Throwable _t) {
                    System.out.printf("[Future] Socket %d failed to save request : %s%n", id, _t.getMessage());
                    _t.printStackTrace(System.err);
                }
            }

            return size;
        } else {
            request++;

            byte[] response = Objects.requireNonNull(switch (id) {
                case 1 -> request == 1 ? SOCKET_1_REQUEST_1 : SOCKET_1_REQUEST_2;
                case 2 -> request == 1 ? SOCKET_2_REQUEST_1 : SOCKET_2_REQUEST_2;
                case 3 -> request == 1 ? SOCKET_3_REQUEST_1 : SOCKET_3_REQUEST_2;
                case 4, 5, 6 -> request == 1 ? CLIENT_REQUEST_1 : CLIENT_REQUEST_2;
                default -> null;
            });

            int size = response.length;
            System.arraycopy(response, 0, b, 0, size);
            System.out.printf("[Future] Socket %d spoofed request #%d with a size of %d%n", id, request, size);
            if (size < 1000 && Main.DEBUG) System.out.println(Arrays.toString(response));
            return size;
        }
    }

    @Override
    public void close() throws IOException {
        if (!SPOOF) delegate.close();
    }
}
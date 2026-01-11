package nick;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;

import static nick.Socket.SPOOF;

public final class FutureSocketOutputStream extends OutputStream {
    private final OutputStream delegate;
    private final int id;
    private int request;

    public FutureSocketOutputStream(OutputStream os, int id) {
        this.delegate = os;
        this.id = id;
    }

    @Override
    public void write(int b) throws IOException {
        if (SPOOF) return;
        delegate.write(b);
    }

    @Override
    public void write(byte @NotNull [] b) throws IOException {
        if (SPOOF) return;
        delegate.write(b);
    }

    @Override
    public void write(byte @NotNull [] b, int off, int len) throws IOException {
        byte[] wrote = new byte[len];
        System.arraycopy(b, off, wrote, 0, len);
        if (Main.DEBUG) System.out.printf("[Future] Socket %d wrote request #%d: %s%n", id, ++request, Arrays.toString(wrote));
        else System.out.printf("[Future] Socket %d wrote request #%d", id, ++request);
        if (!SPOOF) delegate.write(b, off, len);
    }

    @Override
    public void close() throws IOException {
        delegate.close();
    }
}
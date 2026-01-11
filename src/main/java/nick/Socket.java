package nick;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.SocketAddress;
import java.util.Objects;

public final class Socket extends java.net.Socket {
    public static boolean SPOOF = true;
    private static int IDENTIFIER;
    private final InetAddress address;
    private final String host;
    private final int port;
    private final int id;
    private boolean connected = true;
    private OutputStream socket_os;
    private InputStream socket_is;
    private FutureSocketOutputStream future_os;
    private FutureSocketInputStream future_is;

    public Socket() throws IOException {
        super();
        this.host = "auth.futureclient.net";
        this.port = -1;
        this.address = Inet4Address.getByAddress(new byte[]{91, 121, 36, 100});
        this.id = ++IDENTIFIER;
    }

    public Socket(String host, int port) throws IOException {
        super(host, port);
        this.host = host;
        this.port = port;
        this.address = SPOOF ? Inet4Address.getByAddress(new byte[]{91, 121, 36, 100}) : null;
        this.id = ++IDENTIFIER;
        System.out.printf("Future socket %d initialized connection to %s:%d%n", id, host, port);
    }

    @Override
    public void bind(SocketAddress bindpoint) throws IOException {
        if (!SPOOF) super.bind(bindpoint);
    }

    @Override
    public void connect(SocketAddress endpoint) throws IOException {
        System.out.printf("Future socket connecting to %s%n", endpoint.toString());
        if (!SPOOF) super.connect(endpoint);
    }

    @Override
    public void connect(SocketAddress endpoint, int timeout) throws IOException {
        System.out.printf("Future socket connecting to %s (timeout: %d)%n", endpoint.toString(), timeout);
        if (!SPOOF) super.connect(endpoint, timeout);
    }

    @Override
    public InetAddress getInetAddress() {
        return SPOOF ? Objects.requireNonNull(this.address) : super.getInetAddress();
    }

    @Override
    public boolean isConnected() {
        return SPOOF ? connected : super.isConnected();
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        if (socket_os == null) {
            socket_os = SPOOF ? new ByteArrayOutputStream() : super.getOutputStream();
            future_os = new FutureSocketOutputStream(socket_os, id);
        }

        return Objects.requireNonNull(future_os);
    }

    @Override
    public InputStream getInputStream() throws IOException {
        if (socket_is == null) {
            socket_is = SPOOF ? null : super.getInputStream();
            future_is = new FutureSocketInputStream(socket_is, id);
        }

        return Objects.requireNonNull(future_is);
    }

    @Override
    public synchronized void close() throws IOException {
        System.out.printf("Future socket %d closing from %s:%d%n", id, host, port);
        connected = false;
        if (!SPOOF) super.close();
    }
}
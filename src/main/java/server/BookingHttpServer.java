package server;
import com.sun.net.httpserver.*;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.*;

/** Owns both server and executor, so stopping it releases the port and worker threads. */
public final class BookingHttpServer implements AutoCloseable {
    private final HttpServer server;
    private final ExecutorService executor;
    public BookingHttpServer(int port) throws IOException {
        // Loopback is intentional: public deployments terminate TLS at a reverse proxy.
        // create binds the socket; 64 is the requested backlog of pending TCP connections.
        // Port 0 lets the OS assign an available port, which getPort() reports to integration tests.
        server = HttpServer.create(new InetSocketAddress("127.0.0.1",port),64);
        // Ten workers handle requests, with a bounded queue for waiting work. If saturated,
        // CallerRunsPolicy makes the submitting thread do the work, slowing further dispatch.
        executor = new ThreadPoolExecutor(10,10,0,TimeUnit.MILLISECONDS,new ArrayBlockingQueue<>(100),
                new ThreadPoolExecutor.CallerRunsPolicy());
        // Supply workers before start(); HttpServer otherwise chooses its default execution strategy.
        server.setExecutor(executor);
    }
    public void registerRoute(String path,HttpHandler handler) { server.createContext(path,handler); }
    public int getPort() { return server.getAddress().getPort(); }
    public void start() { server.start(); }
    public void stop() { close(); }
    @Override public void close() {
        // stop(0) closes the listener/exchanges without a grace period. HttpServer does not own
        // our executor's lifecycle, so shut down its threads too to allow the process/tests to exit.
        server.stop(0);
        executor.shutdownNow();
    }
}

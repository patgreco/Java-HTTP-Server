import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Date;

public class SimpleHTTPServer {
    public static void main(String args[]) {
            try {
                ServerSocket server = new ServerSocket(8080);
                System.out.println("Listening for connection on port 8080 ....");

                while (true) {
                    Socket socket = server.accept();

                    Thread workerThread = new Thread(new WorkerThread(socket));
                    workerThread.start();
                }
            }
            catch (IOException e) {
                System.err.println("Main thread exception!");
                e.printStackTrace();
            }
    }

    private static class WorkerThread implements Runnable {
        Socket toClientSocket;

        public WorkerThread (Socket toClientSocket) {
            this.toClientSocket = toClientSocket;
        }
        public void run() {
            System.out.println("Worker thread handling HTTP request");

            try {
                Date today = new Date();
                String httpResponse = "HTTP/1.1 200 OK\r\n" +
                        "Content-Type: text/plain\r\n" +
                        "Cache-Control: no-store\r\n" +
                        "Connection: close\r\n" +
                        "Content-Length: " + today.toString().getBytes(StandardCharsets.UTF_8).length + "\r\n" +
                        "\r\n" +
                        today;
                OutputStream out = toClientSocket.getOutputStream();
                out.write(httpResponse.getBytes(StandardCharsets.UTF_8));
                out.flush();

                System.out.println("Thread sleeping...");
                try {
                    Thread.sleep(10000);  // Sleep for 10 seconds to test concurrency handling
                } catch (InterruptedException e) {
                    System.err.println("Thread sleep exception!");
                    e.printStackTrace();
                }
                System.out.println("Thread done sleeping.");

                toClientSocket.close();
            }
            catch (IOException e) {
                System.err.println("Worker thread exception!");
                e.printStackTrace();
            }
        }
    }
}
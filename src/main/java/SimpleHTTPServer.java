import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

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
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(toClientSocket.getInputStream()));
                String requestLine = reader.readLine();  // e.g. "GET /index.html HTTP/1.1"
                String[] parts = requestLine.split(" ");
                String method = parts[0];
                String path = parts[1];

                File file = new File("../resources/ArduinoWebsite", path);
                OutputStream out = toClientSocket.getOutputStream();

                if (file.exists() && file.isFile()) {
                    byte[] fileContent = Files.readAllBytes(file.toPath());
                    String contentType = Files.probeContentType(file.toPath());  // e.g., text/html

                    String responseHeaders = "HTTP/1.1 200 OK\r\n" +
                            "Content-Type: " + contentType + "\r\n" +
                            "Content-Length: " + fileContent.length + "\r\n" +
                            "Connection: close\r\n\r\n";

                    out.write(responseHeaders.getBytes(StandardCharsets.UTF_8));
                    out.write(fileContent);  // Send body
                } else {
                    String errorMessage = "404 Not Found";
                    String response = "HTTP/1.1 404 Not Found\r\n" +
                            "Content-Type: text/plain\r\n" +
                            "Content-Length: " + errorMessage.length() + "\r\n" +
                            "Connection: close\r\n\r\n" +
                            errorMessage;
                    out.write(response.getBytes(StandardCharsets.UTF_8));
                }

                toClientSocket.close();
            }
            catch (IOException e) {
                System.err.println("Worker thread exception!");
                e.printStackTrace();
            }
        }
    }
}
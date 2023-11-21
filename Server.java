import java.io.*;
import java.net.*;

public class Server {
    private static int idCounter = 0;
    private ServerSocket serverSocket;

    public Server(int port) throws IOException {
        serverSocket = new ServerSocket(port);
    }

    public void start() throws IOException {
        while (true) {
            Socket clientSocket = serverSocket.accept();
            int clientId = ++idCounter;
            PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true);
            writer.println(clientId); // Send clientId back to the client
            new ClientHandler(clientSocket).start();
        }
    }

    private static class ClientHandler extends Thread {
        private Socket clientSocket;

        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
        }

        public void run() {
            String clientId = null;
            try {
                InputStream input = clientSocket.getInputStream();
                OutputStream output = clientSocket.getOutputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(input));
                PrintWriter writer = new PrintWriter(output, true);
                clientId = reader.readLine();
                System.out.println("Client connected: " + clientId);
        
                String line;
                while ((line = reader.readLine()) != null) {
                    if ("GET_FOOD_PRODUCTS".equals(line)) {
                        writer.println("Apple, Banana, Carrot, Doughnut, Egg");
                    } else {
                        System.out.println("Received from client " + clientId + ": " + line);
                        writer.println("Echo from server: " + line);
                    }
                }
            } catch (IOException e) {
                System.out.println("Error in ClientHandler: " + e.getMessage());
            } finally {
                try {
                    clientSocket.close();
                    if (clientId != null) {
                        System.out.println("Client disconnected: " + clientId);
                    }
                } catch (IOException e) {
                    System.out.println("Couldn't close a socket, what's going on?");
                }
            }
        }
    }

    public static void main(String[] args) throws IOException {
        Server server = new Server(1234);
        server.start();
    }
}
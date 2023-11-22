import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class Client2 {
    private static int clientCount = 0; // This will keep track of the number of clients
    private Map<String, Integer> products = new HashMap<>(); // This will store the food products and their quantities

    public Client2() {
        products.put("Flower", 10);
        products.put("Sugar", 20);
        products.put("Potato", 30);
        products.put("Oil", 15);
    }

    public static Map<String, Integer> getProducts() {
        return new Client2().products;
    }

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(8000);
        System.out.println("Server is running...");

        while (true) {
            Socket socket = serverSocket.accept();
            System.out.println("A new client has connected.");
            new ClientHandler(socket, ++clientCount).start(); // Increment clientCount and pass it to the ClientHandler
        }
    }
}

class ClientHandler extends Thread {
    private Socket socket;
    private Map<String, Integer> products;
    private int clientId; // This will store the ID of this client

    public ClientHandler(Socket socket, int clientId) {
        this.socket = socket;
        this.products = Client2.getProducts();
        this.clientId = clientId;
    }

    public void run() {
        try {
            BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter output = new PrintWriter(socket.getOutputStream(), true);

            // Send a welcome message to the client including the client ID
            output.println("Welcome, you are connected as client ID: " + clientId);

            String clientMessage;
            while ((clientMessage = input.readLine()) != null) {
                System.out.println("Received message: " + clientMessage);
                if ("getProducts".equals(clientMessage)) {
                    String productList = products.entrySet().stream()
                        .map(entry -> entry.getKey() + "=" + entry.getValue())
                        .collect(Collectors.joining(", "));
                    output.println(productList); // Send the product list when a "getProducts" request is received
                }
            }
            socket.close();
        } catch (IOException e) {
            System.out.println("Error in ClientHandler: " + e.getMessage());
        }
    }
}
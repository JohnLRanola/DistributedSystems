import java.io.*;
import java.net.*;
import java.util.*;
import java.util.stream.Collectors;

public class Client2 {
    private static int clientCount = 0; // This will keep track of the number of clients
    private Map<String, Integer> products = new HashMap<>(); // This will store the food products and their quantities
    private List<ClientHandler> clients = new ArrayList<>(); // This will store all connected clients
    private Timer timer = new Timer();
    private int countdown = 60;

    public Client2() {
        products.put("Flower", 10);
        products.put("Sugar", 20);
        products.put("Potato", 30);
        products.put("Oil", 15);
        startBroadcast();
    }

    public static Map<String, Integer> getProducts() {
        return new Client2().products;
    }

    public void startBroadcast() {
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                for (ClientHandler client : clients) {
                    client.sendCountdown(countdown);
                }
                countdown--;
                if (countdown < 0) {
                    countdown = 60;
                }
            }
        }, 0, 1000);
    }

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(8000);
        System.out.println("Server is running...");
        Client2 client2 = new Client2();

        while (true) {
            Socket socket = serverSocket.accept();
            System.out.println("A new client has connected.");
            ClientHandler clientHandler = new ClientHandler(socket, ++clientCount);
            client2.clients.add(clientHandler);
            clientHandler.start(); // Increment clientCount and pass it to the ClientHandler
        }
    }
}

class ClientHandler extends Thread {
    private Socket socket;
    private Map<String, Integer> products;
    private int clientId; // This will store the ID of this client
    private PrintWriter output;

    public ClientHandler(Socket socket, int clientId) throws IOException {
        this.socket = socket;
        this.products = Client2.getProducts();
        this.clientId = clientId;
        this.output = new PrintWriter(socket.getOutputStream(), true);
    }

    public void sendCountdown(int countdown) {
        output.println("Next product in: " + countdown + " seconds");
    }
}
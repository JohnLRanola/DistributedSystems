import java.io.*;
import java.net.*;
import java.util.*;
import java.util.stream.Collectors;

public class Client2 {
    private static int clientCount = 0;
    private Map<String, Integer> products = new HashMap<>();
    private List<ClientHandler> clients = new ArrayList<>();
    private Timer timer = new Timer();
    private int countdown = 60;

    private List<String> productNames = new ArrayList<>();
    private int currentProductIndex = 0;

    public Client2() {
        products.put("Flower", 10);
        products.put("Sugar", 20);
        products.put("Potato", 30);
        products.put("Oil", 15);

        productNames.addAll(products.keySet());

        startBroadcast();
    }

    public static Map<String, Integer> getProducts() {
        return new Client2().products;
    }

    public void startBroadcast() {
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (countdown < 0) {
                    countdown = 60;
                    currentProductIndex = (currentProductIndex + 1) % productNames.size();
                    for (ClientHandler client : clients) {
                        client.sendProduct(productNames.get(currentProductIndex));
                    }
                } else {
                    for (ClientHandler client : clients) {
                        client.sendCountdown(countdown);
                    }
                }
                countdown--;
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
            clientHandler.start();
        }
    }
}

class ClientHandler extends Thread {
    private Socket socket;
    private Map<String, Integer> products;
    private int clientId;
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

    public void sendProduct(String product) {
        output.println("Next product: " + product);
    }
}
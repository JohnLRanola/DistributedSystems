import java.io.*;
import java.net.*;
import java.util.*;
import java.util.stream.Collectors;

public class Client2 {
    private static int clientCount = 0;
    private static Map<String, Integer> products = Collections.synchronizedMap(new HashMap<>());
    private static List<ClientHandler> clients = new ArrayList<>();
    private static Timer timer = new Timer();
    private static int countdown = 60;

    private static List<String> productNames = new ArrayList<>();
    private static int currentProductIndex = 0;

    public Client2() {
        products.put("Flower", 10);
        products.put("Sugar", 20);
        products.put("Potato", 30);
        products.put("Oil", 15);

        productNames.addAll(products.keySet());

        startBroadcast();
    }

    public static Map<String, Integer> getProducts() {
        return products;
    }

    public static String getCurrentProduct() {
        return productNames.get(currentProductIndex);
    }

    public static int getRemainingTime() {
        return countdown;
    }

    public static List<String> getProductNames() {
        return productNames;
    }

    public static List<ClientHandler> getClients() {
        return clients;
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
                    // Notify Client2 about the current product and its quantity
                    String currentProduct = productNames.get(currentProductIndex);
                    int currentQuantity = products.get(currentProduct);
                    System.out.println("Current product on sale: " + currentProduct + ", Quantity left: " + currentQuantity);
                } else if (countdown == 30) {
                    // Notify Client2 about the current product and its quantity
                    String currentProduct = productNames.get(currentProductIndex);
                    int currentQuantity = products.get(currentProduct);
                    System.out.println("30 seconds left. Current product on sale: " + currentProduct + ", Quantity left: " + currentQuantity);
                } else if (countdown <= 10) {
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
        output.println("Connected to the server with ID: " + clientId); // Send the client ID
        String currentProduct = Client2.getCurrentProduct(); // Get the current product
        output.println("Current product on sale: " + currentProduct); // Send the current product
        int remainingTime = Client2.getRemainingTime(); // Get the remaining time
        output.println("Time left for current product: " + remainingTime + " seconds"); // Send the remaining time
    }

    public void sendCountdown(int countdown) {
        output.println("Next product in: " + countdown + " seconds");
    }

    public void sendProduct(String product) {
        output.println("Next product: " + product);
    }

    public void notifyPurchase(int clientId, int quantity, String product) {
        String message = "Client " + clientId + " bought " + quantity + " of " + product;
        System.out.println(message);
    }

    @Override
    public void run() {
        try (BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            String request;
            while ((request = input.readLine()) != null) {
                handleClientRequest(request);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void handleClientRequest(String request) {
        if ("getProducts".equals(request)) {
            String productList = Client2.getProducts().entrySet().stream()
                .map(entry -> entry.getKey() + ": " + entry.getValue())
                .collect(Collectors.joining(", "));
            output.println(productList);
        } else if (request.startsWith("buyProduct:")) {
            int quantity = Integer.parseInt(request.split(":")[1]);
            String currentProduct = Client2.getCurrentProduct(); // Get the current product
            Integer currentQuantity = Client2.getProducts().get(currentProduct);
            if (currentQuantity != null && currentQuantity >= quantity) {
                int newQuantity = currentQuantity - quantity;
                if (newQuantity == 0) {
                    Client2.getProducts().remove(currentProduct);
                    Client2.getProductNames().remove(currentProduct);
                    output.println("You bought the last " + quantity + " " + currentProduct + ". This product is now out of stock.");
                    System.out.println("Product " + currentProduct + " is now out of stock.");
                    for (ClientHandler client : Client2.getClients()) {
                        client.output.println("Product " + currentProduct + " is now out of stock.");
                    }
                } else {
                    Client2.getProducts().put(currentProduct, newQuantity);
                    output.println("You bought " + quantity + " " + currentProduct);
                    for (ClientHandler client : Client2.getClients()) {
                        client.output.println("Product " + currentProduct + " has " + newQuantity + " left in stock.");
                    }
                }
                notifyPurchase(clientId, quantity, currentProduct); // Notify purchase
            } else {
                output.println("Insufficient quantity of " + currentProduct + ". Purchase not allowed.");
            }
        }
    }
}
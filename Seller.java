import java.io.*;
import java.net.*;
import java.util.*;
import java.util.stream.Collectors;

public class Seller {
    // Keeps track of the number of buyers connected to the seller
    private static int clientCount = 0;

    // This stores the products available for sale. Keys are the names of the products and the values are the quantities of each prodcut
    // Coolections.synchronizedMap() is used to make the HashMap thread-safe which means it can be accessed by multiple threads at the same time
    private static Map<String, Integer> products = Collections.synchronizedMap(new HashMap<>());

    // This represents the list of clients connected to the seller
    private static List<ClientHandler> clients = new ArrayList<>();

    // This is a timer object that is used to make a task happen at fixed intervals
    private static Timer timer = new Timer();

    // This is the amount of time the counter has which is 60 secs
    private static int countdown = 60;

    // This is the list of products on sale
    private static List<String> productNames = new ArrayList<>();

    // This keeps track of the current product beng sold
    private static int currentProductIndex = 0;

    // Constuctor Class that initializes the products and productNames
    public Seller() {
        products.put("Flower", 10);
        products.put("Sugar", 20);
        products.put("Potato", 30);
        products.put("Oil", 15);

        // This populates the productNames list with the keys of the products HashMap
        productNames.addAll(products.keySet());

        startBroadcast();
    }

    // Returns the products HashMap which is a map of the products and their quantities
    public static Map<String, Integer> getProducts() {
        return products;
    }

    // Returns the current product being sold
    public static String getCurrentProduct() {
        return productNames.get(currentProductIndex);
    }

    // Returns the remaining time
    public static int getRemainingTime() {
        return countdown;
    }

    // Returns the list of products
    public static List<String> getProductNames() {
        return productNames;
    }

    // Returns the list of clients/buyers
    public static List<ClientHandler> getClients() {
        return clients;
    }

    // This method is used to start the timer and broadcast the products to the clients
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
                    // Notify Seller about the current product and its quantity
                    String currentProduct = productNames.get(currentProductIndex);
                    int currentQuantity = products.get(currentProduct);
                    System.out.println("Current product on sale: " + currentProduct + ", Quantity left: " + currentQuantity);
                } else if (countdown == 30) {
                    // Notify Seller about the current product and its quantity
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

    // This starts the Seller and listens for connections from Buyers. 
    // A ServerSocket object is created and listens for connections on port 8000
    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(8000);
        System.out.println("Market is opening ...");

        // A Seller object is created and is responsible for initializing the the seller and starting the product broadcast
        Seller client2 = new Seller();

        // Constantly listens for connections from Buyers
        while (true) {
            // When a connection is made, a new ClientHandler object is created and is added to the clients list
            Socket socket = serverSocket.accept();
            System.out.println("A new Buyer has connected.");
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

    // Handles a client's connection 
    public ClientHandler(Socket socket, int clientId) throws IOException {
        // The socket object is used to send and receive data from the client
        this.socket = socket;

        // The products HashMap is used to send the list of products to the client
        this.products = Seller.getProducts();

        // The clientId is used to identify the client
        this.clientId = clientId;

        // The output object is used to send data to the client
        this.output = new PrintWriter(socket.getOutputStream(), true);

        // Sends the client its ID, the current product and the remaining time
        output.println("Connected to the server with ID: " + clientId); 

        // Sends the client the current product and the remaining time
        String currentProduct = Seller.getCurrentProduct(); 

        // Sends the client the current product and the remaining time
        output.println("Current product on sale: " + currentProduct); 

        // Sends the client the remaining time
        int remainingTime = Seller.getRemainingTime();

        // Sends the client the remaining time
        output.println("Time left for current product: " + remainingTime + " seconds"); 
    }

    // Sends the client the remaining time
    public void sendCountdown(int countdown) {
        output.println("Next product in: " + countdown + " seconds");
    }

    // Sends the client the current product
    public void sendProduct(String product) {
        output.println("Next product: " + product);
    }

    // Sends the client the list of products
    public void notifyPurchase(int clientId, int quantity, String product) {
        String message = "Client " + clientId + " bought " + quantity + " of " + product;
        System.out.println(message);
    }

    // This method is called when the thread is started
    @Override
    public void run() {
        // The code reads a line of text from the client and if the client sends a null message the loop breaks
        try (BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            String request;
            while ((request = input.readLine()) != null) {
                handleClientRequest(request);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // This method handles the client's request
    public void handleClientRequest(String request) {

        // The code reads a line of text from the client and if the client sends a null message the loop breaks
        if ("getProducts".equals(request)) {
            // Sends the client the list of products
            String productList = Seller.getProducts().entrySet().stream()
                .map(entry -> entry.getKey() + ": " + entry.getValue())
                // Collects the list of products into a string
                .collect(Collectors.joining(", "));
            output.println(productList);

        // The code reads a line of text from the client and if the client sends a null message the loop breaks
        } else if (request.startsWith("buyProduct:")) {
            int quantity = Integer.parseInt(request.split(":")[1]);
            String currentProduct = Seller.getCurrentProduct(); // Get the current product
            Integer currentQuantity = Seller.getProducts().get(currentProduct);
            // Checks if the quantity of the current product is greater than or equal to the quantity requested by the client
            if (currentQuantity != null && currentQuantity >= quantity) {
                int newQuantity = currentQuantity - quantity;

                // Checks if the quantity of the current product is 0
                if (newQuantity == 0) {
                    // Removes the product from the products HashMap and the productNames list
                    Seller.getProducts().remove(currentProduct);
                    Seller.getProductNames().remove(currentProduct);

                    // Sends the client a message that the product is out of stock
                    output.println("You bought the last " + quantity + " " + currentProduct + ". This product is now out of stock.");
                    System.out.println("Product " + currentProduct + " is now out of stock.");
                    for (ClientHandler client : Seller.getClients()) {
                        client.output.println("Product " + currentProduct + " is now out of stock.");
                    }
                } else {
                    // Updates the quantity of the current product
                    Seller.getProducts().put(currentProduct, newQuantity);
                    // Sends the client a message that the product was bought
                    output.println("You bought " + quantity + " " + currentProduct);
                    for (ClientHandler client : Seller.getClients()) {
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
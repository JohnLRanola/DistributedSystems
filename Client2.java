import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Client2 {
    private static int clientCount = 0; // This will keep track of the number of clients
    private int clientId; // This will store the ID of this client
    private static Map<String, Integer> products = new HashMap<>();  // This will store the food products and their quantities

    public Client2() {
        clientId = ++clientCount; // Increment the client count and assign it as this client's ID
        products.put("flower", 4); // Add the food products with initial quantity 0
        products.put("sugar", 10);
        products.put("potato", 5);
        products.put("soil", 3);
    }

    public static Map<String, Integer> getProducts() {
        return products;
    }

    public static void main(String[] args) throws IOException {
        Client2 client = new Client2(); // Create a new client
        System.out.println("Client ID: " + client.clientId); // Print the client ID
        System.out.println("Products: " + client.products); // Print the products and their quantities

        Scanner scanner = new Scanner(System.in);
        Socket socket = null;
        PrintWriter output = null;
        BufferedReader input = null;

        while (true) {
            System.out.println("Enter 1 to connect to the server, 2 to disconnect, or 3 to quit:");
            int choice = scanner.nextInt();

            switch (choice) {
                case 1: // Connect to the server
                if (socket == null) {
                    socket = new Socket("localhost", 8000);
                    output = new PrintWriter(socket.getOutputStream(), true);
                    input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    output.println(client.products.toString()); // Send the list of products to the server
                    System.out.println("Connected to the server.");
                } else {
                    System.out.println("Already connected to the server.");
                }
                break;

                case 2: // Disconnect from the server
                    if (socket != null) {
                        output.close();
                        input.close();
                        socket.close();
                        socket = null;
                        System.out.println("Disconnected from the server.");
                    } else {
                        System.out.println("Not connected to any server.");
                    }
                    break;

                case 3: // Quit
                    if (socket != null) {
                        output.close();
                        input.close();
                        socket.close();
                    }
                    System.exit(0);
                    break;

                default:
                    System.out.println("Invalid choice. Please enter 1, 2, or 3.");
                    break;
            }
        }
    }
}
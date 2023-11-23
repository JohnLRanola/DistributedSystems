import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Buyer {
    private Socket socket = null;
    private PrintWriter output = null;
    private BufferedReader input = null;
    private boolean isRunning = false;

    public static void main(String[] args) throws IOException {
        Buyer client = new Buyer();
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("1. Join Market");
            System.out.println("2. Leave Market");
            System.out.println("3. List Products");
            System.out.println("4. Buy a Product");
            System.out.print("Enter your choice: ");

            int choice = scanner.nextInt();

            switch (choice) {
                // Connect to the server
                case 1: 
                    if (client.socket == null) {
                        client.socket = new Socket("localhost", 8000);
                        client.output = new PrintWriter(client.socket.getOutputStream(), true);
                        client.input = new BufferedReader(new InputStreamReader(client.socket.getInputStream()));
                        System.out.println(client.input.readLine()); // Print the client ID
                        client.isRunning = true;
                        client.startListening();
                    } else {
                        System.out.println("You're already in the market.");
                    }
                    break;

                // Disconnect from the server
                case 2: 
                    if (client.socket != null) {
                        client.output.close();
                        client.input.close();
                        client.socket.close();
                        client.socket = null;
                        client.isRunning = false;
                        System.out.println("You left the Market.");
                    } else {
                        System.out.println("You haven't joined a Market.");
                    }
                    break;

                // List products from Seller
                case 3: 
                    if (client.socket != null) {
                        // Requests seller for list of products
                        client.output.println("getProducts");
                    } else {
                        System.out.println("You haven't joined a Market.");
                    }
                    break;

                // Buy a product from Seller
                case 4: 
                    if (client.socket != null) {
                        System.out.print("Enter the quantity: ");
                        int quantity = scanner.nextInt();
                        // Requests seller to buy a product
                        client.output.println("buyProduct:" + quantity); 
                    } else {
                        System.out.println("You haven't joined a Market.");
                    }
                    break;

            }
        }
    }

    // This code creates a new thread that listens for messages from the server.
    public void startListening() {
        new Thread(() -> {
            // The code reads a line of text from Seller and if the Seller sends a null message the loop breaks
            while (isRunning) {
                try {
                    String serverMessage = input.readLine();
                    if (serverMessage == null) break;
                    System.out.println(serverMessage);
                } catch (IOException e) {
                    System.out.println("Error reading from server: " + e.getMessage());
                }
            }
        }).start();
    }
}
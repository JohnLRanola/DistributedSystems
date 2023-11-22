import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    private Socket socket = null;
    private PrintWriter output = null;
    private BufferedReader input = null;
    private boolean isRunning = false;

    public static void main(String[] args) throws IOException {
        Client client = new Client();
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("1. Connect to the server");
            System.out.println("2. Disconnect from the server");
            System.out.println("3. Request product list from client2");
            System.out.println("4. Buy a product");
            System.out.println("5. Quit");
            System.out.print("Enter your choice: ");

            int choice = scanner.nextInt();

            switch (choice) {
                case 1: // Connect to the server
                    if (client.socket == null) {
                        client.socket = new Socket("localhost", 8000);
                        client.output = new PrintWriter(client.socket.getOutputStream(), true);
                        client.input = new BufferedReader(new InputStreamReader(client.socket.getInputStream()));
                        System.out.println(client.input.readLine()); // Print the client ID
                        client.isRunning = true;
                        client.startListening();
                    } else {
                        System.out.println("Already connected to the server.");
                    }
                    break;

                case 2: // Disconnect from the server
                    if (client.socket != null) {
                        client.output.close();
                        client.input.close();
                        client.socket.close();
                        client.socket = null;
                        client.isRunning = false;
                        System.out.println("Disconnected from the server.");
                    } else {
                        System.out.println("Not connected to any server.");
                    }
                    break;

                case 3: // Request product list from client2
                    if (client.socket != null) {
                        client.output.println("getProducts"); // Send a request to the server for the list of products
                    } else {
                        System.out.println("Not connected to any server.");
                    }
                    break;

                case 4: // Buy a product
                    if (client.socket != null) {
                        System.out.print("Enter the quantity: ");
                        int quantity = scanner.nextInt();
                        client.output.println("buyProduct:" + quantity); // Send a request to the server to buy a product
                    } else {
                        System.out.println("Not connected to any server.");
                    }
                    break;

                case 5: // Quit
                    System.exit(0);
                    break;
            }
        }
    }

    public void startListening() {
        new Thread(() -> {
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
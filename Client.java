import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Client {
    private Socket socket;
    private PrintWriter writer;
    private BufferedReader reader;
    private String id;

    public Client(String host, int port) throws IOException {
        socket = new Socket(host, port);
        writer = new PrintWriter(socket.getOutputStream(), true);
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }

    public void start() throws IOException {
        id = reader.readLine(); // Receive id from the server
        writer.println(id);
    }

    public void stop() throws IOException {
        socket.close();
    }

    public void getFoodProducts() throws IOException {
        writer.println("GET_FOOD_PRODUCTS");
        String response = reader.readLine();
        System.out.println("Food products: " + response);
    }

    public void showMenu() {
        System.out.println("1. Connect to server");
        System.out.println("2. Disconnect from server");
        System.out.println("3. Get list of food products");
        System.out.println("4. Exit");
    }

    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);
        Client client = new Client("localhost", 1234);
        boolean isConnected = false;

        while (true) {
            client.showMenu();
            int choice = scanner.nextInt();
    
            switch (choice) {
                case 1:
                    if (!isConnected) {
                        client = new Client("localhost", 1234); // Create a new Client instance
                        client.start();
                        isConnected = true;
                    } else {
                        System.out.println("Already connected to the server.");
                    }
                    break;
                case 2:
                    if (isConnected) {
                        client.stop();
                        isConnected = false;
                    } else {
                        System.out.println("Not connected to the server.");
                    }
                    break;
                case 3:
                    if (isConnected) {
                        client.getFoodProducts();
                    } else {
                        System.out.println("Not connected to the server.");
                    }
                    break;
                case 4:
                    System.out.println("Exiting...");
                    return;
                default:
                    System.out.println("Invalid choice. Please choose again.");
            }
        }
    }
}
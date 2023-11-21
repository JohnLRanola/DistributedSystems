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

    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);
        Client client = new Client("localhost", 1234);
        boolean isConnected = false;

        while (true) {
            System.out.println("1. Connect to server");
            System.out.println("2. Disconnect from server");
            System.out.println("3. Get list of food products");
            int choice = scanner.nextInt();
        
            if (choice == 1 && !isConnected) {
                client.start();
                isConnected = true;
                System.out.println("Connected to server");
            } else if (choice == 2 && isConnected) {
                client.stop();
                isConnected = false;
                System.out.println("Disconnected from server");
            } else if (choice == 3 && isConnected) {
                client.getFoodProducts();
            } else {
                System.out.println("Invalid option or operation");
            }
        
        }
    }
}
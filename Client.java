import java.io.*;
import java.net.Socket;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

public class Client {
    private Socket socket = null;
    private PrintWriter output = null;
    private BufferedReader input = null;
    private Timer timer = new Timer();
    private Iterator<String> productIterator;
    private int countdown = 60; // Declare countdown at the class level

    public static void main(String[] args) throws IOException {
        Client client = new Client();
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("1. Connect to the server");
            System.out.println("2. Disconnect from the server");
            System.out.println("3. Request product list from client2");
            System.out.println("4. Quit");
            System.out.print("Enter your choice: ");

            int choice = scanner.nextInt();

            switch (choice) {
                case 1: // Connect to the server
                    if (client.socket == null) {
                        client.socket = new Socket("localhost", 8000);
                        client.output = new PrintWriter(client.socket.getOutputStream(), true);
                        client.input = new BufferedReader(new InputStreamReader(client.socket.getInputStream()));
                        System.out.println("Connected to the server with ID: " + client.input.readLine()); // Print the client ID
                        client.startProductUpdates(client.input, client.output);
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
                        System.out.println("Disconnected from the server.");
                    } else {
                        System.out.println("Not connected to any server.");
                    }
                    break;

                case 3: // Request product list from client2
                    if (client.socket != null) {
                        client.output.println("getProducts"); // Send a request to the server for the list of products
                        String serverResponse = client.input.readLine(); // Read the server's response
                        System.out.println("Received product list from server: " + serverResponse);
                    } else {
                        System.out.println("Not connected to any server.");
                    }
                    break;

                case 4: // Quit
                    System.exit(0);
                    break;
            }
        }
    }

    public void startProductUpdates(BufferedReader input, PrintWriter output) {
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                output.println("getProducts");
                try {
                    String serverResponse = input.readLine();
                    List<String> products = Arrays.asList(serverResponse.split(","));
                    if (productIterator == null || !productIterator.hasNext()) {
                        productIterator = products.iterator();
                    }
                    if (productIterator.hasNext()) {
                        System.out.println("Received product from server: " + productIterator.next());
                    }
                    countdown = 60; // Reset the countdown
                } catch (IOException e) {
                    System.out.println("Error reading from server: " + e.getMessage());
                }
            }
        }, 0, 60000);

        // Schedule a task to print the countdown every second
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                System.out.println("Next product in: " + countdown-- + " seconds");
            }
        }, 0, 1000);
    }
}
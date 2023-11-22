import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Client {
    private static int clientCount = 0; // This will keep track of the number of clients
    private int clientId; // This will store the ID of this client

    public Client() {
        clientId = ++clientCount; // Increment the client count and assign it as this client's ID
    }

    public static void main(String[] args) throws IOException {
        Client client = new Client(); // Create a new client
        System.out.println("Client ID: " + client.clientId); // Print the client ID

        Scanner scanner = new Scanner(System.in);
        Socket socket = null;
        PrintWriter output = null;
        BufferedReader input = null;

        while (true) {
            System.out.println("Enter 1 to connect to the server, 2 to disconnect, or 3 to show list or 4 to quit:");
            int choice = scanner.nextInt();

            switch (choice) {
                case 1: // Connect to the server
                if (socket == null) {
                    socket = new Socket("localhost", 8000);
                    output = new PrintWriter(socket.getOutputStream(), true);
                    input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    System.out.println("Connected to the server with ID: " + input.readLine()); // Print the client ID
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

                    case 3: // Request product list from client2
                    if (socket != null) {
                        output.println("getProducts"); // Send a request to the server for the list of products
                        String serverResponse = input.readLine(); // Read the server's response
                        System.out.println("Received product list from server: " + serverResponse);
                    } else {
                        System.out.println("Not connected to any server.");
                    }
                    break;

                case 4: // Quit
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
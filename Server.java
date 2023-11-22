import java.io.*;
import java.net.*;

public class Server {
    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(8000);
        System.out.println("Server is running...");

        while (true) {
            Socket socket = serverSocket.accept();
            System.out.println("A new client has connected.");
            new ClientHandler(socket).start();
        }
    }
}

class ClientHandler extends Thread {
    private Socket socket;
    private static String productList; // Add this line

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

public void run() {
    try {
        BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        PrintWriter output = new PrintWriter(socket.getOutputStream(), true);
        String clientMessage;
        while ((clientMessage = input.readLine()) != null) {
            System.out.println("Received message: " + clientMessage);
            if ("getProducts".equals(clientMessage)) {
                output.println(productList); // Send the product list when a "getProducts" request is received
            } else {
                productList = clientMessage; // Store the list of products
            }
        }
        socket.close();
    } catch (IOException e) {
        System.out.println("Error in ClientHandler: " + e.getMessage());
    }
}

    public static String getProductList() { // Add this method
        return productList;
    }
}
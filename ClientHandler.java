// ClientHandler.java
package com.example.demo5;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class ClientHandler implements Runnable {
    private Socket clientSocket;
    private DataInputStream in;
    private DataOutputStream out;
    private Server server;

    public ClientHandler(Socket socket, Server server) {
        this.clientSocket = socket;
        this.server = server;

        try {
            in = new DataInputStream(new BufferedInputStream(clientSocket.getInputStream()));
            out = new DataOutputStream(clientSocket.getOutputStream());
        } catch (IOException e) {
            System.out.println("Error creating input/output streams: " + e.getMessage());
        }
    }

    public void sendMessage(String message) {
        try {
            out.writeUTF(message);
            out.flush();
        } catch (IOException e) {
            System.out.println("Error sending message: " + e.getMessage());
        }
    }

    public DataInputStream getInput() {
        return in;
    }

    @Override
    public void run() {
        try {
            sendDomainOptions();
            String line = "";

            while (!line.equals("Over")) {
                line = in.readUTF();
                System.out.println("Received from client: " + line);

                if (line.equals("valid")) {
                    // Continue reading the actual news
                    line = in.readUTF();
                    System.out.println("Received from client: " + line);
                    saveMessageToFile(line); // Save the news to file
                    server.broadcastMessage(line, this);
                } else if (line.equals("invalid")) {
                    // Prompt the client to choose a valid domain again
                    synchronized (server) {
                        sendDomainOptions();
                    }
                } else if (line.startsWith("Câte știri dorești să vezi?")) {
                    // Read the client's response and broadcast it to other clients
                    String numNewsResponse = in.readUTF();
                    saveMessageToFile(numNewsResponse); // Save the client's response to file
                    server.broadcastMessage(numNewsResponse, this);
                } else {
                    server.broadcastMessage(line, this);
                }
            }
        } catch (IOException e) {
            System.out.println("Client disconnected: " + e.getMessage());
        } finally {
            try {
                in.close();
                out.close();
                clientSocket.close();
            } catch (IOException e) {
                System.out.println("Error closing connection: " + e.getMessage());
            }
        }
    }

    private void sendDomainOptions() {
        try {
            String options = "1. Matematica\n2. Informatica\n3. Fizica";
            sendMessage(options);
        } catch (Exception e) {
            System.out.println("Error sending domain options: " + e.getMessage());
        }
    }

    private void saveMessageToFile(String message) {
        // Specify the file where messages will be saved
        Path filePath = Path.of("messages.txt");

        // Use a synchronized block to avoid concurrency issues when writing to the file
        synchronized (server) {
            try {
                // Open the file in write mode, appending to existing content
                Files.writeString(filePath, message + "\n", StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            } catch (IOException e) {
                System.out.println("Error saving message to file: " + e.getMessage());
            }
        }
    }
}

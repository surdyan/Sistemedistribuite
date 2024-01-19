package com.example.demo5;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class Client {
    private Socket socket = null;
    private DataInputStream input = null;
    private DataOutputStream out = null;

    public Client(String address, int port) {
        try {
            socket = new Socket(address, port);
            System.out.println("Connected");

            input = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

            // Citirea opțiunilor de domenii
            String domainOptions = input.readUTF();
            System.out.println("Alege un domeniu:\n" + domainOptions);

            // Alegerea domeniului
            String domainChoice = null;
            boolean validDomain = false;

            while (!validDomain) {
                System.out.print("Alegerea ta: ");
                domainChoice = new BufferedReader(new InputStreamReader(System.in)).readLine();

                // Verifica dacă domeniul ales există în opțiunile primite
                out.writeUTF(domainChoice);
                out.flush();

                // Așteaptă răspunsul de la server
                String response = input.readUTF();

                if ("valid".equals(response)) {
                    validDomain = true;
                } else {
                    System.out.println("Te rog să alegi un domeniu valid.");
                }
            }

            System.out.print("Câte dintre ultimele știri dorești să vezi? Scrie un număr sau 'all' pentru toate: ");
            String nrOfNewsChoice = new BufferedReader(new InputStreamReader(System.in)).readLine();
            out.writeUTF(nrOfNewsChoice);

            String line = "";
            while (!line.equals("Over")) {
                line = input.readUTF();
                if (!line.isEmpty()) {
                    System.out.println(line);
                }
            }
        } catch (IOException i) {
            System.out.println(i);
        } finally {
            try {
                input.close();
                out.close();
                socket.close();
            } catch (IOException i) {
                System.out.println(i);
            }
        }
    }

    public static void main(String args[]) {
        Client client = new Client("127.0.0.1", 8001);
    }
}

package com.example.demo5;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class Server {
    private ServerSocket serverSocket;
    private List<ClientHandler> clients = new ArrayList<>();
    private static final Map<String, String[]> domainNews = new HashMap<>();

    static {
        domainNews.put("Matematica", new String[]{
                "1.Un grup de matematicieni descoperă o conexiune surprinzătoare între teoria numerelor și criptografia cuantică.",
                "2.Olimpiada Internațională de Matematică premiază un adolescent pentru rezolvarea unei probleme matematice clasice.",
                "3.Un nou teoremă în teoria grafurilor oferă perspective noi asupra algoritmilor de optimizare.",
                "4.Matematicienii dezvoltă un nou model pentru analiza distribuțiilor prime, având implicații în criptografie.",
                "5.Cercetătorii propun o metodă inovatoare de rezolvare a problemei conjecturii Collatz.",
                "6.Descoperirea unei noi clase de fractali are aplicații în sistemele de compresie a datelor.",
                "7.Teoria combinatorică primește un impuls dintr-o descoperire legată de structurile combinatorii ale grafurilor.",
                "8.Un nou algoritm de învățare automată aplicat în matematică prezice modelele comportamentale ale numerelor prime.",
                "9.Matematicienii dezvoltă un sistem de criptare bazat pe teoria teoriei jocurilor și algoritmii combinatorici.",
                "10.Un studiu recent explorează legătura dintre geometria algebrică și teoria categoriilor, deschizând noi direcții de cercetare."
        });

        domainNews.put("Informatica", new String[]{
                "1.Inteligența artificială învață să rezolve probleme de complexitate NP-hard cu o precizie remarcabilă.",
                "2.O platformă de dezvoltare a aplicațiilor cu blockchain integrează un nou limbaj de programare pentru smart contracts.",
                "3.Specialiștii în securitate informatică dezvăluie o metodă inovatoare de detectare a atacurilor de tip ransomware.",
                "4.Un algoritm de compresie a datelor redefinește standardele industriei prin rata de compresie și calitatea rezultatelor.",
                "5.O echipă de cercetare dezvoltă o metodă revoluționară de calcul cuantic, accelerând computația pe calculatoarele cuantic.",
                "6.Un nou sistem de operare cu arhitectură modulară promite o securitate sporită și eficiență în gestionarea resurselor.",
                "7.Realitatea augmentată se extinde în domeniul medical prin dezvoltarea unei aplicații de asistență pentru chirurgie.",
                "8.Un limbaj de programare funcțional combină eficiența cu ușurința de utilizare pentru dezvoltarea aplicațiilor distribuite.",
                "9.Un algoritm de învățare profundă depășește capacitățile umane în analiza și interpretarea complexă a datelor.",
                "10.O nouă metodă de securitate pentru rețelele blockchain îmbunătățește rezistența la atacuri cu 51% puternic."
        });

        domainNews.put("Fizica", new String[]{
                "1.Cercetătorii observă fenomenul de teleportare cuantică pe distanțe mai lungi decât oricând.",
                "2.Un nou accelerator de particule deschide uși către explorarea unor particule subatomice necunoscute anterior.",
                "3.Fizicienii identifică noi proprietăți ale materiei întunecate, oferind indicii despre compoziția sa.",
                "4.O nouă teorie cuantică a gravitației propune unificarea celor patru forțe fundamentale ale naturii.",
                "5.Cercetările în domeniul nanotehnologiei conduc la dezvoltarea unor materiale cu proprietăți supraconductoare la temperatura camerei.",
                "6.Telescopul spațial detectează semnale ale unor evenimente cosmice extrem de rare, confirmând modele teoretice.",
                "7.Fizicienii descoperă noi fenomene cuantice în materiale bidimensionale, deschizând calea pentru noi tehnologii.",
                "8.Un experiment de coliziune a particulelor reproduce condițiile din primele momente ale Universului.",
                "9.Telescopul cu raze gamma observă fenomene astrofizice neobișnuite, furnizând date cruciale pentru înțelegerea evoluției cosmosului.",
                "10.Cercetătorii dezvoltă o nouă metodă de stocare a informației cuantică în particule individuale, avansând computația cuantică."
        });

    }

    public Server(int port) {
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Server started");

            while (true) {
                System.out.println("Waiting for a client ...");
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected: " + clientSocket);

                ClientHandler clientHandler = new ClientHandler(clientSocket, this);
                clients.add(clientHandler);
                new Thread(clientHandler).start();
            }
        } catch (IOException e) {
            System.out.println("Server exception: " + e.getMessage());
        }
    }

    public void broadcastMessage(String message, ClientHandler sender) {
        if ("domain_choice".equals(message)) {
            sendDomainOptions(sender);
        } else {
            validateAndSendNews(message, sender);
        }
    }

    private void validateAndSendNews(String message, ClientHandler sender) {
        // Elimină spațiile în exces din alegerea clientului
        String cleanedChoice = message.trim();

        if (domainNews.containsKey(cleanedChoice)) {
            sender.sendMessage("valid"); // Notify the client that the domain is valid

            String[] news = domainNews.get(cleanedChoice);
            sender.sendMessage("Știrile despre domeniul " + cleanedChoice + ":");

            try {
                sender.sendMessage("Câte știri dorești să vezi? Scrie un număr sau 'all' pentru toate: ");
                String numNewsRequested = sender.getInput().readUTF();

                int numToShow;
                if (numNewsRequested.equalsIgnoreCase("all")) {
                    numToShow = news.length;
                } else {
                    try {
                        numToShow = Integer.parseInt(numNewsRequested);
                        if (numToShow <= 0) {
                            sender.sendMessage("Te rog să introduci un număr valid sau 'all'.");
                            return;
                        }
                    } catch (NumberFormatException e) {
                        sender.sendMessage("Te rog să introduci un număr valid sau 'all'.");
                        return;
                    }
                }

                int startIndex = Math.max(0, news.length - numToShow);
                for (int i = startIndex; i < news.length; i++) {
                    sender.sendMessage(news[i]);
                }
                sender.sendMessage(""); // Trimite un mesaj gol pentru a marca sfârșitul știrilor
            } catch (IOException e) {
                System.out.println("Error reading number of news requested: " + e.getMessage());
            }
        } else {
            sender.sendMessage("invalid"); // Notify the client that the domain is invalid
        }
    }

    public void sendDomainOptions(ClientHandler client) {
        try {
            String options = "1. Matematica\n2. Informatica\n3. Fizica\nSelecteaza un domeniu: ";
            client.sendMessage(options);
        } catch (Exception e) {
            System.out.println("Error sending domain options: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        Server server = new Server(8001);
    }
}

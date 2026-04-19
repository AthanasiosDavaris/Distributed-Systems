package gr.uop.ds.lab6;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Lab: Multi-client chat server (thread pool) + shared state + broadcast (Programmatismos Katanemimenon Systhmaton).
 * DIT UoP
 *
 * Run:
 *   javac -d out $(find src -name "*.java")
 *   java -cp out gr.uop.ds.lab6.ChatServer 5555
 */
public class ChatServer {
  private static final int DEFAULT_PORT = 5555;
  private static final int DEFAULT_POOL_SIZE = 16;

  public static void main(String[] args) {
    int port = (args.length >= 1) ? Integer.parseInt(args[0]) : DEFAULT_PORT;
    int poolSize = (args.length >= 2) ? Integer.parseInt(args[1]) : DEFAULT_POOL_SIZE;

    ClientRegistry registry = new ClientRegistry();
    ExecutorService pool = Executors.newFixedThreadPool(poolSize);

    System.out.println("[server] starting on port " + port + " (pool=" + poolSize + ")");

    try (ServerSocket serverSocket = new ServerSocket(port)) {
      while (true) {
        Socket client = serverSocket.accept();
        System.out.println("[server] accepted " + client.getRemoteSocketAddress());
        pool.submit(new ClientHandler(client, registry));
      }
    } catch (IOException e) {
      System.err.println("[server] fatal: " + e.getMessage());
      e.printStackTrace();
    }
  }
}

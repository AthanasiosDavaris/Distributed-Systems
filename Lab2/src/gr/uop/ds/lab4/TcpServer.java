package gr.uop.ds.lab4;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Minimal TCP server (request/response, line-based).
 *
 * Protocol (single-line):
 *   - Client sends one line (command)
 *   - Server replies with exactly one line
 *
 * Response format:
 *   - OK <payload>
 *   - ERR <code> <message>
 */
public class TcpServer {
  private static final int DEFAULT_PORT = 5555;

  public static void main(String[] args) {
    int port = DEFAULT_PORT;

    // Optional port argument: java -cp out gr.uop.ds.lab4.TcpServer 6000
    if (args.length >= 1) {
      try {
        port = Integer.parseInt(args[0]);
      } catch (NumberFormatException e) {
        System.err.println("[server] bad port. Usage: TcpServer [port]");
        return;
      }
    }

    System.out.println("[server] starting on port " + port);

    try (ServerSocket serverSocket = new ServerSocket(port)) {
      // Blocks in accept(). Each accepted connection gets its own handler thread.
      acceptForever(serverSocket);
    } catch (IOException e) {
      System.err.println("[server] fatal: " + e.getMessage());
      e.printStackTrace();
    }
  }

  /**
   * Accept connections forever and spawn one handler thread per client.
   * This is intentionally simple for lab/demo purposes.
   */
  private static void acceptForever(ServerSocket serverSocket) throws IOException {
    while (true) {
      Socket client = serverSocket.accept();
      System.out.println("[server] accepted " + client.getRemoteSocketAddress());

      ClientHandler handler = new ClientHandler(client);
      Thread t = new Thread(handler);
      t.start();
    }
  }
}

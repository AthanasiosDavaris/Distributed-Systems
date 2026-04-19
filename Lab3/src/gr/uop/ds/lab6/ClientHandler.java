package gr.uop.ds.lab6;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * One handler per connected client (submitted to the server's pool)
 * (Programmatismos Katanemimenon Systhmaton).
 * DIT UoP
 *
 * Demonstrates clean cleanup on disconnect (no crash).
 */
public class ClientHandler implements Runnable {
  private final Socket socket;
  private final ClientRegistry registry;

  public ClientHandler(Socket socket, ClientRegistry registry) {
    this.socket = socket;
    this.registry = registry;
  }

  @Override
  public void run() {
    String who = String.valueOf(socket.getRemoteSocketAddress());
    System.out.println("[handler] start " + who);

    // Increment total connections safely
    registry.incrementConnects();

    // bind socket to this thread so Protocol.sessionLoop(...) can access it
    Protocol.bindSocket(socket);

    try (
        BufferedReader in = new BufferedReader(
            new InputStreamReader(socket.getInputStream()));
        PrintWriter out = new PrintWriter(
            new OutputStreamWriter(socket.getOutputStream()),
            true)) {
      out.println("OK READY");
      Protocol.sessionLoop(in, out, registry);
    } catch (IOException e) {
      System.err.println("[handler] io error " + who + ": " + e.getMessage());
    } finally {
      // remove from shared state no matter how we exit
      registry.forceUnregisterBySocket(socket);

      Protocol.clearSocket();

      try {
        socket.close();
      } catch (IOException ignored) {
      }
      System.out.println("[handler] end " + who);
    }
  }
}

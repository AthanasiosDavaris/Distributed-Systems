package gr.uop.ds.lab4;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * 1 handler == 1 client connection.
 *
 * Responsibilities:
 *   - open streams
 *   - send greeting (OK READY)
 *   - readLine() in a loop
 *   - for each input line: produce exactly one output line
 */
public class ClientHandler implements Runnable {
  private static final int MAX_LINE_CHARS = 2000;

  private final Socket socket;

  public ClientHandler(Socket socket) {
    this.socket = socket;
  }

  @Override
  public void run() {
    String who = String.valueOf(socket.getRemoteSocketAddress());
    System.out.println("[handler] start " + who);

    try (
      BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
      PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true)
    ) {
      // Greeting helps with manual testing via: nc localhost 5555
      out.println("OK READY");
      loop(in, out, who);
    } catch (IOException e) {
      System.err.println("[handler] io error " + who + ": " + e.getMessage());
    } finally {
      try {
        socket.close();
      } catch (IOException ignored) {
        // ignore
      }
      System.out.println("[handler] end " + who);
    }
  }

  private static void loop(BufferedReader in, PrintWriter out, String who) throws IOException {
    String line;
    while ((line = in.readLine()) != null) {
      System.out.println("[handler] " + who + " > " + line);

      String response;
      if (line.length() > MAX_LINE_CHARS) {
        response = "ERR 413 too-long";
      } else {
        response = Commands.handle(line);
      }

      out.println(response);
      System.out.println("[handler] " + who + " < " + response);

      if (Commands.isQuit(line)) {
        break;
      }
    }
  }
}

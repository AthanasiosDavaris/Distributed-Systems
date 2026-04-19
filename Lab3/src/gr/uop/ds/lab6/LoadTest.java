package gr.uop.ds.lab6;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Simple load test for the homework (Programmatismos Katanemimenon Systhmaton):
 * DIT UoP
 *  - 10 clients
 *  - each sends 20 SAY messages
 *
 * Run:
 *   java -cp out gr.uop.ds.lab6.LoadTest localhost 5555
 */
public class LoadTest {
  private static final int DEFAULT_CLIENTS = 10;
  private static final int DEFAULT_MSGS = 20;

  public static void main(String[] args) throws InterruptedException {
    String host = (args.length >= 1) ? args[0] : "localhost";
    int port = (args.length >= 2) ? Integer.parseInt(args[1]) : 5555;
    int clients = (args.length >= 3) ? Integer.parseInt(args[2]) : DEFAULT_CLIENTS;
    int msgs = (args.length >= 4) ? Integer.parseInt(args[3]) : DEFAULT_MSGS;

    AtomicInteger ok = new AtomicInteger();
    AtomicInteger err = new AtomicInteger();
    AtomicInteger timeouts = new AtomicInteger();

    CountDownLatch latch = new CountDownLatch(clients);
    List<Thread> threads = new ArrayList<>();

    long start = System.currentTimeMillis();

    for (int i = 0; i < clients; i++) {
      final int id = i;
      Thread t = new Thread(() -> {
        String nick = "c" + id;
        try {
          runOneClient(host, port, nick, msgs, ok, err);
        } catch (IOException e) {
          err.incrementAndGet();
        } catch (RuntimeException e) {
          timeouts.incrementAndGet();
        } finally {
          latch.countDown();
        }
      }, "load-client-" + i);
      threads.add(t);
      t.start();
    }

    latch.await();
    long dur = System.currentTimeMillis() - start;

    System.out.println("=== LoadTest summary ===");
    System.out.println("clients: " + clients + ", msgs/client: " + msgs);
    System.out.println("OK: " + ok.get());
    System.out.println("ERR: " + err.get());
    System.out.println("timeouts: " + timeouts.get());
    System.out.println("duration_ms: " + dur);
  }

  private static void runOneClient(
    String host,
    int port,
    String nick,
    int msgs,
    AtomicInteger ok,
    AtomicInteger err
  ) throws IOException {

    try (Socket socket = new Socket()) {
      socket.connect(new InetSocketAddress(host, port), 2000);
      socket.setSoTimeout(2500);

      try (
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true)
      ) {
        // READY
        readOneLine(in);

        // NICK
        out.println("NICK " + nick);
        String r1 = readResponseIgnoringMsg(in);
        count(r1, ok, err);

        // SAY messages
        for (int i = 0; i < msgs; i++) {
          out.println("SAY msg-" + i);
          String r = readResponseIgnoringMsg(in);
          count(r, ok, err);
        }

        // QUIT
        out.println("QUIT");
        String r2 = readResponseIgnoringMsg(in);
        count(r2, ok, err);
      }
    }
  }

  private static void count(String line, AtomicInteger ok, AtomicInteger err) {
    if (line == null) {
      err.incrementAndGet();
      return;
    }
    if (line.startsWith("OK")) ok.incrementAndGet();
    else if (line.startsWith("ERR")) err.incrementAndGet();
    else {
      // should not happen if we use readResponseIgnoringMsg
      err.incrementAndGet();
    }
  }

  private static String readOneLine(BufferedReader in) throws IOException {
    String line = in.readLine();
    if (line == null) throw new IOException("server closed");
    return line;
  }

  /**
   * Reads until we see a protocol response (OK/ERR), skipping broadcast pushes (MSG ...).
   * If we loop too long without a response, throw RuntimeException to count as timeout.
   */
  private static String readResponseIgnoringMsg(BufferedReader in) throws IOException {
    int guard = 0;
    while (true) {
      String line = readOneLine(in);
      if (line.startsWith("MSG ")) {
        guard++;
        if (guard > 5000) throw new RuntimeException("too many MSG without response");
        continue;
      }
      return line;
    }
  }
}

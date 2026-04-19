package gr.uop.ds.lab4;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Minimal client for quick testing.
 * Sends exactly ONE command line and prints exactly ONE response line.
 *
 * Usage:
 *   java -cp out gr.uop.ds.lab4.TcpClient [host] [port] [command]
 *
 * Examples:
 *   java -cp out gr.uop.ds.lab4.TcpClient localhost 5555 TIME
 *   java -cp out gr.uop.ds.lab4.TcpClient localhost 5555 "ECHO hi there"
 */
public class TcpClient {
  public static void main(String[] args) throws Exception {
    String host = (args.length >= 1) ? args[0] : "localhost";
    int port = (args.length >= 2) ? Integer.parseInt(args[1]) : 5555;
    String cmd = (args.length >= 3) ? args[2] : "TIME";

    try (
      Socket socket = new Socket(host, port);
      BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
      PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true)
    ) {
      // server greeting
      System.out.println("S: " + in.readLine());

      out.println(cmd);
      System.out.println("S: " + in.readLine());
    }
  }
}

package gr.uop.ds.lab6;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Line-based protocol for the lab (Programmatismos Katanemimenon Systhmaton).
 * DIT UoP
 *
 * Requests:
 * NICK <name>
 * SAY <text>
 * WHO
 * HELP
 * QUIT
 *
 * Responses:
 * OK ...
 * ERR <code> <message>
 *
 * Broadcast push:
 * MSG <from> <text>
 */
public final class Protocol {
  private Protocol() {
  }

  private static final int MAX_LINE = 4096;

  private static final ThreadLocal<Socket> TLS_SOCKET = new ThreadLocal<>();

  public static void bindSocket(Socket socket) {
    TLS_SOCKET.set(socket);
  }

  public static void clearSocket() {
    TLS_SOCKET.remove();
  }

  private static Socket currentSocket() {
    return TLS_SOCKET.get();
  }

  public static void sessionLoop(BufferedReader in, PrintWriter out, ClientRegistry registry) throws IOException {
    Socket socket = currentSocket();
    if (socket == null) {
      out.println("ERR 500 INTERNAL no_socket_bound");
      return;
    }

    while (true) {
      String line = in.readLine();
      if (line == null) {
        // client disconnected
        return;
      }

      line = line.trim();
      if (line.isEmpty()) {
        out.println("ERR 400 BAD_REQUEST");
        continue;
      }
      if (line.length() > MAX_LINE) {
        out.println("ERR 413 TOO_LONG");
        continue;
      }

      String cmd;
      String arg = "";
      int sp = line.indexOf(' ');
      if (sp < 0) {
        cmd = line;
      } else {
        cmd = line.substring(0, sp);
        arg = line.substring(sp + 1).trim();
      }

      switch (cmd.toUpperCase()) {
        case "HELP":
          out.println(
              "OK COMMANDS: NICK <name> | SAY <text> | PM <nick> <text> | JOIN <room> | LEAVE | WHO | STATS | HELP | QUIT");
          break;

        case "JOIN": {
          if (registry.getNickBySocket(socket) == null) {
            out.println("ERR 401 NICK_REQUIRED");
            break;
          }
          if (arg.isEmpty()) {
            out.println("ERR 400 BAD_REQUEST missing_room");
            break;
          }
          String err = registry.joinRoom(socket, arg);
          if (err != null) {
            out.println(err);
          } else {
            out.println("OK JOINED " + arg.trim());
          }
          break;
        }

        case "LEAVE": {
          if (registry.getNickBySocket(socket) == null) {
            out.println("ERR 401 NICK_REQUIRED");
            break;
          }
          String err = registry.leaveRoom(socket);
          if (err != null) {
            out.println(err);
          } else {
            out.println("OK LEFT");
          }
          break;
        }

        case "PM": {
          String senderNick = registry.getNickBySocket(socket);
          if (senderNick == null) {
            out.println("ERR 401 NICK_REQUIRED");
            break;
          }

          int spaceIdx = arg.indexOf(' ');
          if (spaceIdx < 0) {
            out.println("ERR 400 BAD_REQUEST usage: PM <nick> <text>");
            break;
          }

          String targetNick = arg.substring(0, spaceIdx).trim();
          String pmText = arg.substring(spaceIdx + 1).trim();

          if (targetNick.isEmpty() || pmText.isEmpty()) {
            out.println("ERR 400 BAD_REQUEST empty_nick_or_text");
            break;
          }

          registry.incrementMessages();

          String err = registry.sendPrivateMessage(senderNick, targetNick, pmText);
          if (err != null) {
            out.println(err);
          } else {
            out.println("OK PM_SENT");
          }
          break;
        }

        case "STATS": {
          String stats = registry.getStats();
          out.println("OK STATS " + stats);
          break;
        }

        case "NICK": {
          if (arg.isEmpty()) {
            out.println("ERR 400 BAD_REQUEST missing_nick");
            break;
          }
          String err = registry.registerNick(arg, socket, out);
          if (err != null) {
            out.println(err);
          } else {
            out.println("OK NICK " + arg.trim());
          }
          break;
        }

        case "WHO": {
          String online = registry.listOnline();
          out.println("OK ONLINE: " + online);
          break;
        }

        case "SAY": {
          String nick = registry.getNickBySocket(socket);
          if (nick == null) {
            out.println("ERR 401 NICK_REQUIRED");
            break;
          }
          if (arg.isEmpty()) {
            out.println("ERR 400 BAD_REQUEST empty_text");
            break;
          }

          registry.incrementMessages();

          registry.broadcast(nick, arg, socket);
          out.println("OK SENT");
          break;
        }

        case "QUIT":
          out.println("OK bye");
          return;

        default:
          out.println("ERR 404 UNKNOWN_COMMAND");
          break;
      }
    }
  }
}

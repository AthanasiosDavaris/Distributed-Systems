package gr.uop.ds.lab6;

import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Shared, thread-safe registry of connected clients (Programmatismos
 * Katanemimenon Systhmaton).
 * DIT UoP.
 *
 * Core rule implemented here:
 * - protect shared state with one lock
 * - NEVER do blocking I/O while holding the lock (broadcast uses snapshot)
 */
public class ClientRegistry {

  public static final class Session {
    public final Socket socket;
    public final PrintWriter out;

    public Session(Socket socket, PrintWriter out) {
      this.socket = socket;
      this.out = out;
    }
  }

  private final Object lock = new Object();
  private final Map<String, Session> sessionsByNick = new HashMap<>();
  private final Map<Socket, String> nickBySocket = new HashMap<>();
  private final Map<Socket, String> roomBySocket = new HashMap<>();
  private long totalConnects = 0;
  private long totalMessages = 0;

  /**
   * Register nickname for the current socket. Nicknames must be unique.
   *
   * @return null on success, otherwise an ERR line (already formatted).
   */
  public String registerNick(String nick, Socket socket, PrintWriter out) {
    if (nick == null) {
      return "ERR 400 BAD_REQUEST";
    }

    String clean = nick.trim();
    if (clean.isEmpty()) {
      return "ERR 400 BAD_REQUEST empty_nick";
    }
    if (!clean.matches("[A-Za-z0-9_]{1,16}")) {
      return "ERR 422 INVALID_NICK use [A-Za-z0-9_] len<=16";
    }

    synchronized (lock) {
      String existing = nickBySocket.get(socket);
      if (existing != null && existing.equals(clean)) {
        return null; // already set to this nick
      }
      if (sessionsByNick.containsKey(clean)) {
        return "ERR 409 NICK_TAKEN";
      }

      // if socket had an old nick, remove it first (simple "rename")
      if (existing != null) {
        sessionsByNick.remove(existing);
        nickBySocket.remove(socket);
      }

      sessionsByNick.put(clean, new Session(socket, out));
      nickBySocket.put(socket, clean);
      return null;
    }
  }

  public void unregisterNick(String nick) {
    if (nick == null)
      return;
    synchronized (lock) {
      Session s = sessionsByNick.remove(nick);
      if (s != null) {
        nickBySocket.remove(s.socket);
        roomBySocket.remove(s.socket);
      }
    }
  }

  public void forceUnregisterBySocket(Socket socket) {
    if (socket == null)
      return;
    synchronized (lock) {
      String nick = nickBySocket.remove(socket);
      if (nick != null) {
        sessionsByNick.remove(nick);
      }
      roomBySocket.remove(socket);
    }
  }

  public String joinRoom(Socket socket, String room) {
    if (room == null || room.trim().isEmpty()) {
      return "ERR 400 BAD_REQUEST empty_room";
    }
    synchronized (lock) {
      roomBySocket.put(socket, room.trim());
    }
    return null;
  }

  public String leaveRoom(Socket socket) {
    synchronized (lock) {
      if (!roomBySocket.containsKey(socket)) {
        return "ERR 400 BAD_REQUEST not_in_a_room";
      }
      roomBySocket.remove(socket);
    }
    return null;
  }

  public String getNickBySocket(Socket socket) {
    synchronized (lock) {
      return nickBySocket.get(socket);
    }
  }

  public String listOnline() {
    synchronized (lock) {
      if (sessionsByNick.isEmpty())
        return "";
      return String.join(",", sessionsByNick.keySet());
    }
  }

  public void incrementConnects() {
    synchronized (lock) {
      totalConnects++;
    }
  }

  public void incrementMessages() {
    synchronized (lock) {
      totalMessages++;
    }
  }

  public String getStats() {
    synchronized (lock) {
      int online = sessionsByNick.size();
      return "online: " + online + ", messages: " + totalMessages + ", connects: " + totalConnects;
    }
  }

  /**
   * Send a private message to a specific user.
   * 
   * @return null on success, or an ERR string if the user is not found.
   */
  public String sendPrivateMessage(String from, String toNick, String text) {
    Session target;
    synchronized (lock) {
      target = sessionsByNick.get(toNick);
    }

    // If the user doesn't exist or is offline
    if (target == null) {
      return "ERR 404 USER_NOT_FOUND";
    }

    // Send outside the lock
    final String msg = "PM " + from + " " + text;
    try {
      target.out.println(msg);
      if (target.out.checkError()) {
        forceUnregisterBySocket(target.socket);
      }
    } catch (Exception ignored) {
      forceUnregisterBySocket(target.socket);
    }

    return null;
  }

  /**
   * Broadcast to ALL connected clients (including the sender).
   * Uses snapshot => no blocking I/O inside the lock.
   */
  public void broadcast(String from, String text, Socket senderSocket) {
    // why do these if exist ???
    if (from == null)
      from = "anon";
    if (text == null)
      text = "";

    final String msg = "MSG " + from + " " + text;

    List<Session> targets = new ArrayList<>();
    synchronized (lock) {
      String senderRoom = roomBySocket.get(senderSocket);

      for (Session s : sessionsByNick.values()) {
        String targetRoom = roomBySocket.get(s.socket);

        // If both are null (global chat), OR both are in the exact same room
        if ((senderRoom == null && targetRoom == null) || (senderRoom != null && senderRoom.equals(targetRoom))) {
          targets.add(s);
        }
      }
    }

    // send outside lock
    List<Socket> dead = new ArrayList<>();
    for (Session s : targets) {
      try {
        s.out.println(msg);
        // PrintWriter doesn't throw; check error flag
        if (s.out.checkError()) {
          dead.add(s.socket);
        }
      } catch (Exception ignored) {
        dead.add(s.socket);
      }
    }

    // best-effort cleanup of dead sockets
    if (!dead.isEmpty()) {
      synchronized (lock) {
        for (Socket sock : dead) {
          String nick = nickBySocket.remove(sock);
          if (nick != null) {
            sessionsByNick.remove(nick);
          }
        }
      }
    }
  }
}

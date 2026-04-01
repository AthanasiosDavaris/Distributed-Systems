package gr.uop.ds.lab4;

import java.time.LocalDateTime;

/**
 * Command parsing and routing for the lab protocol.
 *
 * Commands (single line):
 * - ECHO <text>
 * - TIME
 * - HELP
 * - QUIT
 * - UPPER <text> (mini in-class extension)
 * - LOWER <text>
 * - REVERSE <text>
 * - ADD <a> <b>
 * - LEN <text>
 */
public final class Commands {
  private Commands() {
    // utility class
  }

  public static String handle(String rawLine) {
    String line = (rawLine == null) ? "" : rawLine.trim();
    if (line.isEmpty()) {
      return "ERR 400 empty-request";
    }

    // Split into: COMMAND + (rest-of-line as args)
    String[] parts = line.split("\\s+", 2);
    String cmd = parts[0].toUpperCase();
    String args = (parts.length == 2) ? parts[1] : "";

    switch (cmd) {
      case "ECHO":
        return handleEcho(args);
      case "TIME":
        return "OK " + LocalDateTime.now();
      case "HELP":
        return handleHelp(args);
      case "UPPER":
        return handleUpper(args);
      case "LOWER":
        return handleLower(args);
      case "REVERSE":
        return handleReverse(args);
      case "ADD":
        return handleAdd(args);
      case "LEN":
        return handleLen(args);
      case "QUIT":
        return "OK bye";
      default:
        return "ERR 404 unknown-command";
    }
  }

  private static String handleEcho(String args) {
    if (args.isEmpty()) {
      return "ERR 422 bad-args";
    }
    return "OK " + args;
  }

  private static String handleUpper(String args) {
    if (args.isEmpty()) {
      return "ERR 422 bad-args";
    }
    return "OK " + args.toUpperCase();
  }

  private static String handleLower(String args) {
    if (args.isEmpty()) {
      return "ERR 422 bad-args";
    }
    return "OK " + args.toLowerCase();
  }

  private static String handleReverse(String args) {
    if (args.isEmpty()) {
      return "ERR 422 bad-args";
    }
    return "OK " + new StringBuilder(args).reverse().toString();
  }

  private static String handleAdd(String args) {
    if (args.isEmpty()) {
      return "ERR 422 bad-args (Empty String)";
    }

    String[] numbers = args.split("\\s+");
    if (numbers.length != 2) {
      return "ERR 422 bad-args (expected two numbers)";
    }

    try {
      int a = Integer.parseInt(numbers[0]);
      int b = Integer.parseInt(numbers[1]);
      return "OK " + (a + b);
    } catch (NumberFormatException e) {
      return "ERR 422 bad-args (invalid number)";
    }
  }

  private static String handleLen(String args) {
    if (args.isEmpty()) {
      return "ERR 422 bad-args";
    }
    return "OK " + args.length();
  }

  private static String handleHelp(String args) {
    if (args.isEmpty()) {
      return "OK COMMANDS: ECHO <text> | TIME | UPPER <text> | LOWER <text> | REVERSE <text> | ADD <a> <b> | LEN  <text> | QUIT | HELP [cmd] ";
    }

    String cmd = args.toUpperCase();
    switch (cmd) {
      case "ECHO":
        return "OK ECHO <text> - Returns the provided text exactly as received. (Ex: ECHO hello world)";
      case "TIME":
        return "OK TIME - Returns the current server date and time. (Ex: TIME)";
      case "UPPER":
        return "OK UPPER <text> - Converts the provided text to uppercase. (Ex: UPPER hello)";
      case "LOWER":
        return "OK LOWER <text> - Converts the provided text to lowercase. (Ex: LOWER HELLO)";
      case "REVERSE":
        return "OK REVERSE <text> - Reverses the characters in the provided text. (Ex: REVERSE abc)";
      case "ADD":
        return "OK ADD <a> <b> - Adds two integers and returns the sum. (Ex: ADD 5 3)";
      case "LEN":
        return "OK LEN <text> - Returns the number of characters in the provided text. (Ex: LEN word)";
      case "QUIT":
        return "OK QUIT - Closes the connection to the server. (Ex: QUIT)";
      case "HELP":
        return "OK HELP [cmd] - Shows general help, or specific help for a command. (Ex: HELP ADD)";
      default:
        return "ERR 404 unknown-command-help";
    }
  }

  public static boolean isQuit(String rawLine) {
    if (rawLine == null) {
      return false;
    }
    return rawLine.trim().equalsIgnoreCase("QUIT");
  }
}

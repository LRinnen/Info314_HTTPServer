import java.io.*;
import java.net.*;


public class HTTPClient {

  // args[0] - method type ie. GET, PUT, POST, DELETE
  // args[1] - path/name of file ie. /example.html
  // args[2-n] - body of request ie. Hello! I am going into a new file!
  public static void main(String... args) {
    String host = "localhost";
    int port = 80;
    String method = args[0];
    String name = args[1];
    StringBuilder message = new StringBuilder("");
    int numArgs = args.length;

    // check to see if there is a message
    for (int i = 2; i < numArgs; i++) {
      message.append(args[i] + ' ');
    }

    int length = message.toString().getBytes().length;

    try (Socket socket = new Socket(host, port)) {
      OutputStream out = socket.getOutputStream();

      if (method.equals("PUT")) {
        // PUT
        String put = "PUT " + name + " HTTP/1.1\nHost: localhost\nContent-Type: text/html\nContent-Length: " + (length + 35) + "\n\n<html>\n<body>\n" + message.toString() + "\n</body>\n</html>\n\n";
        out.write(put.getBytes());
        System.out.println("PUT request sent:");
        System.out.println(put);

      } else if (method.equals("GET")) {
        // GET
        String get = "GET " + name + " HTTP/1.1\n Host: localhost\n Accept: */*\n\n";
        out.write(get.getBytes());
        System.out.println("GET request sent:");
        System.out.println(get);

      } else if (method.equals("POST")) {
        // POST
        String post = "POST " + name + " HTTP/1.1\nHost: localhost\nContent-Type: application/text\nContent-Length: " + length + "\n\n" + message.toString() + "\n\n";
        out.write(post.getBytes());
        System.out.println("POST request sent:");
        System.out.println(post);

      } else if (method.equals("DELETE")) {
        // DELETE
        String delete = "DELETE " + name + " HTTP/1.1\n\n";
        out.write(delete.getBytes());
        System.out.println("DELETE request sent:");
        System.out.println(delete);

      }

      // get the response
      InputStream input = socket.getInputStream();
      int exists = input.read();
      while (exists != -1) {
        System.out.write(exists);
        exists = input.read();
      }

    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}


import java.io.*;
import java.net.*;
import java.nio.file.Files;


public class HTTPServer {
  static String startPath = System.getProperty("user.dir");

  public static void handlePutRequest(String name, BufferedReader input, OutputStream output) {
    try {
      String line = input.readLine();

      // Get length of body
      int contentLength = 0;
      while (line != null && line.length() > 0) {
        line = input.readLine();
        String[] tokens = line.split(": ");
        if (tokens[0].equals("Content-Length")) {
          contentLength = Integer.parseInt(tokens[1]);
        }
      }

      // Reference to new file
      File newFile = new File(startPath + name);

      // Create an empty file
      boolean created = false;
      try {
        created = newFile.createNewFile();
      } catch (Exception e) {
        e.printStackTrace();
      }
      System.out.println("Created new file");


      // Overwrite any existing text - append: false
      FileOutputStream fos = new FileOutputStream(newFile, false);

      // Read body and write to the new file
      char[] body = new char[contentLength];
      input.read(body, 0, contentLength);
      fos.write(new String(body).getBytes());

      System.out.println("Wrote request body to file");


      // Check to see which status code to return
      if (created) {
        // True, file didn't exist beforehand - 201 Created
        String statusCode = "HTTP/1.1 201 Created\n\n";
        output.write(statusCode.getBytes());
      } else {
        // False, file did exist beforehand - 200 OK
        String statusCode = "HTTP/1.1 200 OK \n\n";
        output.write(statusCode.getBytes());
      }
      System.out.println("Request complete, response sent!");
      System.out.println();

      fos.close();

    } catch(Exception e) {
      e.printStackTrace();
    }
  }


  public static void handleGetRequest(String name, OutputStream output) {
    try {
      // File reference
      File filePath = new File(startPath + name);

      if (filePath.exists()) {
        System.out.println("File found");

        // File info
        String fileType = Files.probeContentType(filePath.toPath());
        byte[] file = Files.readAllBytes(filePath.toPath());
        String statusCode = "HTTP/1.1 200 OK\nContent-Type: " + fileType + "\nContent-Length: " + file.length + "\n\n";
        output.write(statusCode.getBytes());

        output.write(file);

        output.flush();
      } else {
        // File doesn't exist
        System.out.println("File not found");
        String body = "<html><body><img src='https://http.cat/404'></body></html>";
        String statusCode = "HTTP/1.1 404 Not Found\nContent-Type: text/html\nContent-Length " + body.getBytes().length + "\n\n";
        output.write(statusCode.getBytes());
        output.write(body.getBytes());
      }

      System.out.println("Request complete, response sent!");
      System.out.println();


    } catch(Exception e) {
      e.printStackTrace();
    }
  }


  public static void handlePostRequest(String name, BufferedReader input, OutputStream output) {
    try {
      // File reference
      File filePath = new File(startPath + name);

      if (filePath.exists()) {
        System.out.println("File found");

        String inputLine = input.readLine();

        // Get length of body
        int contentLength = 0;
        while (inputLine != null && inputLine.length() > 0) {
          inputLine = input.readLine();
          String[] tokens = inputLine.split(": ");
          if (tokens[0].equals("Content-Length")) {
            contentLength = Integer.parseInt(tokens[1]);
          }
        }

        // Add new text to file
        FileOutputStream fos = new FileOutputStream(filePath, true);

        char[] body = new char[contentLength];
        input.read(body, 0, contentLength);
        fos.write(new String(body).getBytes());

        System.out.println("Added new text to the file");

        // File info
        String fileType = Files.probeContentType(filePath.toPath());
        byte[] file = Files.readAllBytes(filePath.toPath());
        String statusCode = "HTTP/1.1 200 OK\nContent-Type: " + fileType + "\nContent-Length: " + file.length + "\n\n";
        output.write(statusCode.getBytes());

        output.write(file);

        output.flush();
        fos.close();

      } else {
        // File doesn't exist
        System.out.println("File not found");
        String body = "<html><body><img src='https://http.cat/404'></body></html>";
        String statusCode = "HTTP/1.1 404 Not Found\nContent-Type: text/html\nContent-Length " + body.getBytes().length + "\n\n";
        output.write(statusCode.getBytes());
        output.write(body.getBytes());
      }

      System.out.println("Request complete, response sent!");
      System.out.println();

    } catch (Exception e) {
      e.printStackTrace();
    }
  }


  public static void handleDeleteRequest(String name, OutputStream output) {
    try {
      // File reference
      File file = new File(startPath + name);

      if (file.delete()) {
        // File sucessfully deleted
        String statusCode = "HTTP/1.1 200 OK\n\n";
        output.write(statusCode.getBytes());
      } else {
        // File failed to delete
        String body = "<html><body><img src='https://http.cat/500'></body></html>";
        String statusCode = "HTTP/1.1 500 Internal Server Error\nContent-Type: text/html\nContent-Length " + body.getBytes().length + "\n\n";
        output.write(statusCode.getBytes());
        output.write(body.getBytes());
      }

      System.out.println("Request complete, response sent!");
      System.out.println();

    } catch(Exception e) {
      e.printStackTrace();
    }
  }


  public static void main (String... args) throws Exception {
    ServerSocket server = new ServerSocket(80);

    try {
      Socket socket = null;
      while ((socket = server.accept()) != null) {
        InputStreamReader input = new InputStreamReader(socket.getInputStream());
        BufferedReader bufInput = new BufferedReader(input);
        String firstLine = bufInput.readLine();
        System.out.println(firstLine);
        String[] info = firstLine.split(" ");
        String method = info[0];
        String name = info[1];

        OutputStream output = socket.getOutputStream();

        if (method.equals("PUT")) {
          System.out.println("PUT request received");
          handlePutRequest(name, bufInput, output);

        } else if (method.equals("GET")) {
          System.out.println("GET request received");
          handleGetRequest(name, output);

        } else if (method.equals("POST")) {
          System.out.println("POST request received");
          handlePostRequest(name, bufInput, output);

        } else if (method.equals("DELETE")) {
          System.out.println("DELETE request received");
          handleDeleteRequest(name, output);

        } else {
          // throw error - request doesn't exist
          String body = "<html><body><img src='https://http.cat/400'></body></html>";
          String statusCode = "HTTP/1.1 400 Bad Request\nContent-Type: text/html\nContent-Length " + body.getBytes().length + "\n\n";
          output.write(statusCode.getBytes());
          output.write(body.getBytes());
        }

        System.out.println();
        input.close();
        bufInput.close();
        output.close();
      }
    } catch(Exception e) {
      e.printStackTrace();
    }
    server.close();
  }
}


import java.io.*;
import java.net.*;
import java.nio.file.Files;


public class HTTPServer {
  static String startPath = System.getProperty("user.dir");

  public static void handlePutRequest(String name, BufferedReader input, OutputStream output) {
    try {
      String line = input.readLine();

      // Skip to the body
      // **FIX** Is there a better way?
      while (line != null && line.length() > 0) {
        line = input.readLine();
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
      line = input.readLine();
      while (line != null && line.length() > 0) {
        fos.write(line.toString().getBytes());
        fos.write("\n".getBytes());
        line = input.readLine();
      }
      System.out.println("Wrote request body to file");


      // Check to see which status code to return
      if (created) {
        // True, file didn't exist beforehand - 201 Created
        String statusCode = "201 Created HTTP1.1\n\n";
        output.write(statusCode.getBytes());
      } else {
        // False, file did exist beforehand - 200 OK
        String statusCode = "200 OK HTTP1.1\n\n";
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
        String fileLength = String.valueOf(filePath.length());
        String statusCode = "200 OK HTTP1.1\nContent-Type: " + fileType + "\nContent-Length: " + fileLength + "\n\n";
        output.write(statusCode.getBytes());

        // Read file and write to output
        BufferedReader file = new BufferedReader(new FileReader(filePath));
        String line = file.readLine();
        while (line != null && line.length() > 0) {
          output.write(line.getBytes());
          line = file.readLine();
        }

        // End the response
        output.write("\n\n".getBytes());

        file.close();

      } else {
        // File doesn't exist
        System.out.println("File not found");
        String statusCode = "404 Not Found HTTP1.1\n\n";
        output.write(statusCode.getBytes());
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

        // Add new text to file
        RandomAccessFile file = new RandomAccessFile(filePath, "rw");

        // Move pointer to the bottom of the html body
        long position = file.length() - 16;
        file.seek(position);


        String inputLine = input.readLine();

        // Skip to the body
        // **FIX** Is there a better way?
        while (inputLine != null && inputLine.length() > 0) {
          inputLine = input.readLine();
        }

        // Read input text and write to the file
        inputLine = input.readLine();
        while (inputLine != null && inputLine.length() > 0) {
          file.write(inputLine.toString().getBytes());
          file.write("\n".getBytes());
          inputLine = input.readLine();
        }

        // Add the ending of the html file again
        file.write("</body>\n".getBytes());
        file.write("</html>\n".getBytes());

        System.out.println("Added new text to the file");


        // File info
        String fileType = Files.probeContentType(filePath.toPath());
        String fileLength = String.valueOf(filePath.length());
        String statusCode = "200 OK HTTP1.1\nContent-Type: " + fileType + "\nContent-Length: " + fileLength + "\n\n";
        output.write(statusCode.getBytes());

        // Read file and write to output
        BufferedReader fileReader = new BufferedReader(new FileReader(filePath));
        String line = fileReader.readLine();
        while (line != null && line.length() > 0) {
          output.write(line.getBytes());
          line = fileReader.readLine();
        }

        // End the response
        output.write("\n\n".getBytes());

        file.close();
        fileReader.close();

      } else {
        // File doesn't exist
        System.out.println("File not found");
        String statusCode = "404 Not Found HTTP1.1\n\n";
        output.write(statusCode.getBytes());
      }

      System.out.println("Request complete, response sent!");
      System.out.println();

    } catch(Exception e) {
      e.printStackTrace();
    }
  }


  public static void handleDeleteRequest(String name, OutputStream output) {
    try {
      // File reference
      File file = new File(startPath + name);

      if (file.delete()) {
        // File sucessfully deleted
        String statusCode = "200 OK HTTP1.1\n\n";
        output.write(statusCode.getBytes());
      } else {
        // File failed to delete
        String statusCode = "500 Internal Server Error HTTP1.1\n\n";
        output.write(statusCode.getBytes());
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
          String error = "400 Bad Request HTTP1.1\n\n";
          output.write(error.getBytes());
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


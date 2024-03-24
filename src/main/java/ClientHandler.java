import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientHandler implements Runnable {

    private final Socket clientSocket;
    private final BufferedReader reader;
    private final PrintWriter writer;

    private static final Logger logger = LogManager.getLogger(ClientHandler.class);


    public ClientHandler(Socket clientSocket) throws IOException {
        this.clientSocket = clientSocket;
        this.reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        this.writer = new PrintWriter(clientSocket.getOutputStream(), true);
        String currentDirectory = System.getProperty("user.dir");
        String log4jPropertiesPath = currentDirectory + "/log4j.properties";
        PropertyConfigurator.configure(log4jPropertiesPath);
    }

    @Override
    public void run() {
        try {


            String loggedUser = reader.readLine();
            logger.info("loggeduser : " + loggedUser);

            if (loggedUser == null) {
                return;
            } else if (!Server.clients.containsKey(loggedUser)) {
                Server.addClient(loggedUser, this);
                if (Server.clients.containsKey(loggedUser)) {
                    logger.info("ACCEPTED");
                    writer.println("ACCEPTED");
                }
            }

            while (true) {
                String message = reader.readLine();
                if (message == null) {
                    break;
                }

                int separatorIndex = message.indexOf(":");
                String receiver = message.substring(0, separatorIndex);
                String content = message.substring(separatorIndex + 2);

                Server.sendMessage(loggedUser, receiver, content);
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendMessage(String message) {
        writer.println(message);
    }
}

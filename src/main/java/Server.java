import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.LogManager;
import org.apache.log4j.PropertyConfigurator;
import org.jpos.iso.ISOException;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import org.apache.log4j.Logger;

public class Server {
    private static final int PORT = 9090;

    public static HashMap<String, ClientHandler> clients = new HashMap<>();

    private static final Logger logger = LogManager.getLogger(Server.class);
    public static void main(String[] args) throws IOException {


        String currentDirectory = System.getProperty("user.dir");
        String log4jPropertiesPath = currentDirectory + "/log4j.properties";
        PropertyConfigurator.configure(log4jPropertiesPath);

        ServerSocket listener = new ServerSocket(PORT);

        while (true) {

            logger.info("[SERVER] waiting for client connection...");
            Socket client = listener.accept();
            logger.info("[SERVER] Connected to client");

            ClientHandler clientThread = new ClientHandler(client);


            new Thread(clientThread).start();

        }
    }

    public static void addClient(String username, ClientHandler clientHandler) {
        clients.put(username, clientHandler);
    }

    public static void sendMessage(String sender, String receiver, String iso8583Message) {
        try {
            // Decode ISO 8583 message using ISO8583MessageHandler
            VisaMessageHandler isoHandler = new VisaMessageHandler("visapack.xml");
            MasterMessageHandler masterMessageHandler = new MasterMessageHandler("mastercard.xml");

            if(iso8583Message.startsWith("visa")){

                String visaMessage = iso8583Message.substring(4);
                String decodedMessage = isoHandler.decodeISO8583Message(visaMessage);

                // Modify the decoded message as needed
                String modifiedMessage = sender + ": " + decodedMessage;

                // get handler object from clients map
                ClientHandler clientHandler = clients.get(receiver);
                if (clientHandler != null) {
                    // pass modified message to receiving client
                    clientHandler.sendMessage(modifiedMessage);
                } else {
                    System.out.println("User '" + receiver + "' not found.");
                }

            }

            if(iso8583Message.startsWith("mastercard")){
                String masterMessage = iso8583Message.substring(10);
                String decodedMasterMessage = masterMessageHandler.decodeMastercardISO8583Message(masterMessage);

                // Modify the decoded message as needed
                String modifiedMessage = sender + ": " + decodedMasterMessage;

                // get handler object from clients map
                ClientHandler clientHandler = clients.get(receiver);
                if (clientHandler != null) {
                    // pass modified message to receiving client
                    clientHandler.sendMessage(modifiedMessage);
                } else {
                    System.out.println("User '" + receiver + "' not found.");
                }
            }

        } catch (ISOException e) {
            e.printStackTrace();
            System.out.println("Error decoding ISO 8583 message");
        }
    }


}

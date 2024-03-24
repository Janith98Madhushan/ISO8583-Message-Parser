import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.jpos.iso.ISOException;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.Socket;

public class Client {

    private static final String SERVER_ADDRESS = "localhost";
    private static final int PORT = 9090;

    private final VisaMessageHandler visaMessageHandler;

    private static final Logger logger = LogManager.getLogger(Client.class);



    JFrame frame = new JFrame("Chatter App");
    JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    JTextField textFieldMessage = new JTextField(40);
    JTextField textFieldReceiver = new JTextField("Receiver's name", 20);
    JTextArea messageArea = new JTextArea(10, 40);

    BufferedReader inputReader;
    PrintWriter writer;

    String sendMessage;

    public Client() throws  ISOException {
        this.visaMessageHandler = new VisaMessageHandler("visapack.xml");

        topPanel.add(textFieldReceiver);
        textFieldMessage.setEditable(false);
        messageArea.setEditable(false);
        textFieldReceiver.setEditable(false);
        frame.getContentPane().add(topPanel, "North");
        frame.getContentPane().add(textFieldMessage, "Center");
        frame.getContentPane().add(new JScrollPane(messageArea), "South");
        frame.pack();

        textFieldMessage.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage = textFieldMessage.getText();
                String receiver = textFieldReceiver.getText();

                // Check if the message is ISO 8583
//                if (sendMessage.startsWith("ISO8583:")) {
//                    // Decode ISO 8583 message
//                    String decodedMessage = iso8583MessageHandler.decodeISO8583Message(sendMessage.substring(8));
//                    sendMessage = "Decoded ISO 8583 Message:\n" + decodedMessage;
//                }

                // send typed message to the server
                logger.info("Receiver :" + sendMessage);
                writer.println(receiver + ": " + sendMessage);

                // append sent message to the message area
                messageArea.append("To " + textFieldReceiver.getText() + ": " + sendMessage + "\n");
                textFieldMessage.setText("");
            }
        });
    }

    private String getName() {
        return JOptionPane.showInputDialog(
                frame,
                "Choose a screen name : ",
                "Screen name selection",
                JOptionPane.PLAIN_MESSAGE
        );
    }

    public static void main(String[] args) throws ISOException {

        String currentDirectory = System.getProperty("user.dir");
        String log4jPropertiesPath = currentDirectory + "/log4j.properties";
        PropertyConfigurator.configure(log4jPropertiesPath);

        Client client = new Client();
        client.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        client.frame.setVisible(true);

        try {
            Socket socket = new Socket(SERVER_ADDRESS, PORT);

            //client.consoleReader = new BufferedReader(new InputStreamReader(System.in));
            client.inputReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            client.writer = new PrintWriter(socket.getOutputStream(), true);


            String userName = client.getName();
            logger.info("[User] : " + userName + " Send to the server");
            client.writer.println(userName);

            // check username accepted by the server
            if (client.inputReader.readLine().equals("ACCEPTED")) {

                logger.info("Username '" + userName + "' accepted by server");
                client.frame.setTitle("Chatter App | " + userName);
                client.textFieldMessage.setEditable(true);
                client.textFieldReceiver.setEditable(true);

            }

            // create new thread for listening receiving messages
            new Thread(()->{
                try {
                    while (true) {
                        String message = client.inputReader.readLine();
                        if (message == null) {
                            break;
                        }
                        client.messageArea.append(message + "\n");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();




        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}

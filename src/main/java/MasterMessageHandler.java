import org.jpos.iso.*;
import org.jpos.iso.packager.GenericPackager;

import java.io.InputStream;

public class MasterMessageHandler {

    private final ISOPackager packager;

    public MasterMessageHandler(String packagerFilePath) throws ISOException {
        // Load the Mastercard packager using the specified configuration file
        InputStream is = getClass().getClassLoader().getResourceAsStream(packagerFilePath);
        packager = new GenericPackager(is);
    }

    public String decodeMastercardISO8583Message(String iso8583Message) {
        try {
            // Create an ISOMsg and set the raw ISO 8583 message
            ISOMsg isoMsg = new ISOMsg();
            isoMsg.setPackager(packager);

            // Convert the hex message to a byte array
            byte[] messageBytes = ISOUtil.hex2byte(iso8583Message);

            // Unpack the ISO message
            isoMsg.unpack(messageBytes);

            // Print the decoded Mastercard ISO 8583 message including all fields and subfields
            StringBuilder decodedMessage = new StringBuilder("Decoded Mastercard ISO 8583 Message:\n");
            // Recursive method to print fields and subfields
            printFields(isoMsg, decodedMessage);

            return decodedMessage.toString();
        } catch (ISOException e) {
            e.printStackTrace();
            return "Error decoding Mastercard ISO 8583 message";
        }
    }

    private void printFields(ISOMsg isoMsg, StringBuilder decodedMessage) throws ISOException {
        for (int i = 1; i <= isoMsg.getMaxField(); i++) {
            if (isoMsg.hasField(i)) {
                ISOComponent fieldComponent = isoMsg.getComponent(i);

                if (fieldComponent instanceof ISOMsg) {
                    // Recursively print subfields
                    decodedMessage.append("Field ").append(i).append(": \n");
                    printSubfields((ISOMsg) fieldComponent, decodedMessage);
                } else if (fieldComponent instanceof ISOField) {
                    // Print ISOField value
                    decodedMessage.append("Field ").append(i).append(": ").append(isoMsg.getString(i)).append("\n");
                } else if (fieldComponent instanceof ISOComponent) {
                    // Handle generic ISOComponent (may be a subfield)
                    decodedMessage.append("Field ").append(i).append(": ").append(fieldComponent.getValue()).append("\n");
                }
            }
        }
    }

    private void printSubfields(ISOMsg parentMsg, StringBuilder decodedMessage) throws ISOException {
        for (int i = 1; i <= parentMsg.getMaxField(); i++) {
            if (parentMsg.hasField(i)) {
                ISOComponent subFieldComponent = parentMsg.getComponent(i);

                if (subFieldComponent instanceof ISOMsg) {
                    // Recursively print subfields
                    decodedMessage.append("  Sub-field ").append(i).append(": \n");
                    printSubfields((ISOMsg) subFieldComponent, decodedMessage);
                } else if (subFieldComponent instanceof ISOField) {
                    // Print ISOField value
                    decodedMessage.append("  Sub-field ").append(i).append(": ").append(parentMsg.getString(i)).append("\n");
                } else if (subFieldComponent instanceof ISOComponent) {
                    // Handle generic ISOComponent (may be a subfield)
                    decodedMessage.append("  Sub-field ").append(i).append(": ").append(subFieldComponent.getValue()).append("\n");
                }
            }
        }
    }
}

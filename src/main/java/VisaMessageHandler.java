import org.jpos.iso.ISOComponent;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOPackager;
import org.jpos.iso.packager.GenericPackager;

import java.io.InputStream;

public class VisaMessageHandler {

    private final ISOPackager packager;

    public VisaMessageHandler(String packagerFilePath) throws ISOException {
        // Load the packager using the specified configuration file
        InputStream is = getClass().getClassLoader().getResourceAsStream(packagerFilePath);
        packager = new GenericPackager(is);
    }

    public String decodeISO8583Message(String iso8583Message) {
        try {
            // Create an ISOMsg and set the raw ISO 8583 message
            ISOMsg isoMsg = new ISOMsg();
            isoMsg.setPackager(packager);
            isoMsg.unpack(hexStringToByteArray(iso8583Message));

            // Print the decoded ISO 8583 message including all fields and subfields
            StringBuilder decodedMessage = new StringBuilder("Decoded Visa ISO 8583 Message:\n");
            // Recursive method to print fields and subfields
            printFields(isoMsg, decodedMessage, "");

            return decodedMessage.toString();
        } catch (ISOException e) {
            e.printStackTrace();
            return "Error decoding ISO 8583 message";
        }
    }

    // Recursive method to print fields and subfields
    private void printFields(ISOMsg msg, StringBuilder decodedMessage, String fieldPrefix) throws ISOException {
        for (int i = 1; i <= msg.getMaxField(); i++) {
            if (msg.hasField(i)) {
                String fieldName = fieldPrefix + i;
                ISOComponent field = msg.getComponent(i);
                if (field instanceof ISOMsg) {
                    // If the field is an ISOMsg (subfield), recursively print its contents
                    printFields((ISOMsg) field, decodedMessage, fieldName + ".");
                } else {
                    // If the field is not a subfield, print its value
                    decodedMessage.append("Field ").append(fieldName).append(": ").append(msg.getString(i)).append("\n");
                }
            }
        }
    }

    // Helper method to convert hex string to byte array
    private byte[] hexStringToByteArray(String hexString) {
        int len = hexString.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hexString.charAt(i), 16) << 4)
                    + Character.digit(hexString.charAt(i + 1), 16));
        }
        return data;
    }
}

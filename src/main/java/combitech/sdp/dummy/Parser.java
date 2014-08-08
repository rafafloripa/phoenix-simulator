package combitech.sdp.dummy;

import android.swedspot.scs.data.*;

/**
 * Created by nine on 8/6/14.
 */
public class Parser {

    public final String COMMAND_START = "start";
    public final String COMMAND_HELP = "help";
    public final String COMMAND_SUBSCRIBE = "subscribe";
    public final String COMMAND_UNSUBSCRIBE = "unsubscribe";
    public final String COMMAND_SEND = "send";
    public final String COMMAND_EXIT = "exit";

    private Server server;

    public Parser(Server server) {
        this.server = server;
    }

    public void parseCommand(String[] cmd) {
        if (cmd.length == 0) {
            System.out.println("Enter a command or type help ");
            return;
        }
        String command = cmd[0];
        String[] args = new String[cmd.length - 1];
        if (cmd.length > 1) {
            for (int i = 0; i < cmd.length - 1; i++) {
                args[i] = cmd[i + 1];
            }
        }
        if (server.getManager() == null && !cmd[0].equals(COMMAND_START)) {
            System.out
                    .println("The Server object is null please start the server with startServer");
            return;
        }
        switch (command) {
        case COMMAND_START:
            server.startServer();
            break;

        case COMMAND_HELP:
            server.printHelp();
            break;

        case COMMAND_SUBSCRIBE:
            server.subscribe(args);
            break;

        case COMMAND_UNSUBSCRIBE:
            server.unsubscribe(args);
            break;

        case COMMAND_SEND:
            SCSData sendRdyData = createSCSData(args[2], args[1]);
            server.send(Integer.parseInt(args[0]), sendRdyData);
            break;

        case COMMAND_EXIT:
            System.out.println("Shutting down server...");
            server.shutdownServer();
            break;
        default:
            System.out.println("command not recognized");
            break;
        }
    }

    private SCSData createSCSData(String type, String stringData) {

        SCSData rdyData = null;
        switch (type) {
        case "float":
            float floatData = Float.parseFloat(stringData);
            rdyData = new SCSFloat(floatData);
            break;
        case "double":
            double doubleData = Double.parseDouble(stringData);
            rdyData = new SCSDouble(doubleData);
            break;
        case "uint32":
            int uint32Data = Integer.parseInt(stringData);
            rdyData = new Uint32(uint32Data);
            break;
        case "uint16":
            int uint16Data = Integer.parseInt(stringData);
            rdyData = new Uint16(uint16Data);
            break;
        case "uint8":
            int uint8Data = Integer.parseInt(stringData);
            rdyData = new Uint8(uint8Data);
            break;
        case "short":
            short shortData = Short.parseShort(stringData);
            rdyData = new SCSShort(shortData);
            break;
        case "long":
            long longData = Long.parseLong(stringData);
            rdyData = new SCSLong(longData);
            break;
        case "integer":
            int integerData = Integer.parseInt(stringData);
            rdyData = new SCSInteger(integerData);
            break;
        default:
            System.out.println("type does not exist");
        }
        return rdyData;
    }
}

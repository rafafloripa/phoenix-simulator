package combitech.sdp.dummy;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.swedspot.automotiveapi.AutomotiveFactory;
import com.swedspot.automotiveapi.AutomotiveListener;
import com.swedspot.automotiveapi.AutomotiveManager;
import com.swedspot.automotiveapi.AutomotiveSignal;
import com.swedspot.scs.data.SCSData;
import com.swedspot.scs.data.Uint8;
import com.swedspot.sdp.observer.SDPNode;
import com.swedspot.sdp.observer.SDPNodeImpl;
import com.swedspot.vil.distraction.DriverDistractionLevel;
import com.swedspot.vil.distraction.DriverDistractionListener;
import com.swedspot.vil.policy.AutomotiveCertificate;

public class Server implements Runnable {
    private static final int DRIVER_DISTRACTION_ID = 513;
    private AutomotiveManager manager;
    private static Thread serverThread;
    private boolean isRunning;
    private volatile HashMap<String, SDPNode> nodeNetwork;
    private volatile HashMap<Integer, LinkedList<SCSData>> receivedValues;
    final private Lock lock;

    public Server() {
        nodeNetwork = new HashMap<>();
        receivedValues = new HashMap<>();
        lock = new ReentrantLock();
    }

    public void startServer() {
        manager = AutomotiveFactory.createAutomotiveManagerInstance(
                new AutomotiveCertificate(new byte[0]),
                new AutomotiveListener() {
                    @Override
                    public void timeout(int arg0) {
                        System.out.println("Timeout for: " + arg0);
                    }

                    @Override
                    public void receive(AutomotiveSignal signal) {
                        lock.lock();
                        LinkedList<SCSData> data = receivedValues.get(signal.getSignalId());
                        if (data == null) {
                            data = new LinkedList<>();
                        }
                        data.add(signal.getData());
                        receivedValues.put(signal.getSignalId(), data);
                        lock.unlock();
                        System.out.println("got: " + signal.getSignalId() + " with " + signal.getData().getDataType());
                    }

                    @Override
                    public void notAllowed(int arg0) {
                        System.out.println("Not allowed: " + arg0);
                    }
                },
                new DriverDistractionListener() {
                    @Override
                    public void levelChanged(DriverDistractionLevel level) {
                        lock.lock();
                        LinkedList<SCSData> data = receivedValues.get(DRIVER_DISTRACTION_ID);
                        if (data == null) {
                            data = new LinkedList<>();
                        }
                        data.add(new Uint8(level.getLevel()));
                        receivedValues.put(DRIVER_DISTRACTION_ID, data);
                        lock.unlock();
                        System.out
                                .println("driver distraction level changed to "
                                        + level.getLevel());
                    }
                });
    }

    public void shutdownServer() {
        isRunning = false;
        manager = null;
    }

    @Override
    public void run() {
        String[] cmd = new String[] {};
        String cmdLine = "";
        printWelcomeMessage();
        while (isRunning) {
            System.out.print("\n\nEnter command > ");
            cmdLine = System.console().readLine();
            cmd = cmdLine.split(" ");
            switch (cmd[0]) {
            case "startServer":
                startServer();
                break;

            case "help":
                printHelp();
                break;

            case "network": // AutomotiveManager has no method to tell if a node
                            // is connected or not
                System.out.println("command not supported yet");
                break;

            case "subscribe":
                if (manager != null) {
                    subscribe(cmd);
                } else {
                    System.out.println("The Server object is null please start the server with startServer");
                }
                break;

            case "addNode":
                addNode(cmd);
                break;

            case "removeNode":
                removeNode(cmd);
                break;

            case "unsubscribe":
                if (manager != null) {
                    unsubscribe(cmd);
                } else {
                    System.out.println("The Server object is null please start the server with startServer");
                }
                break;

            case "exit":
                System.out.println("Shutting down server...");
                shutdownServer();
                break;

            default:
                System.out.println("command not recognized");
                break;
            }
        }
    }

    public void removeNode(String[] cmd) {
        for (int i = 1; i < cmd.length; i++) {
            SDPNode node = nodeNetwork.get(cmd[i]);
            node.disconnect(node);
            nodeNetwork.remove(cmd[i]);
        }
    }

    public void addNode(String[] cmd) {
        for (int i = 1; i < cmd.length; i++) {
            SDPNode node = nodeNetwork.get(cmd[i]);
            if (node == null) {
                nodeNetwork.put(cmd[i], new SDPNodeImpl());
            } else {
                System.out.println("A node with that name already exists, use removeNode to remove previous node.");
            }
        }
    }

    public void printWelcomeMessage() {
        System.out.println("\n\n");
        System.out.println("Welcome to Combitech's SDP server");
        System.out.println("Type help for info on available commands");
    }

    public void unsubscribe(String[] cmd) {
        for (int i = 1; i < cmd.length; i++) {
            try {
                manager.unregister(Integer.parseInt(cmd[i]));
            } catch (NumberFormatException e) {
                System.out.println("One or more of the parameters are not valid integers: " + cmd[i]);
            }
        }
    }

    public void subscribe(String[] cmd) {
        for (int i = 1; i < cmd.length; i++) {
            try {
                manager.register(Integer.parseInt(cmd[i]));
            } catch (NumberFormatException e) {
                System.out.println("One or more of the parameters are not valid integers: " + cmd[i]);
            }
        }
    }

    public void printHelp() {
        String com = "\n\n\t";
        String des = "\t\t";
        System.out.print("\nThe commands are:");

        System.out.println(com + "startServer");
        System.out.println(des + "Starts the server listening on port 9898 and 8251");

        System.out.println(com + "subscribe <int> <int> <int> ...");
        System.out.println(des + "The server subscribes for the signals provided as the argument");

        System.out.println(com + "unsubscribe <int> <int> <int> ...");
        System.out.println(des + "The server unsubscribes for the signals provided as the argument");

        System.out.println(com + "exit");
        System.out.println(des + "The server shuts down and the program exits");
    }

    public static void main(String[] args) {
        Server dummyServer = new Server();
        dummyServer.start();

        try {
            serverThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("Good Bye!");
        System.exit(1);
    }

    public void start() {
        serverThread = new Thread(this);
        isRunning = true;
        serverThread.start();
    }

    public boolean didReceiveValue(int signalID, SCSData value) {
        lock.lock();
        boolean result = false;

        LinkedList<SCSData> data = receivedValues.get(signalID);
        if (data == null) {
            return false;
        }
        for (SCSData scsData : data) {
            if (Arrays.equals(scsData.getData(), value.getData())) {
                result = true;
            }
        }
        lock.unlock();
        return result;
    }
}

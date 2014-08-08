package combitech.sdp.dummy;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import android.swedspot.automotiveapi.AutomotiveSignal;
import android.swedspot.automotiveapi.AutomotiveSignalInfo;
import android.swedspot.scs.data.SCSData;
import android.swedspot.scs.data.Uint8;

import com.swedspot.automotiveapi.AutomotiveFactory;
import com.swedspot.automotiveapi.AutomotiveListener;
import com.swedspot.automotiveapi.AutomotiveManager;
import com.swedspot.vil.configuration.ConfigurationFactory;
import com.swedspot.vil.distraction.DriverDistractionLevel;
import com.swedspot.vil.distraction.DriverDistractionListener;
import com.swedspot.vil.policy.AutomotiveCertificate;

public class Server implements Runnable, DriverDistractionListener,
		AutomotiveListener {
	private static final int DRIVER_DISTRACTION_ID = 513;
	private static Thread serverThread;
	final private Lock lock;
	private AutomotiveManager manager;
	private boolean isRunning;
	private volatile HashMap<Integer, LinkedList<SCSData>> receivedValues;
	private Parser dummyParser;

	public Server() {
		receivedValues = new HashMap<>();
		lock = new ReentrantLock();
		dummyParser = new Parser(this);
	}

	public static void main(String[] args) throws InterruptedException {
		Server dummyServer = new Server();
		dummyServer.start();

		serverThread.join();
		System.out.println("Good Bye!");
		System.exit(1);
	}

	public void levelChanged(DriverDistractionLevel level) {
		System.out.println("driver distraction level changed to "
				+ level.getLevel());
		lock.lock();
		try {
			LinkedList<SCSData> data = receivedValues
					.get(DRIVER_DISTRACTION_ID);
			if (data == null) {
				data = new LinkedList<>();
			}
			data.add(new Uint8(level.getLevel()));
			receivedValues.put(DRIVER_DISTRACTION_ID, data);

		} finally {
			lock.unlock();
		}
	}

	public void timeout(int arg0) {
		System.out.println("Timeout for: " + arg0);
	}

	public void receive(AutomotiveSignal signal) {
		System.out.println("got: " + signal.getSignalId() + " as a "
				+ signal.getData().getDataType());
		lock.lock();
		try {
			LinkedList<SCSData> data = receivedValues.get(signal.getSignalId());
			if (data == null) {
				data = new LinkedList<>();
			}
			data.add(signal.getData());
			receivedValues.put(signal.getSignalId(), data);
		} finally {
			lock.unlock();
		}
	}

	public void notAllowed(int arg0) {
		System.out.println("Not allowed: " + arg0);
	}

	public void startServer() {
		manager = AutomotiveFactory.createAutomotiveManagerInstance(
				new AutomotiveCertificate(new byte[0]), this, this);
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
		if (System.console() == null) {
			startServer();
			while (true) {
			}
		}
		while (isRunning) {
			System.out.print("\n\nEnter command > ");
			cmdLine = System.console().readLine();
			cmd = cmdLine.split(" ");
			dummyParser.parseCommand(cmd);
		}
	}

	public void printWelcomeMessage() {
		System.out.println("\n\n");
		System.out.println("Welcome to Combitech's SDP server");
		System.out.println("Type help for info on available commands");
	}

	public void unsubscribe(String[] cmd) {
		for (int i = 0; i < cmd.length; i++) {
			try {
				manager.unregister(Integer.parseInt(cmd[i]));
			} catch (NumberFormatException e) {
				System.out
						.println("One or more of the parameters are not valid integers: "
								+ cmd[i]);
			}
		}
	}

	public void subscribe(String[] cmd) {
		for (int i = 0; i < cmd.length; i++) {
			try {
				manager.register(Integer.parseInt(cmd[i]));
			} catch (NumberFormatException e) {
				System.out
						.println("One or more of the parameters are not valid integers: "
								+ cmd[i]);
			}
		}
	}

	public void printHelp() {
		String com = "\n\n\t";
		String des = "\t\t";
		System.out.print("\nThe commands are:");

		System.out.println(com + "startServer");
		System.out.println(des
				+ "Starts the server listening on port 9898 and 8251");

		System.out.println(com + "subscribe <int> <int> <int> ...");
		System.out
				.println(des
						+ "The server subscribes for the signals provided as the argument");

		System.out.println(com + "unsubscribe <int> <int> <int> ...");
		System.out
				.println(des
						+ "The server unsubscribes for the signals provided as the argument");

		System.out.println(com + "exit");
		System.out.println(des + "The server shuts down and the program exits");
	}

	public void start() {
		serverThread = new Thread(this);
		isRunning = true;
		serverThread.start();
	}

	public boolean didReceiveValue(int signalID, SCSData value) {
		lock.lock();
		boolean result = false;
		try {
			LinkedList<SCSData> data = receivedValues.get(signalID);
			if (data == null) {
				System.out.println(receivedValues.toString());
				return false;
			}
			for (SCSData scsData : data) {
				if (Arrays.equals(scsData.getData(), value.getData())) {
					result = true;
				}
			}
		} finally {
			lock.unlock();
		}
		return result;
	}

	public HashMap<Integer, LinkedList<SCSData>> getReceivedValues() {
		return receivedValues;
	}

	public AutomotiveManager getManager() {
		return manager;
	}

	public void send(int signalID, SCSData data) {
		AutomotiveSignalInfo info = new AutomotiveSignalInfo(
				ConfigurationFactory.getConfiguration().getSignalInformation(
						signalID));
		manager.send(new AutomotiveSignal(signalID, data, info.getUnit()));
	}
}

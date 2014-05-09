package combitech.sdp.dummy;

import com.swedspot.automotiveapi.AutomotiveFactory;
import com.swedspot.automotiveapi.AutomotiveListener;
import com.swedspot.automotiveapi.AutomotiveManager;
import com.swedspot.automotiveapi.AutomotiveSignal;
import com.swedspot.vil.distraction.DriverDistractionLevel;
import com.swedspot.vil.distraction.DriverDistractionListener;
import com.swedspot.vil.policy.AutomotiveCertificate;

public class Server implements Runnable {

	private AutomotiveManager manager;
	private Thread serverThread;
	private boolean isRunning;

	public Server() {

	}

	public void startServer() {
		manager = AutomotiveFactory.createAutomotiveManagerInstance(
				new AutomotiveCertificate(new byte[0]),
				new DriverDistractionListener() {
					@Override
					public void levelChanged(DriverDistractionLevel level) {
						System.out
								.println("driver distraction level changed to "
										+ level.getLevel());
					}
				});
		serverThread = new Thread(this);
		isRunning = true;
		serverThread.start();
	}

	public void shutdownServer() {
		isRunning = false;
		manager = null;
		System.exit(1);
	}

	@Override
	public void run() {
		String cmd = "";
		while (isRunning) {
			System.out.println("Enter command");
			cmd = System.console().readLine();
			switch (cmd) {
			case "network":
				System.out.println("command not supported yet");
				break;

			case "listen":
				manager.registerListener(new AutomotiveListener() {

					@Override
					public void timeout(int time) {
						System.out.println("Timeout: " + time);
					}

					@Override
					public void receive(AutomotiveSignal signal) {
						System.out.println("got signal " + signal.getSignalId()
								+ " with value " + signal.getData());
					}

					@Override
					public void notAllowed(int whatIsThis) {
						System.out.println("not allowed " + whatIsThis);
					}
				}, 257, 258, 259, 260, 261);
				break;

			case "exit":
				shutdownServer();
				break;
				
			default:
				System.out.println("command not recognized");
				break;
			}
		}
	}

	public static void main(String[] args) {
		Server dummyServer = new Server();
		dummyServer.startServer();
	}
}

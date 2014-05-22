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
				new AutomotiveListener() {
                    @Override
                    public void timeout(int arg0) {
                        System.out.println("Timeout for: "+arg0);
                    }
                    
                    @Override
                    public void receive(AutomotiveSignal arg0) {
                        System.out.println("got: "+arg0.getSignalId()+ "with "+arg0.getData().getDataType());
                    }
                    
                    @Override
                    public void notAllowed(int arg0) {
                        System.out.println("Not allowed: "+arg0);
                    }
                },
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
		String[] cmd = new String[]{};
		String cmdLine = "";
		while (isRunning) {
			System.out.println("Enter command");
			cmdLine = System.console().readLine();
			cmd = cmdLine.split(" ");
			switch (cmd[0]) {
			case "network":
				System.out.println("command not supported yet");
				break;

			case "listen":
			    for (int i = 1; i < cmd.length; i++) {
			        try {
			            manager.register(Integer.parseInt(cmd[i]));
                    } catch (NumberFormatException e) {
                        System.out.println("One or more of the parameters are not valid integers: "+cmd[i]);
                    }
                }
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

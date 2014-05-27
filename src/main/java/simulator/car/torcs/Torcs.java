package simulator.car.torcs;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

import com.sun.javafx.webkit.UIClientImpl;
import com.swedspot.automotiveapi.AutomotiveSignalId;
import com.swedspot.scs.data.SCSFloat;
import com.swedspot.scs.data.Uint8;

import simulator.BasicModule;

public class Torcs extends BasicModule implements Runnable {
	ServerSocket welcomeSocket;
	Thread torcsThread;
	boolean isStarted = false;
	int currentGear;
	float fuelLevel;
	float speed;
	
	final int GEAR_LEVEL_ID = 256;

	@Override
	public void run() {
		String signalUpdate;
		try {
			welcomeSocket = new ServerSocket(6000);
		} catch (IOException e) {
			e.printStackTrace();
		}

		Socket clientSocket;
		try {
			System.out.println("Awaiting connection");
			clientSocket = welcomeSocket.accept();
			System.out.println("Got connection");
			
			BufferedReader inFromTorcs = new BufferedReader(
					new InputStreamReader(clientSocket.getInputStream()));
			
			while (isStarted && clientSocket.isConnected()) {
				signalUpdate = inFromTorcs.readLine().trim();
				extractValues(signalUpdate);
				simulator.sendValue(AutomotiveSignalId.WHEEL_BASED_SPEED, new SCSFloat(speed));
				simulator.sendValue(AutomotiveSignalId.CURRENT_GEAR, new Uint8(currentGear));
				simulator.sendValue(AutomotiveSignalId.FUEL_LEVEL_1, new SCSFloat(fuelLevel));
				Thread.sleep(20);
			}

		} catch (Exception e1) {
			e1.printStackTrace();
		}

	}

	private void extractValues(String signalUpdate) {
		String[] values = signalUpdate.replace("FromTorcs ", "").split(";");
		try{
		fuelLevel = Float.parseFloat(values[0]);
		currentGear = Integer.parseInt(values[1]);
		speed = Float.parseFloat(values[2]);
		} catch(NumberFormatException e){
			
		}
	}

	@Override
	public void startSimulation() throws Exception {
		isStarted = true;
		torcsThread = new Thread(this);
		simulator.provideSignal(AutomotiveSignalId.WHEEL_BASED_SPEED);
		simulator.provideSignal(AutomotiveSignalId.CURRENT_GEAR);
		simulator.provideSignal(AutomotiveSignalId.FUEL_LEVEL_1);
		torcsThread.start();
	}

	@Override
	public void stopSimulation() throws Exception {
		welcomeSocket.close();
		isStarted = false;
		simulator.unprovideSignal(AutomotiveSignalId.WHEEL_BASED_SPEED);
		simulator.unprovideSignal(AutomotiveSignalId.CURRENT_GEAR);
		simulator.unprovideSignal(AutomotiveSignalId.FUEL_LEVEL_1);
		torcsThread.join();
	}

	@Override
	public void pauseSimulation() throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void resumeSimulation() throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean equals(Object o) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int hashCode() {
		// TODO Auto-generated method stub
		return 0;
	}

}

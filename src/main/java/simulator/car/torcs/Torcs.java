package simulator.car.torcs;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

import simulator.BasicModule;

import com.swedspot.automotiveapi.AutomotiveSignalId;
import com.swedspot.scs.data.SCSFloat;
import com.swedspot.scs.data.SCSLong;
import com.swedspot.scs.data.SCSShort;

public class Torcs extends BasicModule implements Runnable {
	ServerSocket welcomeSocket;
	Thread torcsThread;
	boolean isStarted = false;
	short currentGear;
	float fuelLevel;
	float fuelConsumption;
	float speed;
	long distance;

	@Override
	public void run() {
		String signalUpdate;
		while (true) {
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
					simulator.sendValue(
							AutomotiveSignalId.FMS_WHEEL_BASED_SPEED,
							new SCSFloat(speed));
					simulator.sendValue(AutomotiveSignalId.FMS_CURRENT_GEAR,
							new SCSShort(currentGear));
					simulator.sendValue(AutomotiveSignalId.FMS_FUEL_LEVEL_1,
							new SCSFloat(fuelLevel));
					simulator.sendValue(AutomotiveSignalId.FMS_FUEL_RATE,
							new SCSFloat(fuelConsumption));
					simulator
							.sendValue(
									AutomotiveSignalId.FMS_HIGH_RESOLUTION_TOTAL_VEHICLE_DISTANCE,
									new SCSLong(distance));
					Thread.sleep(20);
				}

			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}
	}

	private void extractValues(String signalUpdate) {
		String[] values = signalUpdate.replace("FromTorcs ", "").split(";");
		try {
			fuelLevel = Float.parseFloat(values[0]);
			currentGear = Short.parseShort(values[1]);
			speed = Float.parseFloat(values[2]);
			fuelConsumption = Float.parseFloat(values[3]);
			distance = Long.parseLong(values[4]);
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void startSimulation() throws Exception {
		isStarted = true;
		torcsThread = new Thread(this);
		simulator.provideSignal(AutomotiveSignalId.FMS_WHEEL_BASED_SPEED);
		simulator.provideSignal(AutomotiveSignalId.FMS_CURRENT_GEAR);
		simulator.provideSignal(AutomotiveSignalId.FMS_FUEL_LEVEL_1);
		simulator.provideSignal(AutomotiveSignalId.FMS_FUEL_RATE);
		simulator
				.provideSignal(AutomotiveSignalId.FMS_HIGH_RESOLUTION_TOTAL_VEHICLE_DISTANCE);
		torcsThread.start();
	}

	@Override
	public void stopSimulation() throws Exception {
		welcomeSocket.close();
		isStarted = false;
		simulator.unprovideSignal(AutomotiveSignalId.FMS_WHEEL_BASED_SPEED);
		simulator.unprovideSignal(AutomotiveSignalId.FMS_CURRENT_GEAR);
		simulator.unprovideSignal(AutomotiveSignalId.FMS_FUEL_LEVEL_1);
		simulator.unprovideSignal(AutomotiveSignalId.FMS_FUEL_RATE);
		simulator
				.unprovideSignal(AutomotiveSignalId.FMS_HIGH_RESOLUTION_TOTAL_VEHICLE_DISTANCE);
		torcsThread.join();
	}

	@Override
	public void pauseSimulation() throws Exception {
	}

	@Override
	public void resumeSimulation() throws Exception {
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + currentGear;
		result = prime * result + (int) (distance ^ (distance >>> 32));
		result = prime * result + Float.floatToIntBits(fuelConsumption);
		result = prime * result + Float.floatToIntBits(fuelLevel);
		result = prime * result + (isStarted ? 1231 : 1237);
		result = prime * result + Float.floatToIntBits(speed);
		result = prime * result
				+ ((torcsThread == null) ? 0 : torcsThread.hashCode());
		result = prime * result
				+ ((welcomeSocket == null) ? 0 : welcomeSocket.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Torcs other = (Torcs) obj;
		if (currentGear != other.currentGear)
			return false;
		if (distance != other.distance)
			return false;
		if (Float.floatToIntBits(fuelConsumption) != Float
				.floatToIntBits(other.fuelConsumption))
			return false;
		if (Float.floatToIntBits(fuelLevel) != Float
				.floatToIntBits(other.fuelLevel))
			return false;
		if (isStarted != other.isStarted)
			return false;
		if (Float.floatToIntBits(speed) != Float.floatToIntBits(other.speed))
			return false;
		if (torcsThread == null) {
			if (other.torcsThread != null)
				return false;
		} else if (!torcsThread.equals(other.torcsThread))
			return false;
		if (welcomeSocket == null) {
			if (other.welcomeSocket != null)
				return false;
		} else if (!welcomeSocket.equals(other.welcomeSocket))
			return false;
		return true;
	}

}

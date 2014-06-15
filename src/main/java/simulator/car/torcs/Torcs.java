package simulator.car.torcs;

import static simulator.SimulationModuleState.RUNNING;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

import simulator.BasicModule;
import simulator.SimulatorGateway;
import android.swedspot.automotiveapi.AutomotiveSignalId;
import android.swedspot.scs.data.SCSFloat;
import android.swedspot.scs.data.SCSLong;
import android.swedspot.scs.data.SCSShort;


public class Torcs extends BasicModule {
	
	public Torcs(SimulatorGateway gateway) {
		super(gateway);
	}

	ServerSocket welcomeSocket;
	short currentGear;
	float fuelLevel;
	float fuelConsumption;
	float speed;
	long distance;

	@Override
	public int[] getProvidingSingals() {
		return new int[] {
				AutomotiveSignalId.FMS_WHEEL_BASED_SPEED, 
				AutomotiveSignalId.FMS_CURRENT_GEAR, 
				AutomotiveSignalId.FMS_FUEL_LEVEL_1, 
				AutomotiveSignalId.FMS_FUEL_RATE,
				AutomotiveSignalId.FMS_HIGH_RESOLUTION_TOTAL_VEHICLE_DISTANCE}; 
	}
	
	@Override
	public void run() {
		String signalUpdate;
        while (state == RUNNING) {
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

				while (state == RUNNING && clientSocket.isConnected()) {
					signalUpdate = inFromTorcs.readLine().trim();
					extractValues(signalUpdate);
					gateway.sendValue(
							AutomotiveSignalId.FMS_WHEEL_BASED_SPEED,
							new SCSFloat(speed));
					gateway.sendValue(AutomotiveSignalId.FMS_CURRENT_GEAR,
							new SCSShort(currentGear));
					gateway.sendValue(AutomotiveSignalId.FMS_FUEL_LEVEL_1,
							new SCSFloat(fuelLevel));
					gateway.sendValue(AutomotiveSignalId.FMS_FUEL_RATE,
							new SCSFloat(fuelConsumption));
					gateway
							.sendValue(
									AutomotiveSignalId.FMS_HIGH_RESOLUTION_TOTAL_VEHICLE_DISTANCE,
									new SCSLong(distance));
					Thread.sleep(30);
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
	public void stopModule() {
		try {
			welcomeSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		super.stopModule();
	}
}

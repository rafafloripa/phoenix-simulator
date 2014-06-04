package simulator.filereplayer.openxcreplayer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;

import simulator.BasicModule;

import com.swedspot.automotiveapi.AutomotiveSignalId;
import com.swedspot.scs.data.SCSData;
import com.swedspot.scs.data.SCSFloat;
import com.swedspot.scs.data.SCSShort;
import com.swedspot.scs.data.Uint8;

public class OpenXCReplayer extends BasicModule implements Runnable {

	private BufferedReader br;
	private File file;
	private LinkedList<Integer> availableIDs;
	private boolean isRunning = false;
	private boolean isPaused = false;
	private Thread storageFileReaderThread;

	private long previousTimestamp;
	private long currentTimestamp;
	private long timeDiff;
	private int previousNanos;
	private int currentNanos;
	private int nanosDiff;

	public OpenXCReplayer() {
		availableIDs = new LinkedList<>();
	}

	public boolean readFile(File file) throws IOException {
		this.file = file;
		br = new BufferedReader(new FileReader(file));
		String newLine = "";
		int id;
		while ((newLine = br.readLine()) != null) {
			id = convertNameToID(extractName(newLine));
			if (!availableIDs.contains(id) && id != -1) {
				availableIDs.add(id);
			}
		}
		br.close();
		return !availableIDs.isEmpty();
	}

	@Override
	public void startSimulation() {
		if (!availableIDs.isEmpty()) {
			try {
				br = new BufferedReader(new FileReader(file));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			setFirstTimestamp();
			isRunning = true;
			storageFileReaderThread = new Thread(this);
			storageFileReaderThread.start();
		}
	}

	private void setFirstTimestamp() {
		try {
			br.mark(180);
			String timestamp = extractTimestamp(br.readLine());
			if (timestamp.contains(".")) {
				currentNanos = Integer.parseInt(timestamp.substring(timestamp
						.indexOf(".") + 2));
				currentTimestamp = Long.parseLong(timestamp.substring(0,
						timestamp.indexOf(".")));
			} else {
				currentNanos = 0;
				currentTimestamp = Long.parseLong(timestamp);
			}
			br.reset();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void stopSimulation() {
		isRunning = false;
		try {
			storageFileReaderThread.join();
			br.close();
		} catch (InterruptedException | IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		String newLine = "";
		String openXCName = "";
		int id;
		try {
			while (isRunning && (newLine = br.readLine()) != null) {
				newLine = newLine.trim();
				openXCName = extractName(newLine);
				id = convertNameToID(openXCName);
				if (availableIDs.contains(id)) {
					updateTimeDiffs(extractTimestamp(newLine));
					if (timeDiff > 0 || nanosDiff > 0) {
						Thread.sleep(timeDiff, nanosDiff);
					}

					SCSData data = convertToSCSData(extractName(newLine),
							extractValue(newLine));
					System.out.println("sending id: "+id+" with value: "+extractValue(newLine));
					simulator.sendValue(id, data);
				}
				while (isPaused) {
					Thread.sleep(1000);
				}
			}
		} catch (InterruptedException | IOException e) {
			e.printStackTrace();
		}
	}

	private void updateTimeDiffs(String timestamp) {
		previousNanos = currentNanos;
		previousTimestamp = currentTimestamp;
		if (timestamp.contains(".")) {
			currentNanos = Integer.parseInt(timestamp.substring(timestamp
					.indexOf(".") + 2));
			currentTimestamp = Long.parseLong(timestamp.substring(0,
					timestamp.indexOf(".")));
		} else {
			currentNanos = 0;
			currentTimestamp = Long.parseLong(timestamp);
		}

		nanosDiff = Math.max(currentNanos - previousNanos, 0);
		timeDiff = Math.max(currentTimestamp - previousTimestamp, 0);
	}

	private int convertNameToID(String name) {
		switch (name) {
		case "fuel_level":
			return AutomotiveSignalId.FMS_FUEL_LEVEL_1;
		case "engine_speed":
			return AutomotiveSignalId.FMS_ENGINE_SPEED;
		case "transmission_gear_position":
			return AutomotiveSignalId.FMS_CURRENT_GEAR;
		case "vehicle_speed":
			return AutomotiveSignalId.FMS_WHEEL_BASED_SPEED;
		case "parking_brake_status":
			return AutomotiveSignalId.FMS_PARKING_BRAKE;
		default:
			break;
		}
		return -1;
	}

	private SCSData convertToSCSData(String name, String value) {
		switch (name) {
		case "fuel_level":
			return new SCSFloat(Float.parseFloat(value));
		case "engine_speed":
			return new SCSFloat(Float.parseFloat(value));
		case "transmission_gear_position":
			switch (value) {
			case "first":
				return new SCSShort((short) 1);
			case "second":
				return new SCSShort((short) 2);
			case "third":
				return new SCSShort((short) 3);
			case "fourth":
				return new SCSShort((short) 4);
			case "fith":
				return new SCSShort((short) 5);
			case "sixth":
				return new SCSShort((short) 6);
			case "reverse":
				return new SCSShort((short) -1);
			case "neutral":
				return new SCSShort((short) 0);
			}

		case "vehicle_speed":
			return new SCSFloat(Float.parseFloat(value));
		case "parking_brake_status":
			switch (value) {
			case "true":
				new Uint8(1);
			case "false":
				new Uint8(0);
			}
		default:
			break;
		}
		return null;
	}

	public String extractTimestamp(String string) {
		String cmd = "timestamp:";
		String replacedString = string.replaceAll("[[\"] [ ]]", "");
		int pointOfIntrest = replacedString.indexOf(cmd) + cmd.length();
		int endpoint = replacedString.indexOf(",", pointOfIntrest);
		if (endpoint == -1) {
			endpoint = replacedString.indexOf("}", pointOfIntrest);
		}
		return replacedString.substring(pointOfIntrest, endpoint);
	}

	public String extractName(String string) {
		String cmd = "name:";
		String replacedString = string.replaceAll("[[\"] [ ]]", "");
		int pointOfIntrest = replacedString.indexOf(cmd) + cmd.length();
		int endpoint = replacedString.indexOf(",", pointOfIntrest);
		if (endpoint == -1) {
			endpoint = replacedString.indexOf("}", pointOfIntrest);
		}
		return replacedString.substring(pointOfIntrest, endpoint);
	}

	public String extractValue(String string) {
		String cmd = "value:";
		String replacedString = string.replaceAll("[[\"] [ ]]", "");
		int pointOfIntrest = replacedString.indexOf(cmd) + cmd.length();
		int endpoint = replacedString.indexOf(",", pointOfIntrest);
		if (endpoint == -1) {
			endpoint = replacedString.indexOf("}", pointOfIntrest);
		}
		return replacedString.substring(pointOfIntrest, endpoint);
	}

	@Override
	public void pauseSimulation() throws Exception {
		isPaused = true;
	}

	@Override
	public void resumeSimulation() throws Exception {
		isPaused = false;
	}

	public int[] getAvailableIDs() {
		int[] tmp = new int[availableIDs.size()];
		for (int i = 0; i < tmp.length; i++) {
			tmp[i] = availableIDs.get(i);
		}
		return tmp;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o instanceof File) {
			OpenXCReplayer tmp = (OpenXCReplayer) o;
			return Arrays.equals(getAvailableIDs(), tmp.getAvailableIDs());
		}
		return false;
	}

	@Override
	public int hashCode() {
		int hash = 1;
		for (Integer i : availableIDs) {
			hash *= (10 + i);
		}
		return hash;
	}
}

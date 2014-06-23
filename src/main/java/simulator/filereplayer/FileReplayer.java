package simulator.filereplayer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;

import simulator.BasicModule;
import simulator.SimulatorGateway;
import android.swedspot.scs.data.Uint32;
import static simulator.SimulationModuleState.*;

public class FileReplayer extends BasicModule {

	private BufferedReader br;
	private ArrayList<ReplayerDataRow> dataValues;
	private LinkedList<Integer> providedIDs;
	private int index;
	private long timeDiff;
	private long previousTimestamp;
	private ReplayerDataRow current;
	private int data;

	public FileReplayer(SimulatorGateway gateway) {
		super(gateway);
		dataValues = new ArrayList<>();
		providedIDs = new LinkedList<>();
	}

	public boolean readFile(File file) throws IOException {
		br = new BufferedReader(new FileReader(file));
		System.err.println("file exists: " + file.exists());
		String newLine = "";
		int id;
		long timestamp;
		String[] data;
		while ((newLine = br.readLine()) != null) {
			data = newLine.split(",");
			id = Integer.parseInt(extractData(data[1]));
			if (!providedIDs.contains(id)) {
				providedIDs.add(id);
				gateway.provideSignal(id);
			}
			timestamp = Long.parseLong(extractData(data[0]));
			dataValues.add(new ReplayerDataRow(id, extractData(data[2]),
					timestamp));
		}
		br.close();
		return !dataValues.isEmpty();
	}

	public static String extractData(String input) {
		String trimmedData = input.replaceAll("[[{}]]", "");
		return trimmedData.substring(trimmedData.indexOf(":") + 1,
				trimmedData.length());
	}

	@Override
	public void startModule() {
		super.startModule();
		if (!dataValues.isEmpty()) {
			index = 0;
			previousTimestamp = dataValues.get(0).getTimestamp();
		}
	}

	@Override
	public void stopModule() {
		super.stopModule();
		index = 0;
	}

	@Override
	public void run() {
		try {
			while (state == RUNNING) {
				current = dataValues.get(index);
				timeDiff = current.getTimestamp() - previousTimestamp;
				if (timeDiff > 0) {
					// System.out.println("index: " + index + ", sleeping for:"
					// + timeDiff);
					Thread.sleep(timeDiff);
				}
				// TODO are all values going to be integers? Fix if not!
				data = Integer.parseInt(current.getData());
				gateway.sendValue(current.getSignalID(), new Uint32(data));
				System.err.println("Sent: signalID: " + current.getSignalID()
						+ ", data: " + current.getData());
				previousTimestamp = current.getTimestamp();
				index++;
				if (index >= dataValues.size()) {
					return;
				}
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public ArrayList<ReplayerDataRow> getDataValues() {
		return dataValues;
	}

	public LinkedList<Integer> getProvidedIDs() {
		return providedIDs;
	}

	@Override
	public int[] getProvidingSingals() {
		int[] tmp = new int[providedIDs.size()];
		for (int i = 0; i < providedIDs.size(); i++) {
			tmp[i] = providedIDs.get(i);
		}
		return tmp;
	}

}

package simulator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;

import com.swedspot.scs.SCS;
import com.swedspot.scs.data.Uint32;

public class StorageFileReader implements ModuleInterface {
	
	private Simulator					simulator;
	private SCS							node;
	private BufferedReader				br;
	private ArrayList<ReplayerDataRow>	dataValues;
	private LinkedList<Integer>			providedIDs;
	private int							index;
	private long						timeDiff;
	private long						previousTimestamp;
	private boolean						isRunning	= false;
	ReplayerDataRow 					current;
	int 								data;
	
	public StorageFileReader(Simulator simu) {
		simulator = simu;
		node = simulator.getNode();
		dataValues = new ArrayList<>();
		providedIDs = new LinkedList<>();
	}
	
	public boolean readFile(File file) throws IOException {
		br = new BufferedReader(new FileReader(file));
		System.err.println("file exists: "+file.exists());
		String newLine = "";
		int id;
		long timestamp;
		String[] data;
		while ((newLine = br.readLine()) != null) {
			data = newLine.split(",");
			id = Integer.parseInt(extractData(data[1]));
			if (!providedIDs.contains(id)) {
				providedIDs.add(id);
				node.provide(id);
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
	
	/***
	 * 
	 * called
	 */
	@Override
	public void startSimulation() {
		if (!dataValues.isEmpty()) {
			index = 0;
			previousTimestamp = dataValues.get(0).getTimestamp();
			isRunning = true;
		}
	}
	
	@Override
	public void stopSimulation() {
		isRunning = false;
		index = 0;
	}
	
	@Override
	public void run() {
		try {
			while (isRunning) {
				current = dataValues.get(index);
				timeDiff = current.getTimestamp() - previousTimestamp;
				if(timeDiff > 0){
//					System.out.println("index: " + index + ", sleeping for:" + timeDiff);
					Thread.sleep(timeDiff);
				}
				// TODO are all values going to be integers? Fix if not!
				data = Integer.parseInt(current.getData());
				node.send(current.getSignalID(), new Uint32(data));
//				System.err.println("Sent: signalID: "+current.getSignalID() + ", data: "+current.getData());
				previousTimestamp = current.getTimestamp();
				index++;
				if (index >= dataValues.size()) {
					stopSimulation();
					break;
				}
			}
			Thread.sleep(100);
			run();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}

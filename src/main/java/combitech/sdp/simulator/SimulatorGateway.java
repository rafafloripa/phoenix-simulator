package combitech.sdp.simulator;

import android.swedspot.scs.SCS;
import android.swedspot.scs.SCSFactory;
import android.swedspot.scs.data.SCSData;
import android.swedspot.sdp.SDPFactory;
import android.swedspot.sdp.configuration.Configuration;
import android.swedspot.sdp.observer.SDPConnectionListener;
import android.swedspot.sdp.observer.SDPDataListener;
import android.swedspot.sdp.observer.SDPGatewayNode;
import android.swedspot.sdp.observer.SDPNode;
import android.swedspot.sdp.routing.SDPNodeEthAddress;
import com.swedspot.vil.configuration.ConfigurationFactory;
import com.swedspot.vil.configuration.VilConstants;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.concurrent.locks.ReentrantLock;

public class SimulatorGateway {
	private static final int DRIVER_DISTRACTION_LEVEL_DATA_ID = 513;
	private static final int HARDWARE_KEY_ID = 514;

	private HashMap<Integer, Integer> provideMap;
	private HashMap<Integer, SCSData> lastValueSent;
	private LinkedList<SDPGatewayNode> simulatorGateways;
	private LinkedList<SCS> driverDistractionNodes;
	private LinkedList<SCS> hardwareKeyNodes;
	private LinkedList<SCS> signalNodes;
	private ReentrantLock lock;

	public SimulatorGateway() {
		simulatorGateways = new LinkedList<>();
		driverDistractionNodes = new LinkedList<>();
		hardwareKeyNodes = new LinkedList<>();
		signalNodes = new LinkedList<>();
		provideMap = new HashMap<>();
		lock = new ReentrantLock();
		lastValueSent = new HashMap<>();
	}

	public boolean addAndInitiateNode(String address, int port,
			SDPConnectionListener connectionListener) {
		SDPNode tmpNode = SDPFactory.createNodeInstance();
		SDPGatewayNode simulatorGateway = SDPFactory
				.createGatewayClientInstance();
		simulatorGateway.init(new SDPNodeEthAddress(address, port), tmpNode);
		simulatorGateway.addDataListener(new SDPDataListener() {
			@Override
			public byte[] request(int signalID) {
				byte[] data = lastValueSent.get(signalID).getData();
				return data != null ? data : new byte[] { 0 };
			}

			@Override
			public void receive(int signalID, byte[] data) {
				// TODO deal with values received from the node
			}
		});
		simulatorGateway.start();
		simulatorGateway.setConnectionListener(connectionListener);

		simulatorGateways.add(simulatorGateway);
		Configuration conf = ConfigurationFactory.getConfiguration();

		if (port == VilConstants.DRIVER_DISTRACTION_PORT) {
			System.out.println("added driver distraction node");
			driverDistractionNodes.add(SCSFactory.createSCSInstance(tmpNode,
					conf));
		} else if (port == VilConstants.HARDWARE_BUTTON_PORT) {
			System.out.println("added driver hardware key node");
			hardwareKeyNodes.add(SCSFactory.createSCSInstance(tmpNode, conf));
		} else {
			System.out.println("added signal node");
			signalNodes.add(SCSFactory.createSCSInstance(tmpNode, conf));
		}
		return true;
	}

	/**
	 * The Simulator provides on all signalNodes a signal id
	 *
	 * @param signalID
	 */
	public void provideSignal(int signalID) {
		lock.lock();
		try {
			if (!provideMap.containsKey(signalID)) {
				provideMap.put(signalID, 1);
				if (signalID == DRIVER_DISTRACTION_LEVEL_DATA_ID) {
					for (SCS node : driverDistractionNodes)
						node.provide(signalID);
				} else if (signalID == HARDWARE_KEY_ID) {
					for (SCS node : hardwareKeyNodes)
						node.provide(signalID);
				} else {
					for (SCS node : signalNodes)
						node.provide(signalID);
				}
			} else {
				provideMap.put(signalID, provideMap.get(signalID) + 1);
			}
		} finally {
			lock.unlock();
		}
	}

	/**
	 * The Simulator unprovides on all signalNodes a signal id
	 *
	 * @param signalID
	 */
	public void unprovideSignal(int signalID) {
		lock.lock();
		try {
			if (provideMap.containsKey(signalID)) {
				provideMap.put(signalID, provideMap.get(signalID) - 1);
				if (provideMap.get(signalID) == 0) {
					if (signalID == DRIVER_DISTRACTION_LEVEL_DATA_ID) {
						for (SCS node : driverDistractionNodes)
							node.unprovide(signalID);
					} else if (signalID == HARDWARE_KEY_ID) {
						for (SCS node : hardwareKeyNodes)
							node.unprovide(signalID);
					} else {
						for (SCS node : signalNodes)
							node.unprovide(signalID);
					}
					;
					provideMap.remove(signalID);
				}
			}
		} finally {
			lock.unlock();
		}
	}

	/**
	 * Unprovides all signals for all current signalNodes but retains the IDs
	 */
	public void unprovideAll() {
		lock.lock();
		try {
			for (SCS node : signalNodes) {
				for (Entry<Integer, Integer> entry : provideMap.entrySet()) {
					node.unprovide(entry.getKey());
				}
			}
		} finally {
			lock.unlock();
		}
	}

	/**
	 * Provides all signals that have not been removed from the simulator for
	 * all current signalNodes
	 */
	public void provideAll() {
		lock.lock();
		try {
			for (SCS node : signalNodes) {
				for (Entry<Integer, Integer> entry : provideMap.entrySet()) {
					node.provide(entry.getKey());
				}
			}
		} finally {
			lock.unlock();
		}
	}

	/**
	 * Sends a single signal to all connected signalNodes It is important that
	 * the SCSData match the signal id
	 *
	 * @param signalID
	 * @param data
	 */
	public void sendValue(int signalID, SCSData data) {
		lock.lock();
		try {
			if (signalID == DRIVER_DISTRACTION_LEVEL_DATA_ID) {
				for (SCS node : driverDistractionNodes)
					node.send(signalID, data);
				System.out.println("sent data on driverDistractionNodes");
			} else if (signalID == HARDWARE_KEY_ID) {
				for (SCS node : hardwareKeyNodes)
					node.send(signalID, data);
				System.out.println("sent data on hardwareNodes");
			} else {
				for (SCS node : signalNodes)
					node.send(signalID, data);
				System.out.println("sent data on signalNodes");
			}
			lastValueSent.put(signalID, data);
		} finally {
			lock.unlock();
		}

	}

	public void disconnectSimulator() {
		lock.lock();
		try {
			for (SDPGatewayNode simulatorGateway : simulatorGateways) {
				simulatorGateway.stop();
			}
			simulatorGateways.clear();
		} finally {
			lock.unlock();
		}
	}
}

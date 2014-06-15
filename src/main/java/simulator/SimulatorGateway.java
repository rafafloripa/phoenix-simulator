package simulator;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.locks.ReentrantLock;

import android.swedspot.scs.SCS;
import android.swedspot.scs.SCSFactory;
import android.swedspot.scs.data.SCSData;
import android.swedspot.sdp.SDPFactory;
import android.swedspot.sdp.observer.SDPGatewayNode;
import android.swedspot.sdp.observer.SDPNode;
import android.swedspot.sdp.routing.SDPNodeEthAddress;

public class SimulatorGateway {
	private HashMap<Integer, Integer> provideMap;
	private LinkedList<SDPGatewayNode> simulatorGateways;
	private LinkedList<SCS> nodes;
	private ReentrantLock lock;

	public SimulatorGateway() {
		simulatorGateways = new LinkedList<>();
		nodes = new LinkedList<>();
		provideMap = new HashMap<>();
		lock = new ReentrantLock();
	}

	public boolean addAndInitiateNode(String adress, int port) {
		SDPNode tmpNode = SDPFactory.createNodeInstance();
		SDPGatewayNode simulatorGateway = SDPFactory
				.createGatewayClientInstance();
		simulatorGateway.init(new SDPNodeEthAddress(adress, port), tmpNode);
		simulatorGateway.start();

		simulatorGateways.add(simulatorGateway);
		nodes.add(SCSFactory.createSCSInstance(tmpNode));
	      try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
		return true;
	}

	/**
	 * The Simulator provides on all nodes a signal id
	 * 
	 * @param signalID
	 */
	public void provideSignal(int signalID) {
		lock.lock();
		try {
			if (!provideMap.containsKey(signalID)) {
				provideMap.put(signalID, 1);
				for (SCS node : nodes)
					node.provide(signalID);
			} else {
				provideMap.put(signalID, provideMap.get(signalID) + 1);
			}
		} finally {
			lock.unlock();
		}
	}

	/**
	 * The Simulator unprovides on all nodes a signal id
	 * 
	 * @param signalID
	 */
	public void unprovideSignal(int signalID) {
		lock.lock();
		try {
			if (provideMap.containsKey(signalID)) {
				provideMap.put(signalID, provideMap.get(signalID) - 1);
				if (provideMap.get(signalID) == 0) {
					for (SCS node : nodes)
						node.unprovide(signalID);
					provideMap.remove(signalID);
				}
			}
		} finally {
			lock.unlock();
		}
	}

	/**
	 * Sends a single signal to all connected nodes It is important that the
	 * SCSData match the signal id
	 * 
	 * 
	 * @param signalID
	 * @param data
	 */
	public void sendValue(int signalID, SCSData data) {
		for (SCS node : nodes)
			node.send(signalID, data);
	}

	public void disconnectSimulator() {
		for (SDPGatewayNode simulatorGateway : simulatorGateways) {
			for (SDPNode node : simulatorGateway.connections()) {
				simulatorGateway.disconnect(node);
			}
			simulatorGateway.stop();
		}
		simulatorGateways.clear();
	}
}

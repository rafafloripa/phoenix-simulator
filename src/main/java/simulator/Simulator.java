package simulator;

import java.util.ArrayList;
import java.util.LinkedList;

import android.swedspot.scs.SCS;
import android.swedspot.scs.SCSFactory;
import android.swedspot.scs.data.SCSData;
import android.swedspot.sdp.SDPFactory;
import android.swedspot.sdp.observer.SDPGatewayNode;
import android.swedspot.sdp.observer.SDPNode;
import android.swedspot.sdp.routing.SDPNodeEthAddress;

public class Simulator {
	private LinkedList<SDPGatewayNode> simulatorGateways;
	private LinkedList<SCS> nodes;
	private ArrayList<BasicModule> availableModules = new ArrayList<BasicModule>();
	private SimulationState simulationState = SimulationState.STOPPED;

	public Simulator() {
		simulatorGateways = new LinkedList<>();
		nodes = new LinkedList<>();
	}

	/**
	 * The simulator creates a node and tries to connect it to the desired
	 * destination
	 * 
	 * @param adress
	 * @param port
	 * @return true if a connection was made, false otherwise
	 */
	public boolean addAndInitiateNode(String adress, int port) {
		SDPNode tmpNode = SDPFactory.createNodeInstance();
		SDPGatewayNode simulatorGateway = SDPFactory
				.createGatewayClientInstance();
		simulatorGateway.init(new SDPNodeEthAddress(adress, port), tmpNode);
		simulatorGateway.start();
		
		int maxNumberOfTries = 5;
		try {
			for (int i = 0; i < maxNumberOfTries; i++) {
				if (!simulatorGateway.connections().isEmpty()) {
					simulatorGateways.add(simulatorGateway);
					nodes.add(SCSFactory.createSCSInstance(tmpNode));
					simulationState = SimulationState.INITIALIZED;
					return true;
				}
				Thread.sleep(250);	
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}	
		
		simulatorGateway.stop();
		return false;
	}

	/**
	 * The Simulator provides on all nodes a signal id
	 * 
	 * @param signalID
	 */
	public void provideSignal(int signalID) {
		for (SCS node : nodes)
			node.provide(signalID);
	}

	/**
	 * The Simulator unprovides on all nodes a signal id
	 * 
	 * @param signalID
	 */
	public void unprovideSignal(int signalID) {
		for (SCS node : nodes)
			node.unprovide(signalID);
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

	/**
	 * The Simulator goes into the state RUNNING and all modules will start
	 * their operations
	 * 
	 * @throws Exception
	 */
	public void startSimulation() throws Exception {
		for (BasicModule module : availableModules)
			module.startSimulation();
		simulationState = SimulationState.RUNNING;
	}

	/**
	 * The Simulator goes into the state STOP and all modules will stop their
	 * operations and any further use will require the Simulator to go into
	 * RUNNING state via startSimulation method
	 * 
	 * @throws Exception
	 */
	public void stopSimulation() throws Exception {
		for (BasicModule module : availableModules)
			module.stopSimulation();
		simulationState = SimulationState.STOPPED;
	}

	/**
	 * The Simulator disconnects itself to any and all nodes it knows about and
	 * then goes into the state STOPPED
	 * 
	 */
	public void disconnectSimulator() {
		for (SDPGatewayNode simulatorGateway : simulatorGateways) {
			for (SDPNode node : simulatorGateway.connections()) {
				simulatorGateway.disconnect(node);
			}
			simulatorGateway.stop();
		}
		simulatorGateways.clear();
		simulationState = SimulationState.STOPPED;
	}

	/**
	 * The Simulator goes into the state PAUSE and all modules will pause in
	 * their operations
	 * 
	 * @throws Exception
	 */
	public void pauseSimulation() throws Exception {
		for (BasicModule module : availableModules)
			module.pauseSimulation();
		simulationState = SimulationState.PAUSED;
	}

	/**
	 * The Simulator goes into the state RUNNING and all modules resume their
	 * operations.
	 * 
	 * @throws Exception
	 */
	public void resumeSimulation() throws Exception {
		for (BasicModule module : availableModules)
			module.resumeSimulation();
		simulationState = SimulationState.RUNNING;
	}

	/**
	 * A list containing the nodes responsible for keeping the connection with
	 * the rest of the SDP network
	 * 
	 * @return A list of all SDPGatwayNodes
	 */
	public LinkedList<SDPGatewayNode> getGatewaysNodes() {
		return simulatorGateways;
	}

	/**
	 * Add a BasicModule to the list of modules. This operation should only be
	 * done when the Simulator is STOPPED state via the stopSimulation method
	 * 
	 * @param simulationModule
	 */
	public void addSimulationModule(BasicModule simulationModule) {
		availableModules.add(simulationModule);
		simulationModule.setSimulator(this);
	}

	/**
	 * @return the current state of the Simulator
	 */
	public SimulationState getSimulationState() {
		return simulationState;
	}

	/**
	 * Sets the simulation state but the change is not reflected from other
	 * parts of the simulation, should only be used for testing purposes
	 * 
	 * @param simulationState
	 */
	public void setSimulationState(SimulationState simulationState) {
		this.simulationState = simulationState;
	}

	/**
	 * Tries to remove a module from the list of modules. This operation should
	 * only be done when the Simulator is STOPPED state via the stopSimulation
	 * method
	 * 
	 * @param module
	 * @return true of the module was removed, false otherwise
	 */
	public boolean removeModule(BasicModule module) {
		return availableModules.remove(module);
	}

}

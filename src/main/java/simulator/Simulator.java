package simulator;

import java.util.ArrayList;
import java.util.LinkedList;

import com.swedspot.scs.SCS;
import com.swedspot.scs.SCSFactory;
import com.swedspot.scs.data.Uint32;
import com.swedspot.sdp.SDPFactory;
import com.swedspot.sdp.observer.SDPGatewayNode;
import com.swedspot.sdp.observer.SDPNode;
import com.swedspot.sdp.routing.SDPNodeEthAddress;

public class Simulator {
	private LinkedList<SDPGatewayNode> simulatorGateways;
	private LinkedList<SCS> nodes;
	private ArrayList<BasicModule> availableModules = new ArrayList<BasicModule>();
	private SimulationState simulationState = SimulationState.STOPPED;

	public Simulator() {
		simulatorGateways = new LinkedList<>();
		nodes = new LinkedList<>();
	}

	public void addAndInitiateNode(String adress, int port) {
		SDPNode tmpNode = SDPFactory.createNodeInstance();
		SDPGatewayNode simulatorGateway = SDPFactory
				.createGatewayClientInstance();
		simulatorGateway.init(new SDPNodeEthAddress(adress, port), tmpNode);
		simulatorGateway.start();
		simulatorGateways.add(simulatorGateway);
		nodes.add(SCSFactory.createSCSInstance(tmpNode));
		simulationState = SimulationState.INITIALIZED;
	}

	public void setupSignal(int signalID, int startingValue)
			throws InterruptedException {
		for (SCS node : nodes)
			node.provide(signalID);
	}

	public void changeValue(int signalID, int newValue) {
		for (SCS node : nodes)
			node.send(signalID, new Uint32(newValue));
	}

	public void startSimulation() throws Exception {
		for (BasicModule module : availableModules)
			module.startSimulation();
		simulationState = SimulationState.RUNNING;
	}

	public void stopSimulation() throws Exception {
		for (BasicModule module : availableModules)
			module.startSimulation();

		for (SDPGatewayNode simulatorGateway : simulatorGateways)
			simulatorGateway.stop();
		simulationState = SimulationState.STOPPED;

	}

	public void pauseSimulation() throws Exception {
		for (BasicModule module : availableModules)
			module.pauseSimulation();
		simulationState = SimulationState.PAUSED;
	}

	public void resumeSimulation() throws Exception {
		for (BasicModule module : availableModules)
			module.resumeSimulation();
		simulationState = SimulationState.RUNNING;
	}

	public LinkedList<SCS> getNodes() {
		return nodes;
	}

	public LinkedList<SDPGatewayNode> getGatewaysNodes() {
		return simulatorGateways;
	}

	public void addSimulationModule(BasicModule simulationModule) {
		availableModules.add(simulationModule);
		simulationModule.setSimulator(this);
	}

	public SimulationState getSimulationState() {
		return simulationState;
	}

	public void setSimulationState(SimulationState simulationState) {
		this.simulationState = simulationState;
	}

}

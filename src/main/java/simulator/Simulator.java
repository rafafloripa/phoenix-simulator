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

	public boolean addAndInitiateNode(String adress, int port) {
		SDPNode tmpNode = SDPFactory.createNodeInstance();
		SDPGatewayNode simulatorGateway = SDPFactory
				.createGatewayClientInstance();
		simulatorGateway.init(new SDPNodeEthAddress(adress, port), tmpNode);
		simulatorGateway.start();
		try {
			Thread.sleep(25);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		if (!simulatorGateway.connections().isEmpty()) {
			simulatorGateways.add(simulatorGateway);
			nodes.add(SCSFactory.createSCSInstance(tmpNode));
			simulationState = SimulationState.INITIALIZED;
			return true;
		}
		simulatorGateway.stop();
		return false;
	}

	public void stopAllNodes() {
		for (SDPGatewayNode simulatorGateway : simulatorGateways) {
			for (SDPNode node : simulatorGateway.connections()) {
				simulatorGateway.disconnect(node);
			}
			simulatorGateway.stop();
		}
		simulatorGateways.clear();
	}

	public void provideSignal(int signalID) {
		for (SCS node : nodes)
			node.provide(signalID);
	}

	public void unprovideSignal(int signalID) {
		for (SCS node : nodes)
			node.unprovide(signalID);
	}

	public void sendValue(int signalID, SCSData data) {
		for (SCS node : nodes)
			node.send(signalID, data);
	}

	public void startSimulation() throws Exception {
		for (BasicModule module : availableModules)
			module.startSimulation();
		simulationState = SimulationState.RUNNING;
	}

	public void stopSimulation() throws Exception {
		for (BasicModule module : availableModules)
			module.stopSimulation();
	}

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

	public void removeModule(BasicModule module) {
		availableModules.remove(module);
	}

}

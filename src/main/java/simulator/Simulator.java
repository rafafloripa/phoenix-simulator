package simulator;

import java.util.ArrayList;

import com.swedspot.scs.SCS;
import com.swedspot.scs.SCSFactory;
import com.swedspot.scs.data.Uint32;
import com.swedspot.sdp.SDPFactory;
import com.swedspot.sdp.observer.SDPGatewayNode;
import com.swedspot.sdp.observer.SDPNode;
import com.swedspot.sdp.routing.SDPNodeEthAddress;

public class Simulator {
    private SDPNode simulatorNode;
    private SDPGatewayNode simulatorGateway;
    private SCS node;
    private ArrayList<BasicModule> availableModules = new ArrayList<BasicModule>();
    private SimulationState simulationState = SimulationState.STOPPED;

    public void init() {
        simulatorNode = SDPFactory.createNodeInstance();
        simulatorGateway = SDPFactory.createGatewayClientInstance();
        simulatorGateway.init(new SDPNodeEthAddress("localhost", 8126), simulatorNode);
        simulatorGateway.start();
        node = SCSFactory.createSCSInstance(simulatorNode);
        simulationState = SimulationState.INITIALIZED;
    }

    public void setupSignal(int signalID, int startingValue) throws InterruptedException {
        node.provide(signalID);
    }

    public void changeValue(int signalID, int newValue) {
        node.send(signalID, new Uint32(newValue));
    }

    public void startSimulation() throws Exception
    {
        for (BasicModule module : availableModules)
            module.startSimulation();
        simulationState = SimulationState.RUNNING;
    }

    public void stopSimulation() throws Exception {
        for (BasicModule module : availableModules)
            module.startSimulation();

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

    public SCS getNode()
    {
        return node;
    }

    public void addSimulationModule(BasicModule simulationModule)
    {
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

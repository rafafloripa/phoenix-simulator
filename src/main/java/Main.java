import simulator.Simulator;
import simulator.car.torcs.Torcs;


public class Main {

	public static void main(String[] args) throws Exception {
		Simulator simulator = new Simulator();
		simulator.addAndInitiateNode("127.0.0.1", 8251);
		simulator.addAndInitiateNode("127.0.0.1", 9898);
		simulator.addSimulationModule(new Torcs());
		simulator.startSimulation();
		
	}

}

import simulator.SimulatorGateway;
import simulator.car.torcs.Torcs;

public class Main {
	public static void main(String[] args) throws Exception {
		SimulatorGateway gateway = new SimulatorGateway();
		gateway.addAndInitiateNode("127.0.0.1", 8251);
		gateway.addAndInitiateNode("127.0.0.1", 9898);
		Torcs torcs = new Torcs(gateway);
		torcs.startModule();
	}
}

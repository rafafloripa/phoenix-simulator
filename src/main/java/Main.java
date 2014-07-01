import simulator.SimulatorGateway;
import simulator.car.torcs.Torcs;
import simulator.steeringwheel.gxt27.GXT27Module;

public class Main {
    public static void main(String[] args) throws Exception {
        SimulatorGateway gateway = new SimulatorGateway();
        gateway.addAndInitiateNode("127.0.0.1", 8251);
        gateway.addAndInitiateNode("127.0.0.1", 9898);
        gateway.addAndInitiateNode("127.0.0.1", 9899);
        Torcs torcs = new Torcs(gateway);
        GXT27Module gtGxt27Module = new GXT27Module(gateway);
        gtGxt27Module.startModule();
        torcs.startModule();
    }
}

import combitech.sdp.simulator.SimulatorGateway;
import combitech.sdp.simulator.car.torcs.Torcs;
import simulator.steeringwheel.gxt27.GXT27Module;

import java.util.Arrays;

public class Main {
    public static void main(String[] args) throws Exception {
        String ipaddress = "localhost";
        SimulatorGateway gateway = new SimulatorGateway();
        gateway.addAndInitiateNode(ipaddress, 8251, null, null);
        gateway.addAndInitiateNode(ipaddress, 9898, null, null);
        gateway.addAndInitiateNode(ipaddress, 9899, null, null);
        Torcs torcs = new Torcs(gateway);
        GXT27Module gtGxt27Module = new GXT27Module(gateway);
        gtGxt27Module.startModule();
        torcs.startModule();
    }
}

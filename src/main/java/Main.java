import combitech.sdp.simulator.SimulatorGateway;
import ets2.EuroTruck;

public class Main {
    public static void main(String[] args) throws Exception {
        String ipaddress = "127.0.0.1";
        SimulatorGateway gateway = new SimulatorGateway();
        gateway.addAndInitiateNode(ipaddress, 8251, null, null);
        gateway.addAndInitiateNode(ipaddress, 9898, null, null);
        gateway.addAndInitiateNode(ipaddress, 9899, null, null);
        Thread.sleep(1000);
        EuroTruck ets2 = new EuroTruck(gateway);
        for (int i : ets2.getProvidingSignals()) {
            gateway.provideSignal(i);
        }
        
        Thread.sleep(2000);
        ets2.startModule();
    }
}

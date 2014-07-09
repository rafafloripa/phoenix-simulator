import simulator.SimulatorGateway;
import simulator.car.torcs.Torcs;
import simulator.serialdevice.SerialDevice;
import simulator.steeringwheel.gxt27.GXT27Module;

public class Main {
    public static void main(String[] args) throws Exception {
        String ipaddress = "192.168.1.79";
        SimulatorGateway gateway = new SimulatorGateway();
        gateway.addAndInitiateNode(ipaddress, 8251, null);
        gateway.addAndInitiateNode(ipaddress, 9898, null);
        gateway.addAndInitiateNode(ipaddress, 9899, null);
        Torcs torcs = new Torcs(gateway);
        GXT27Module gtGxt27Module = new GXT27Module(gateway);
        //SerialDevice arduino = new SerialDevice(gateway);
        //arduino.initializeDevice("COM3");
        gtGxt27Module.startModule();
        torcs.startModule();
        //arduino.startModule();
    }
}

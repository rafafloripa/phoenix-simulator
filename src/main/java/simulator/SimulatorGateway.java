package simulator;

import android.swedspot.scs.SCS;
import android.swedspot.scs.SCSFactory;
import android.swedspot.scs.data.SCSData;
import android.swedspot.sdp.ConnectionStatus;
import android.swedspot.sdp.SDPFactory;
import android.swedspot.sdp.configuration.Configuration;
import android.swedspot.sdp.observer.SDPConnectionListener;
import android.swedspot.sdp.observer.SDPDataListener;
import android.swedspot.sdp.observer.SDPGatewayNode;
import android.swedspot.sdp.observer.SDPNode;
import android.swedspot.sdp.routing.SDPNodeEthAddress;
import com.swedspot.vil.configuration.ConfigurationFactory;
import com.swedspot.vil.configuration.VilConstants;
import com.swedspot.vil.distraction.impl.DriverDistractionImpl;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.concurrent.locks.ReentrantLock;

public class SimulatorGateway {


    private HashMap<Integer, Integer> provideMap;
    private HashMap<Integer, SCSData> lastValueSent;
    private LinkedList<SDPGatewayNode> signalGateways;
    private LinkedList<SDPGatewayNode> driverDistractionGateways;
    private LinkedList<SDPGatewayNode> hardwareKeyGateways;
    private LinkedList<SCS> nodes;
    private boolean connected = false;
    private ReentrantLock lock;
    private static final int DRIVER_DISTRACTION_LEVEL_DATA_ID = 513;

    public SimulatorGateway() {
        signalGateways = new LinkedList<>();
        driverDistractionGateways = new LinkedList<>();
        hardwareKeyGateways = new LinkedList<>();
        nodes = new LinkedList<>();
        provideMap = new HashMap<>();
        lock = new ReentrantLock();
        lastValueSent = new HashMap<>();
    }

    public boolean addAndInitiateNode(String address, int port) {

        SDPNode tmpNode = SDPFactory.createNodeInstance();
        SDPGatewayNode simulatorGateway = SDPFactory
                .createGatewayClientInstance();
        simulatorGateway.init(new SDPNodeEthAddress(address, port), tmpNode);
        simulatorGateway.addDataListener(new SDPDataListener() {
            @Override
            public byte[] request(int signalID) {
                return lastValueSent.get(signalID).getData();
            }

            @Override
            public void receive(int signalID, byte[] data) {
                // TODO deal with values received from the node
            }
        });
        simulatorGateway.start();
        simulatorGateway.setConnectionListener(new SDPConnectionListener() {

            @Override
            public void connectionStatusChanged(ConnectionStatus newStatus) {
                connected = newStatus == ConnectionStatus.CONNECTED ? true : false;
            }
        });

        if (port == VilConstants.API_PORT) {
            signalGateways.add(simulatorGateway);
        } else if (port == VilConstants.DRIVER_DISTRACTION_PORT) {
            driverDistractionGateways.add(simulatorGateway);
        } else if (port == VilConstants.HARDWARE_BUTTON_PORT) {
            hardwareKeyGateways.add(simulatorGateway);
        }


        Configuration conf = ConfigurationFactory.getConfiguration();
        nodes.add(SCSFactory.createSCSInstance(tmpNode, conf));
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return true;
    }

    /**
     * The Simulator provides on all nodes a signal id
     *
     * @param signalID
     */
    public void provideSignal(int signalID) {
        lock.lock();
        try {
            if (!provideMap.containsKey(signalID)) {
                provideMap.put(signalID, 1);
                for (SCS node : nodes)
                    node.provide(signalID);
            } else {
                provideMap.put(signalID, provideMap.get(signalID) + 1);
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * The Simulator unprovides on all nodes a signal id
     *
     * @param signalID
     */
    public void unprovideSignal(int signalID) {
        lock.lock();
        try {
            if (provideMap.containsKey(signalID)) {
                provideMap.put(signalID, provideMap.get(signalID) - 1);
                if (provideMap.get(signalID) == 0) {
                    for (SCS node : nodes)
                        node.unprovide(signalID);
                    provideMap.remove(signalID);
                }
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * Unprovides all signals for all current nodes but retains the IDs
     */
    public void unprovideAll() {
        lock.lock();
        try {
            for (SCS node : nodes) {
                for (Entry<Integer, Integer> entry : provideMap.entrySet()) {
                    node.unprovide(entry.getKey());
                }
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * Provides all signals that have not been removed from the simulator for
     * all current nodes
     */
    public void provideAll() {
        lock.lock();
        try {
            for (SCS node : nodes) {
                for (Entry<Integer, Integer> entry : provideMap.entrySet()) {
                    node.provide(entry.getKey());
                }
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * Sends a single signal to all connected nodes It is important that the
     * SCSData match the signal id
     *
     * @param signalID
     * @param data
     */
    public void sendValue(int signalID, SCSData data) {
        lock.lock();
        try {
            if(signalID == DRIVER_DISTRACTION_LEVEL_DATA_ID){}
            for (SCS node : nodes)
                node.send(signalID, data);
            lastValueSent.put(signalID, data);
        } finally {
            lock.unlock();
        }

    }

    public void disconnectSimulator() {
        lock.lock();
        try {
            for (SDPGatewayNode simulatorGateway : signalGateways) {
                simulatorGateway.stop();
            }
            signalGateways.clear();
        } finally {
            lock.unlock();
        }
    }

    public boolean isConnected() {
        return connected;
    }
}

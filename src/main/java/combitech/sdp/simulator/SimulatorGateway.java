package combitech.sdp.simulator;

import android.swedspot.scs.SCS;
import android.swedspot.scs.SCSDataListener;
import android.swedspot.scs.SCSFactory;
import android.swedspot.scs.SCSStatusListener;
import android.swedspot.scs.data.SCSData;
import android.swedspot.scs.data.Uint32;
import android.swedspot.sdp.SDPFactory;
import android.swedspot.sdp.configuration.Configuration;
import android.swedspot.sdp.observer.SDPConnectionListener;
import android.swedspot.sdp.observer.SDPDataListener;
import android.swedspot.sdp.observer.SDPGatewayNode;
import android.swedspot.sdp.observer.SDPNode;
import android.swedspot.sdp.routing.SDPNodeEthAddress;
import com.swedspot.vil.configuration.ConfigurationFactory;
import com.swedspot.vil.configuration.VilConstants;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.concurrent.locks.ReentrantLock;

public class SimulatorGateway {
    private static final int DRIVER_DISTRACTION_LEVEL_DATA_ID = 513;
    private static final int HARDWARE_KEY_ID = 514;

    private HashMap<Integer, Integer> provideMap;
    private HashMap<Integer, SCSData> lastValueSent;
    private LinkedList<SDPGatewayNode> simulatorGateways;
    private LinkedList<SCS> driverDistractionNodes;
    private LinkedList<SCS> hardwareKeyNodes;
    private LinkedList<SCS> signalNodes;
    private HashMap<Integer, LinkedList<SCSData>> receivedData;
    private SCS receiveNode;
    private LinkedList<Integer> collectedIds;
    private ReentrantLock lock;

    public SimulatorGateway() {
        simulatorGateways = new LinkedList<>();
        driverDistractionNodes = new LinkedList<>();
        hardwareKeyNodes = new LinkedList<>();
        signalNodes = new LinkedList<>();
        provideMap = new HashMap<>();
        lock = new ReentrantLock();
        lastValueSent = new HashMap<>();
        receivedData = new HashMap<>();
        collectedIds = collectAllIds();
    }

    public static LinkedList<Integer> collectAllIds() {
        LinkedList<Integer> tmp = new LinkedList<>();
        tmp.addAll(ConfigurationFactory.getConfiguration()
                .getConfiguredSignals().keySet());
        return tmp;
    }

    public boolean addAndInitiateNode(String address, int port,
                                      SDPConnectionListener connectionListener, SCSStatusListener statusListener) {
        SDPNode tmpNode = SDPFactory.createNodeInstance();
        SDPGatewayNode simulatorGateway = SDPFactory
                .createGatewayClientInstance();

        simulatorGateway.init(new SDPNodeEthAddress(address, port), tmpNode);
        simulatorGateway.addDataListener(new SDPDataListener() {
            @Override
            public byte[] request(int signalID) {
                byte[] data;
                lock.lock();
                try {
                    data = lastValueSent.get(signalID).getData();
                } finally {
                    lock.unlock();
                }
                return data != null ? data : new byte[]{0};
            }

            @Override
            public void receive(int signalID, byte[] data) {
            }
        });
        if (connectionListener != null) {
            simulatorGateway.setConnectionListener(connectionListener);
        }
        simulatorGateway.start();

        simulatorGateways.add(simulatorGateway);
        Configuration conf = ConfigurationFactory.getConfiguration();

        SCS node = SCSFactory.createSCSInstance(tmpNode, conf);
        if (statusListener != null) {
            node.setStatusListener(statusListener);
        }

        if (port == VilConstants.DRIVER_DISTRACTION_PORT) {
            driverDistractionNodes.add(node);
        } else if (port == VilConstants.HARDWARE_BUTTON_PORT) {
            hardwareKeyNodes.add(node);
        } else {
            signalNodes.add(node);
        }

        return true;
    }

    public boolean addAndInitiateNode(String address, int port,
                                      SDPConnectionListener connectionListener) {
        return addAndInitiateNode(address, port, connectionListener, null);
    }

    /**
     * The Simulator provides on all signalNodes a signal id
     *
     * @param signalID
     */
    public void provideSignal(int signalID) {
        // System.out.println("SimulatorGateway is providing signal; " + signalID);
        lock.lock();
        try {
            if (!provideMap.containsKey(signalID)) {
                provideMap.put(signalID, 1);
                if (signalID == DRIVER_DISTRACTION_LEVEL_DATA_ID) {
                    for (SCS node : driverDistractionNodes) {
                        node.provide(signalID);
                        // System.out.println("Providing signal " + signalID + " on driver distraction node");
                    }
                } else if (signalID == HARDWARE_KEY_ID) {
                    for (SCS node : hardwareKeyNodes) {
                        node.provide(signalID);
                        // System.out.println("Providing signal " + signalID + " on hardware node");
                    }
                } else {
                    for (SCS node : signalNodes) {
                        node.provide(signalID);
                        // System.out.println("Providing signal " + signalID + " on data node");
                    }
                    //System.out.println("providing: " + signalID
                    //        + " on signalNodes");
                }
                if (receiveNode != null) {
                    receiveNode.unsubscribe(signalID);
                }
            } else {
                provideMap.put(signalID, provideMap.get(signalID) + 1);
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * The Simulator unprovides on all signalNodes a signal id
     *
     * @param signalID
     */
    public void unprovideSignal(int signalID) {
        lock.lock();
        try {
            if (provideMap.containsKey(signalID)) {
                provideMap.put(signalID, provideMap.get(signalID) - 1);
                if (provideMap.get(signalID) == 0) {
                    if (signalID == DRIVER_DISTRACTION_LEVEL_DATA_ID) {
                        for (SCS node : driverDistractionNodes) {
                            node.unprovide(signalID);
                        }
                    } else if (signalID == HARDWARE_KEY_ID) {
                        for (SCS node : hardwareKeyNodes) {
                            node.unprovide(signalID);
                        }
                    } else {
                        for (SCS node : signalNodes) {
                            node.unprovide(signalID);
                        }
                    }
                    provideMap.remove(signalID);
                    if (receiveNode != null) {
                        receiveNode.subscribe(signalID);
                    }
                }
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * Unprovides all signals for all current signalNodes but retains the IDs
     */
    public void unprovideAll() {
        lock.lock();
        try {
            for (SCS node : signalNodes) {
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
     * all current signalNodes
     */
    public void provideAll() {
        lock.lock();
        try {
            for (SCS node : signalNodes) {
                for (Entry<Integer, Integer> entry : provideMap.entrySet()) {
                    node.provide(entry.getKey());
                }
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * Sends a single signal to all connected signalNodes It is important that
     * the SCSData match the signal id
     *
     * @param signalID
     * @param data
     */
    public void sendValue(int signalID, SCSData data) {
        lock.lock();
        // System.out.println("sending data");
        try {
            if (signalID == HARDWARE_KEY_ID) {
                for (SCS node : hardwareKeyNodes) {
                    node.send(signalID, data);
                    // System.out.println("GXT27 Steering Wheel module is sending" + new Uint32(data.getData()).getIntValue());
                }
            }
            if (lastValueSent.get(signalID) == null || !lastValueSent.get(signalID).equals(data)) {
                if (signalID == DRIVER_DISTRACTION_LEVEL_DATA_ID) {
                    for (SCS node : driverDistractionNodes) {
                        node.send(signalID, data);
                    }
                } else {
                    for (SCS node : signalNodes) {
                        node.send(signalID, data);
                    }
                }
            }
            lastValueSent.put(signalID, data);

        } finally {
            lock.unlock();
        }

    }

    public void disconnectSimulator() {
        lock.lock();
        try {
            for (SDPGatewayNode simulatorGateway : simulatorGateways) {
                simulatorGateway.stop();
            }
            simulatorGateways.clear();
        } finally {
            lock.unlock();
        }
    }

    public void subscribeForAll(SCS node) {
        lock.lock();
        for (Integer i : collectedIds) {
            node.subscribe(i);
        }
        lock.unlock();
    }

    public void createReceiveNode(String address, int port,
                                  final ReceiveListener listener) {
        SDPNode tmpNode = SDPFactory.createNodeInstance();
        SDPGatewayNode receiveGateway = SDPFactory
                .createGatewayClientInstance();

        receiveGateway.init(new SDPNodeEthAddress(address, port), tmpNode);
        Configuration conf = ConfigurationFactory.getConfiguration();
        receiveNode = SCSFactory.createSCSInstance(tmpNode, conf);
        receiveNode.setDataListener(new SCSDataListener() {
            @Override
            public void receive(int signalID, SCSData data) {
                lock.lock();
                try {
                    if (listener != null) {
                        listener.receiveData(signalID, data);
                    }
                    LinkedList<SCSData> tmp = getReceivedValuesFor(signalID);
                    if (tmp != null) {
                        tmp.add(0, data);
                        if (tmp.size() >= 11) {
                            tmp.remove(10);
                        }
                    } else {
                        tmp = new LinkedList<>();
                        tmp.add(data);
                    }
                    receivedData.put(signalID, tmp);
                } finally {
                    lock.unlock();
                }
            }

            @Override
            public SCSData request(int signalID) {
                return null;
            }
        });
        receiveGateway.start();
        subscribeForAll(receiveNode);
    }

    public LinkedList<SCSData> getReceivedValuesFor(int signalID) {
        return receivedData.get(signalID);
    }
}

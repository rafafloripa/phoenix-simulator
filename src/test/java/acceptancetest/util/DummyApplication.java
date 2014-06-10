package acceptancetest.util;

import java.util.HashMap;
import java.util.LinkedList;

import android.swedspot.scs.SCS;
import android.swedspot.scs.SCSDataListener;
import android.swedspot.scs.SCSFactory;
import android.swedspot.scs.SCSStatusListener;
import android.swedspot.scs.data.SCSData;
import android.swedspot.sdp.SDPFactory;
import android.swedspot.sdp.SubscriptionStatus;
import android.swedspot.sdp.observer.SDPGatewayNode;
import android.swedspot.sdp.observer.SDPNode;
import android.swedspot.sdp.routing.SDPNodeEthAddress;
import android.swedspot.sdp.util.Converter;

public class DummyApplication {
    SDPNode dummyAppNode;
    SDPGatewayNode dummyAppGateway;
    SCS dummySCSNode;
    HashMap<Integer, Integer> signalStorage = new HashMap<>();
    LinkedList<Integer> receivedData = new LinkedList<>();

    public DummyApplication(int portToRunOn) throws InterruptedException {

        dummyAppNode = SDPFactory.createNodeInstance();

        dummyAppGateway = SDPFactory.createGatewayServerInstance();
        dummyAppGateway.init(new SDPNodeEthAddress("localhost", portToRunOn), dummyAppNode);
        dummySCSNode = SCSFactory.createSCSInstance(dummyAppNode);
        dummySCSNode.setDataListener(new SCSDataListener() {

            @Override
            public SCSData request(int arg0) {
                return null;
            }

            @Override
            public void receive(int signalID, SCSData data) {
                int normalInt = Converter.getAs32BitUnsignedInteger(data.getData());
                signalStorage.put(signalID, normalInt);
                receivedData.add(normalInt);
                System.err.println("Signal: " + signalID + " Received data: " + normalInt);
            }
        });
        dummySCSNode.setStatusListener(new SCSStatusListener() {

            @Override
            public void statusChanged(int arg0, SubscriptionStatus arg1) {
                // System.err.println("status changed: "+arg1);
            }
        });
        dummyAppGateway.start();
    }

    public void subscribe(int signalID) {
        dummySCSNode.subscribe(signalID);
    }

    public boolean getStatus(int signalID) {
        return dummyAppNode.isSubscriber(signalID);
    }

    public Integer getReceivedValue(int signalID) {
        return signalStorage.get(signalID);
    }

    public LinkedList<Integer> getListOfreceivedValues() {
        return receivedData;
    }

    public void stop() {
        dummyAppGateway.stop();
    }
}

package acceptancetest.util;

import java.util.HashMap;
import java.util.LinkedList;

import com.swedspot.scs.SCS;
import com.swedspot.scs.SCSDataListener;
import com.swedspot.scs.SCSFactory;
import com.swedspot.scs.SCSStatusListener;
import com.swedspot.scs.data.SCSData;
import com.swedspot.sdp.SDPFactory;
import com.swedspot.sdp.SubscriptionStatus;
import com.swedspot.sdp.observer.SDPGatewayNode;
import com.swedspot.sdp.observer.SDPNode;
import com.swedspot.sdp.routing.SDPNodeEthAddress;
import com.swedspot.sdp.util.Converter;

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

    public int getReceivedValue(int signalID) {
        return signalStorage.get(signalID);
    }

    public LinkedList<Integer> getListOfreceivedValues() {
        return receivedData;
    }

    public void stop() {
        dummyAppGateway.stop();
    }
}

package acceptancetest.subscribe;

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

	public DummyApplication() throws InterruptedException {

		dummyAppNode = SDPFactory.createNodeInstance();

		dummyAppGateway = SDPFactory.createGatewayServerInstance();
		dummyAppGateway.init(new SDPNodeEthAddress("localhost", 8126), dummyAppNode);
		dummySCSNode = SCSFactory.createSCSInstance(dummyAppNode);
		dummySCSNode.setDataListener(new SCSDataListener() {
			
			@Override
			public SCSData request(int arg0) {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public void receive(int signalID, SCSData data) {
				int[] dataArray = Converter.getAsIntArray(data.getData());
				int normalInt = (dataArray[0] + 1) * (dataArray[1] + 1) - 1;
				signalStorage.put(signalID, normalInt);
				receivedData.add(normalInt);
//				System.err.println("Received data: "+normalInt);
			}
		});
		dummySCSNode.setStatusListener(new SCSStatusListener() {
			
			@Override
			public void statusChanged(int arg0, SubscriptionStatus arg1) {
//				System.err.println("status changed: "+arg1);
			}
		});
		dummyAppGateway.start();
	}

	public void subscribe(int signalID) {
		dummySCSNode.subscribe(signalID);
	}
	
	public boolean getStatus(int signalID){
		return dummyAppNode.isSubscriber(signalID);
	}

	public int getReceivedValue(int signalID) {
		return signalStorage.get(signalID);
	}
	
	public LinkedList<Integer> getListOfreceivedValues() {
		return receivedData;
	}

}

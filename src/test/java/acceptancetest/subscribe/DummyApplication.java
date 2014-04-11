package acceptancetest.subscribe;

import java.util.HashMap;

import com.swedspot.sdp.SDPFactory;
import com.swedspot.sdp.observer.SDPDataListener;
import com.swedspot.sdp.observer.SDPGatewayNode;
import com.swedspot.sdp.observer.SDPNode;
import com.swedspot.sdp.routing.SDPNodeEthAddress;

public class DummyApplication {
	SDPNode dummyAppNode;
	SDPGatewayNode dummyAppGateway;
	HashMap<Integer, Integer> signalStorage = new HashMap<>();

	public DummyApplication() throws InterruptedException {

		dummyAppNode = SDPFactory.createNodeInstance();
		dummyAppNode.addDataListener(new SDPDataListener() {

			@Override
			public byte[] request(int arg0) {
				return null;
			}

			@Override
			public void receive(int signalID, byte[] data) {
				signalStorage.put(signalID, (int) data[0]);
				System.err.print(data[0]);
			}
		});

		dummyAppGateway = SDPFactory.createGatewayServerInstance();
		dummyAppGateway.init(new SDPNodeEthAddress("localhost", 8126), dummyAppNode);
		dummyAppGateway.start();
	}

	public void subscribe(int signalID) {
		dummyAppNode.subscribe(signalID);
	}

	public int getReceivedValue(int signalID) {
		return signalStorage.get(signalID);
	}

}

package simulator;

import com.swedspot.sdp.SDPFactory;
import com.swedspot.sdp.observer.SDPGatewayNode;
import com.swedspot.sdp.observer.SDPNode;
import com.swedspot.sdp.routing.SDPNodeEthAddress;

public class Simulator {
	private SDPNode simulatorNode;
	private SDPGatewayNode simulatorGateway;

	public void setupSignal(int signalID, int startingValue) throws InterruptedException {
		simulatorNode = SDPFactory.createNodeInstance();

		simulatorGateway = SDPFactory.createGatewayClientInstance();
		simulatorGateway.init(new SDPNodeEthAddress("localhost", 8126), simulatorNode);
		simulatorGateway.start();
		simulatorNode.provide(signalID);
	}

	public void changeValue(int signalID, int newValue) {
		simulatorNode.send(signalID, new byte[] { (byte) newValue });
	}

	public void stop(){
		simulatorGateway.stop();
	}
}

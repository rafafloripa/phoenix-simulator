package simulator;

import com.swedspot.scs.SCS;
import com.swedspot.scs.SCSFactory;
import com.swedspot.scs.data.Uint32;
import com.swedspot.sdp.SDPFactory;
import com.swedspot.sdp.observer.SDPGatewayNode;
import com.swedspot.sdp.observer.SDPNode;
import com.swedspot.sdp.routing.SDPNodeEthAddress;

public class Simulator {
	private SDPNode simulatorNode;
	private SDPGatewayNode simulatorGateway;
	private SCS node;
	
	public void setupNode(){
		simulatorNode = SDPFactory.createNodeInstance();

		simulatorGateway = SDPFactory.createGatewayClientInstance();
		simulatorGateway.init(new SDPNodeEthAddress("localhost", 8126), simulatorNode);
		simulatorGateway.start();
		node = SCSFactory.createSCSInstance(simulatorNode);
	}

	public void setupSignal(int signalID, int startingValue) throws InterruptedException {
		node.provide(signalID);
	}

	public void changeValue(int signalID, int newValue) {
//		simulatorNode.send(signalID, new byte[] { (byte) newValue });
		node.send(signalID, new Uint32(newValue));
	}

	public void stop(){
		simulatorGateway.stop();
	}
	
	public SCS getNode()
	{
		return node;
	}
	
}

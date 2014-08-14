package acceptancetest.util;

import android.swedspot.scs.data.*;
import combitech.sdp.dummy.Server;
import combitech.sdp.simulator.SimulatorGateway;
import cucumber.api.java.After;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;

import java.util.LinkedList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class CommonSteps {

    @After("@shutdownNode")
    public void shutdownNodeStep() {
        try {
            if (Util.staticSimulator != null) {
                Util.staticSimulator.disconnectSimulator();
                Util.staticSimulator = null;
            }
            if (Util.staticDummyApp != null) {
                Util.staticDummyApp.stop();
                Util.staticDummyApp = null;
            }
            if (Util.staticServer != null) {
                Util.staticServer.shutdownServer();
                Util.staticServer = null;
            }
            if(Util.staticModule != null){
                Util.staticModule.stopModule();
                Util.staticModule = null;
            }
            Thread.sleep(3000);
        } catch (Exception e) {
        }

    }

    @Given("^The simulator is setup$")
    public void setupSimulator() throws Throwable {
        Util.staticSimulator = new SimulatorGateway();
    }

    @Given("^The simulator is providing signal id (\\d+)$")
    public void simulatorProvidesSignal(int signalID) {
        Util.staticSimulator.provideSignal(signalID);
    }

    @Given("^Add a node to simulator on port (\\d+) and ip (.*)$")
    public void addNode(int port, String ipAddress) {
        Util.staticSimulator.addAndInitiateNode(ipAddress, port, null);
    }

    @And("^After (\\d+) mSec have passed$")
    public void wait(int timeOut) throws Throwable {
        Thread.sleep(timeOut);
    }

    @Given("^The dummy application is setup and listening on port (\\d+)$")
    public void the_dummy_application_is_setup_and_listening_on_port(int port) throws Throwable {
        Util.staticDummyApp = new DummyApplication(port);
    }

    @Given("^The dummy application subscribes for signal id (\\d+)$")
    public void dummyAppSubscribe(int signalID) {
        Util.staticDummyApp.subscribe(signalID);
    }

    @Given("^The dummy server is setup$")
    public void dummyServerSetup() {
        Util.staticServer = new Server();
        Util.staticServer.startServer();
        Util.staticServer.start();
    }

    @Then("^The simulator should have received (\\d+) from signal id (\\d+) as a (.*)$")
    public void simulatorHasReceived(int value, int signalID, String t) {
        String type = t.toLowerCase();
        LinkedList<SCSData> data = Util.staticSimulator.getReceivedValuesFor(signalID);
        assertNotNull(data);

        switch (type) {
        case "integer":
            SCSInteger scsInteger = new SCSInteger(data.getFirst().getData());
            assertEquals(scsInteger.getIntValue(), value);
            break;
        case "float":
            SCSFloat scsFloat = new SCSFloat(data.getFirst().getData());
            assertEquals(scsFloat.getFloatValue(), value, 0.01);
            break;
        case "uint32":
            Uint32 uint32 = new Uint32(data.getFirst().getData());
            assertEquals(uint32.getIntValue(), value);
            break;
        case "uint16":
            Uint16 uint16 = new Uint16(data.getFirst().getData());
            assertEquals(uint16.getIntValue(), value);
            break;
        case "uint8":
            Uint8 uint8 = new Uint8(data.getFirst().getData());
            assertEquals(uint8.getIntValue(), value);
            break;
        case "double":
            SCSDouble scsDouble = new SCSDouble(data.getFirst().getData());
            assertEquals(scsDouble.getDoubleValue(), value, 0.01);
            break;
        }
    }

    @And("^The simulator is ready to receive data from address (.*) and port (\\d+)$")
    public void simulatorCreateReceiveNode(String address, int port) {
        Util.staticSimulator.createReceiveNode(address, port, null);
    }
}

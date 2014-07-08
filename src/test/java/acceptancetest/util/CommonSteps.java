package acceptancetest.util;

import combitech.sdp.simulator.SimulatorGateway;
import cucumber.api.java.After;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;

public class CommonSteps {

    @After("@shutdownNode")
    public void shutdownNodeStep()
    {
        try {
            Util.staticSimulator.disconnectSimulator();
            Util.staticDummyApp.stop();
            Thread.sleep(1000);
        } catch (Exception e) {
        }
        
    }

    @Given("^The simulator is setup$")
    public void setupSimulator() throws Throwable {
        Util.staticSimulator = new SimulatorGateway();
    }
    
    @Given("^The simulator is providing signal id (\\d+)$")
    public void simulatorProvidesSignal(int signalID)
    {
        Util.staticSimulator.provideSignal(signalID);
    }
    
    @Given("^Add a node to simulator on port (\\d+) and ip (.*)$")
    public void addNode(int port, String ipAdress){
    	Util.staticSimulator.addAndInitiateNode(ipAdress, port, null);
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
    public void dummyAppSubscribe(int signalID){
        Util.staticDummyApp.subscribe(signalID);
    }
}

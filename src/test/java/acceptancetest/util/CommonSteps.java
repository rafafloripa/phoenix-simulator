package acceptancetest.util;

import simulator.Simulator;
import cucumber.api.java.After;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;

public class CommonSteps {

    @After("@shutdownNode")
    public void shutdownNodeStep()
    {
        try {
            Util.staticSimulator.stopSimulation();
            Util.staticDummyApp.stop();
        } catch (Exception e) {
        }
    }

    @Given("^The simulator is setup$")
    public void setupSimulator() throws Throwable {
        Util.staticSimulator = new Simulator();
    }
    
    @Given("^Add a node on port (\\d+) and ip (.*)$")
    public void addNode(int port, String ipAdress){
    	Util.staticSimulator.addAndInitiateNode(ipAdress, port);
    }

    @And("^After (\\d+) mSec have passed$")
    public void wait(int timeOut) throws Throwable {
        Thread.sleep(timeOut);
    }
}

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
			Util.staticSimulator.stop();
			Util.staticDummyApp.stop();
		} catch (Exception e) {}
	}
	
	@Given("^The simulator is setup and running$")
	public void setupSimulator() throws Throwable {
		Util.staticSimulator = new Simulator();
		Util.staticSimulator.setupNode();
	}
	
	@And("^After (\\d+) mSec have passed$")
	public void wait(int timeOut) throws Throwable {
		Thread.sleep(timeOut);
	}
}

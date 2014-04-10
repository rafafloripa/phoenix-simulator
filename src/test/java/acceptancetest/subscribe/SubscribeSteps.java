package acceptancetest.subscribe;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import simulator.Simulator;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

public class SubscribeSteps {

	Simulator simulator;
	DummyApplication dummyApplication;
	

	@Given("^The simulator is setup and running$")
	public void setupSimulator() throws Throwable {
		simulator = new Simulator();
		assertNotNull(simulator);
	}

	@Given("^The simulator is providing 0x([0-9a-fA-F]+) with value (\\d+)$")
	public void setupSimulatorSignal(int signalID, int startingValue) throws Throwable {
		simulator.setupSignal(signalID, startingValue);
	}

	@Given("^DummyApp subscribes for signal 0x([0-9a-fA-F]+)$")
	public void developerSubscribes(int signalID) throws Throwable {
		dummyApplication = new DummyApplication();
		dummyApplication.subscribe(signalID);
	}
	
	@Given("^After (\\d+) mSec have passed$")
	public void wait(int timeOut) throws Throwable {
		Thread.sleep(timeOut);
	}

	@When("^The simulator changes the signal 0x([0-9a-fA-F]+) to (\\d+)$")
	public void signalChangesValue(int signalID, int newValue) throws Throwable {
		simulator.changeValue(signalID, newValue);
	}

	@Then("^DummyApp should get a notification for signal 0x([0-9a-fA-F]+) with value (\\d+)$")
	public void notifyDeveloper(int signalID, int newValue) throws Throwable {
		assertTrue(dummyApplication.getReceivedValue(signalID) == newValue);
	}
}

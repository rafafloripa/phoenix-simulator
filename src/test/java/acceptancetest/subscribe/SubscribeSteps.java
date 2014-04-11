package acceptancetest.subscribe;

import static org.junit.Assert.assertTrue;
import acceptancetest.util.ATUtillity;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

public class SubscribeSteps {

	
	// First given method lies within the ReadFileSetps.java
	
	@Given("^The simulator is providing 0x([0-9a-fA-F]+) with value (\\d+)$")
	public void setupSimulatorSignal(int signalID, int startingValue) throws Throwable {
		ATUtillity.staticSimulator.setupSignal(signalID, startingValue);
	}

	@Given("^DummyApp subscribes for signal 0x([0-9a-fA-F]+)$")
	public void developerSubscribes(int signalID) throws Throwable {
		ATUtillity.staticDummyApp = new DummyApplication();
		ATUtillity.staticDummyApp.subscribe(signalID);
	}
	
	@Given("^After (\\d+) mSec have passed$")
	public void wait(int timeOut) throws Throwable {
		Thread.sleep(timeOut);
	}

	@When("^The simulator changes the signal 0x([0-9a-fA-F]+) to (\\d+)$")
	public void signalChangesValue(int signalID, int newValue) throws Throwable {
		ATUtillity.staticSimulator.changeValue(signalID, newValue);
	}

	@Then("^DummyApp should get a notification for signal 0x([0-9a-fA-F]+) with value (\\d+)$")
	public void notifyDeveloper(int signalID, int newValue) throws Throwable {
		assertTrue(ATUtillity.staticDummyApp.getReceivedValue(signalID) == newValue);
		ATUtillity.staticSimulator.stop();
	}
}

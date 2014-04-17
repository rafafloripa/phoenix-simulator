package acceptancetest.subscribe;

import static org.junit.Assert.assertEquals;
import acceptancetest.util.DummyApplication;
import acceptancetest.util.Util;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

public class SubscribeSteps {

    // First given method lies within the ReadFileSetps.java

    @Given("^The simulator is providing (\\d+) with value (\\d+)$")
    public void setupSimulatorSignal(int signalID, int startingValue) throws Throwable {
        Util.staticSimulator.setupSignal(signalID, startingValue);
    }

    @Given("^DummyApp subscribes for signal (\\d+)$")
    public void developerSubscribes(int signalID) throws Throwable {
        Util.staticDummyApp = new DummyApplication();
        Util.staticDummyApp.subscribe(signalID);
    }

    @When("^The simulator changes the signal (\\d+) to (\\d+)$")
    public void signalChangesValue(int signalID, int newValue) throws Throwable {
        Util.staticSimulator.changeValue(signalID, newValue);
    }

    @Then("^DummyApp should get a notification for signal (\\d+) with value (\\d+)$")
    public void notifyDeveloper(int signalID, int newValue) throws Throwable {
        assertEquals(newValue, Util.staticDummyApp.getReceivedValue(signalID));
    }
}

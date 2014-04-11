package acceptancetest.readfile;

import simulator.Simulator;
import acceptancetest.subscribe.DummyApplication;
import acceptancetest.util.ATUtillity;
import cucumber.api.DataTable;
import cucumber.api.PendingException;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

public class ReadFileSteps {

	@Given("^The simulator is setup and running$")
	public void setupSimulatorReadFile() throws Throwable {
	    ATUtillity.staticSimulator = new Simulator();
	}

	@Given("^The DummyApp has subscribed to signal 0x([0-9a-fA-F]+)$")
	public void setupDummyApp(int signalID) throws Throwable {
	    ATUtillity.staticDummyApp = new DummyApplication();
	    ATUtillity.staticDummyApp.subscribe(signalID);
	}
	
	@Given("^The simulator reads ExampleData file to replay$")
	public void the_simulator_reads_ExampleData_file_to_replay() throws Throwable {
	    // Write code here that turns the phrase above into concrete actions
	    throw new PendingException();
	}

	@When("^The simulator start replaying$")
	public void the_simulator_start_replaying() throws Throwable {
	    // Write code here that turns the phrase above into concrete actions
	    throw new PendingException();
	}

	@Then("^DummyApp should get all changes$")
	public void dummyapp_should_get_all_changes(DataTable table) throws Throwable {
	    throw new PendingException();
	}
}

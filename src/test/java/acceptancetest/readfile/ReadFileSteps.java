package acceptancetest.readfile;

import static org.junit.Assert.*;

import java.io.File;

import simulator.Simulator;
import simulator.StorageFileReader;
import acceptancetest.subscribe.DummyApplication;
import acceptancetest.util.ATUtillity;
import cucumber.api.DataTable;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

public class ReadFileSteps {

	StorageFileReader reader;
	
	@Given("^The simulator is setup and running$")
	public void setupSimulatorReadFile() throws Throwable {
	    ATUtillity.staticSimulator = new Simulator();
	    ATUtillity.staticSimulator.setupNode();
	}

	@Given("^The DummyApp has subscribed to signal (\\d+)$")
	public void setupDummyApp(int signalID1) throws Throwable {
	    ATUtillity.staticDummyApp = new DummyApplication();
	    Thread.sleep(3000);
	    ATUtillity.staticDummyApp.subscribe(signalID1);
	    ATUtillity.staticDummyApp.subscribe(150);
	}
	
	@Given("^The simulator reads ExampleData file to replay$")
	public void the_simulator_reads_ExampleData_file_to_replay() throws Throwable {
	    File exampleData = new File("testResources/ExampleData");
	    assertTrue(exampleData.exists());
	    reader = new StorageFileReader(ATUtillity.staticSimulator);
	    assertTrue(reader.readFile(exampleData));
	}

	@When("^The simulator start replaying$")
	public void the_simulator_start_replaying() throws Throwable {
	    reader.startSimulation();
	    Thread runThis = new Thread(reader);
	    runThis.start();
	}

	@Then("^The DummyApp should should wait for the simulator to finish sending all data$")
	public void dummyapp_should_get_all_changes(DataTable table) throws Throwable {
		long totalTimestamp = 0;
		long diff = 0;
		for (int i = 2; i < table.raw().size(); i++) {
			diff = Long.parseLong(table.raw().get(i).get(0)) - Long.parseLong(table.raw().get(i-1).get(0));
			totalTimestamp += diff;
		}
		Thread.sleep(totalTimestamp+1000);
	}
	
	@Then("^The DummyApp should have received all data for signal (\\d+)$")
	public void receivedSignals(int signalID1, DataTable table){
		for (int i = 1; i < table.raw().size(); i++) {
			assertTrue(Integer.parseInt(table.raw().get(i).get(2)) == (int)ATUtillity.staticDummyApp.getListOfreceivedValues().get(i-1));
		}
		assertNotNull(ATUtillity.staticDummyApp.getReceivedValue(signalID1));
	}
}

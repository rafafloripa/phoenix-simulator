package acceptancetest.readfile;


import static org.junit.Assert.*;

import java.io.File;

import acceptancetest.util.DummyApplication;
import acceptancetest.util.Util;
import simulator.Simulator;
import simulator.StorageFileReader;
import cucumber.api.DataTable;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

public class ReadFileSteps {

	StorageFileReader reader;
	


	@Given("^The DummyApp has subscribed to signal (\\d+)$")
	public void setupDummyApp(int signalID1) throws Throwable {
	    Util.staticDummyApp = new DummyApplication();
	    Thread.sleep(3000);
	    Util.staticDummyApp.subscribe(signalID1);
	    Util.staticDummyApp.subscribe(150);
	}
	
	@Given("^The simulator reads ExampleData file to replay$")
	public void the_simulator_reads_ExampleData_file_to_replay() throws Throwable {
		System.err.println(new java.io.File( "." ).getCanonicalPath());
	    File exampleData = new File("testResources/ExampleData");
	    assertTrue(exampleData.exists());
	    reader = new StorageFileReader(Util.staticSimulator);
	    assertTrue(reader.readFile(exampleData));
	}

	@When("^The simulator start replaying$")
	public void the_simulator_start_replaying() throws Throwable {
	    reader.startSimulation();
	    Thread runThis = new Thread(reader);
	    runThis.start();
	}

	@Then("^The DummyApp should wait for the simulator to finish sending all data$")
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
			assertTrue(Integer.parseInt(table.raw().get(i).get(2)) == (int)Util.staticDummyApp.getListOfreceivedValues().get(i-1));
		}
		assertNotNull(Util.staticDummyApp.getReceivedValue(signalID1));
	}
}

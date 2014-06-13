package acceptancetest.connectToDriverDistraction;

import static org.junit.Assert.*;

import com.swedspot.vil.distraction.DriverDistraction;
import com.swedspot.vil.distraction.impl.DriverDistractionImpl;

import acceptancetest.util.Util;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

/**
 * This is a very basic type of test, for tutorials look here:
 * https://github.com/cucumber/cucumber/wiki/tutorials-and-related-blog-posts
 * 
 * @author Christopher
 * 
 */
public class ConnectToDriverDistractionSteps {

	private DriverDistraction driverDistraction;
//	  Given The simulator is setup
//    And Add a node on port 8126 and ip localhost
//    And Add a node on port 8127 and ip localhost
	
	@Given("^The Driver Distraction is setup$")
    public void driverDistractionSetup() throws Throwable {
		driverDistraction = DriverDistractionImpl.getInstance();
        Thread.sleep(1000);
    }
	
    @When("^The simulator connects to the driver distraction$")
    public void waitingForConnection() throws Throwable {
        Thread.sleep(1000);
    }
    
    

    @Then("^The connection should be accepted$")
    public void connectionExists() throws Throwable {
        assertTrue(Util.staticSimulator.getGatewaysNodes().size()>1);
        assertTrue(Util.staticSimulator.getGatewaysNodes().get(1).connections().size()>0);
    }
}

package acceptancetest.connectToDriverDistraction;

import org.junit.runner.RunWith;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;

@RunWith(Cucumber.class)
@CucumberOptions(features = "classpath:acceptancetest/connectToDriverDistraction/connectToDriverDistraction.feature", glue = { "acceptancetest.connectToDriverDistraction", "acceptancetest.util" }, monochrome = true, format = "pretty")
public class ConnectToDriverDistractionTest {
}

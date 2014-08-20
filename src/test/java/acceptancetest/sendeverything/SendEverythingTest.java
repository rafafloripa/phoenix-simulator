package acceptancetest.sendeverything;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;
import org.junit.Ignore;
import org.junit.runner.RunWith;
@RunWith(Cucumber.class)
@CucumberOptions(
		features = "classpath:acceptancetest/sendeverything/sendEverything.feature",
		glue = { "acceptancetest.sendeverything", "acceptancetest.util" },
		monochrome = true, 
		format = {"pretty", "json:build/cucumber-reports/cucumber.json"})
public class SendEverythingTest {

}

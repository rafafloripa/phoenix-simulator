package acceptancetest.subscribe;

import org.junit.runner.RunWith;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;

@RunWith(Cucumber.class)
@CucumberOptions(
		features = "classpath:acceptancetest/subscribe/subscribe.feature", 
		glue = { "acceptancetest.subscribe", "acceptancetest.util" }, 
		monochrome = true, 
		format = {"pretty", "json:build/cucumber-reports/cucumber.json"})
public class SubscribeTest {

}

package acceptancetest.readfile;

import org.junit.runner.RunWith;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;

@RunWith(Cucumber.class)
@CucumberOptions(features = "classpath:acceptanceTest/readfile/readFile.feature", glue = { "acceptancetest.readfile",  "acceptancetest.util" }, monochrome = true, format = "pretty")
public class ReadFileTest {}

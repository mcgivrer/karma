package my.karma.app.tests;

import io.cucumber.core.options.Constants;
import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.Suite;

@Suite
@IncludeEngines("cucumber")
@ConfigurationParameter(key = Constants.GLUE_PROPERTY_NAME, value = "com.karma.test.features")
@ConfigurationParameter(key = "cucumber.features", value = "src/test/resources/features")
@ConfigurationParameter(key = "plugin", value = "usage")
class CucumberTests {
}

package my.karma.app.tests.features;

import io.cucumber.java8.En;
import my.karma.app.KarmaPlatform;
import org.junit.jupiter.api.Assertions;

public class KarmaConfigurationStepDefs implements En {
  KarmaPlatform kp;
  KarmaPlatform.Configuration config;

  public KarmaConfigurationStepDefs() {
    When("KarmaPlatform is created with {string}", (String configPath) -> {
      kp = new KarmaPlatform(configPath);
    });
    Then("KarmaPlatform has a configuration object instantiated", () -> {
      Assertions.assertNotNull(kp.getConfiguration(), "Configuration does not exists");
    });
    And("The configuration file is loaded", () -> {
      config = kp.getConfiguration();
      Assertions.assertFalse(config.getProperties().isEmpty(), "Configuration contains properties");
    });

  }

}

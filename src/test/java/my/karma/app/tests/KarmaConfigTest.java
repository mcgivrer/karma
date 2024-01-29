package my.karma.app.tests;

import my.karma.app.KarmaPlatform;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Order;
import org.junit.platform.suite.api.Suite;

import java.util.List;

@Suite(failIfNoTests = false)
public class KarmaConfigTest {
    KarmaPlatform app;
    KarmaPlatform.Configuration config;

    @Before
    public void setup() {
        app = new KarmaPlatform("/test-config.properties");
        config = new KarmaPlatform.Configuration(app);
    }

    @Test
    @Order(1)
    public void configurationHasProperties() {
        Assertions.assertNotNull(config.getProperties());
    }

    @Test
    @Order(2)
    public void configurationHasExitMode() {
        config.parseArguments(List.of("app.exit=true"));
        Assertions.assertTrue(app.isExit(), "Configuration has no exit mode");
    }

    @Test
    @Order(3)
    public void configurationHasTestMode() {
        config.parseArguments(List.of("app.test.mode=true"));
        Assertions.assertTrue(app.isTestMode(), "Configuration has no test mode");
    }


}

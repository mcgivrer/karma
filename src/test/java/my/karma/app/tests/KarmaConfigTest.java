package my.karma.app.tests;

import my.karma.app.KarmaPlatform;
import my.karma.app.tests.scenes.TestScene;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.platform.suite.api.Suite;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.Optional;

@Suite(failIfNoTests = false)
public class KarmaConfigTest {
    static KarmaPlatform app;
    static KarmaPlatform.Configuration config;

    @BeforeAll
    public static void setup() {
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

    @Test
    @Order(4)
    public void configurationHasPhysicProperties() {
        config.load("/test-physic-config.properties");
        Assertions.assertEquals(
                app.getWorld()
                        .getPlayArea(),
                new Rectangle2D.Double(0.0, 0.0, 800.0, 600.0),
                "Configuration has no Play area defined !");
        Assertions.assertEquals(
                app.getWorld()
                        .getGravity(),
                new KarmaPlatform.Vector2D(0.0, -0.00981),
                "Configuration has no gravity defined !");
        Assertions.assertEquals(
                app.getWorld()
                        .getVelocityMax(),
                new KarmaPlatform.Vector2D(0.2, 0.2),
                "Configuration has no velocity max defined !");
        Assertions.assertEquals(
                app.getWorld()
                        .getAccelerationMax(),
                new KarmaPlatform.Vector2D(0.012, 0.012),
                "Configuration has no acceleration max defined !");
        Assertions.assertEquals(
                app.getWorld()
                        .getPartitioningLevelMax(), 5,
                "Configuration has no partitioning max level defined !");
        Assertions.assertEquals(
                app.getWorld()
                        .getPartitioningCellPerLevel(), 5,
                "Configuration has no max number of cells in a partition defined !");
    }

    @Test
    @Order(5)
    public void configurationHasDebugProperties() {
        config.load("/test-debug-config.properties");
        Assertions.assertEquals(4, app.getDebugLevel());
        Assertions.assertEquals("filtered,entities,names", app.getDebugEntityFilteredName());
    }

    @Test
    @Order(6)
    public void configurationHasRenderingProperties() {
        config.load("/test-render-config.properties");
        Assertions.assertEquals(
                new Dimension(640, 400),
                app.getWindowSize(),
                "Configuration has no window size set");
        Assertions.assertEquals(
                new Dimension(320, 200),
                app.getScreenSize(),
                "Configuration has no rendering buffer set");
        Assertions.assertEquals(
                3,
                app.getStrategyBufferNb(),
                "Configuration has no buffer strategy set");
    }

    @Test
    @Order(7)
    public void configurationHasSceneProperties() {

        config.load("/test-scene-config.properties");
        //# App scenes list and default.
        //app.scenes.list=test:my.karma.app.tests.scenes.TestScene,
        //app.scenes.default=test
        Assertions.assertEquals(
                "test",
                app.getSceneManager().getDefaultSceneName(),
                "Configuration has no default Scene name set");
        List<KarmaPlatform.Scene> list = List.of(new TestScene(app));
        Optional<KarmaPlatform.Scene> propScene = app.getSceneManager().getScenes()
                .stream().filter(s -> s.getName().equals("test"))
                .findFirst();
        Assertions.assertEquals(
                list.getFirst().getName(), propScene.get().getName(),
                "Configuration has no Scene list set");
    }
}

package my.karma.app.tests;

import my.karma.app.ConfigService;
import my.karma.app.KConfigAttr;
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
    static ConfigService config;

    @BeforeAll
    public static void setup() {
        app = new KarmaPlatform("/test-config.properties");
        config = new ConfigService(KConfigAttr.values());
    }

    @Test
    @Order(1)
    public void configurationHasProperties() {
        Assertions.assertNotNull(ConfigService.getAttributes());
    }

    @Test
    @Order(2)
    public void configurationHasExitMode() {
        config.parseArguments(List.of("exit=true"));
        boolean exit = ConfigService.get(KConfigAttr.APP_EXIT);
        Assertions.assertTrue(exit, "Configuration has no exit mode");
    }

    @Test
    @Order(3)
    public void configurationHasTestMode() {
        config.parseArguments(List.of("mode=true"));
        boolean testMode = ConfigService.get(KConfigAttr.TEST_MODE);
        Assertions.assertTrue(testMode, "Configuration has no test mode");
    }

    @Test
    @Order(4)
    public void configurationHasPhysicProperties() {
        config.loadFrom("/test-config.properties");
        Assertions.assertEquals(
            new Rectangle2D.Double(0.0, 0.0, 800.0, 600.0),
            ConfigService.get(KConfigAttr.WORLD_PLAY_AREA),
            "Configuration has no Play area defined !");
        Assertions.assertEquals(
            new KarmaPlatform.Vector2D(0.0, -0.00981),
            ConfigService.get(KConfigAttr.WORLD_GRAVITY),
            "Configuration has no gravity defined !");
        Assertions.assertEquals(
            new KarmaPlatform.Vector2D(0.2, 0.2),
            ConfigService.get(KConfigAttr.PHYSIC_VELOCITY_MAX),
            "Configuration has no velocity max defined !");
        Assertions.assertEquals(
            new KarmaPlatform.Vector2D(0.012, 0.012),
            ConfigService.get(KConfigAttr.PHYSIC_ACCELERATION_MAX),
            "Configuration has no acceleration max defined !");
        Assertions.assertEquals(5,
            (int) ConfigService.get(KConfigAttr.PHYSIC_PARTITION_MAX_LEVEL),
            "Configuration has no partitioning max level defined !");
        Assertions.assertEquals(
            5,
            (int) ConfigService.get(KConfigAttr.PHYSIC_PARTITION_MAX_CELL_PER_LEVEL),
            "Configuration has no max number of cells in a partition defined !");
    }

    @Test
    @Order(5)
    public void configurationHasDebugProperties() {
        config.loadFrom("/test-debug-config.properties");
        Assertions.assertEquals(4, (int) ConfigService.get(KConfigAttr.DEBUG_LEVEL));
        Assertions.assertEquals("filtered,entities,names", ConfigService.get(KConfigAttr.DEBUG_FILTER));
    }

    @Test
    @Order(6)
    public void configurationHasRenderingProperties() {
        config.loadFrom("/test-render-config.properties");
        Assertions.assertEquals(
            new Dimension(640, 400),
            ConfigService.get(KConfigAttr.APP_WINDOW_SIZE),
            "Configuration has no window size set");
        Assertions.assertEquals(
            new Dimension(320, 200),
            ConfigService.get(KConfigAttr.RENDERING_BUFFER_SIZE),
            "Configuration has no rendering buffer set");
        Assertions.assertEquals(
            3,
            (int) ConfigService.get(KConfigAttr.RENDERING_BUFFER_STRATEGY),
            "Configuration has no buffer strategy set");
    }

    @Test
    @Order(7)
    public void configurationHasSceneProperties() {

        config.loadFrom("/test-scene-config.properties");
        //# App scenes list and default.
        //app.scenes.list=test:my.karma.app.tests.scenes.TestScene,
        //app.scenes.default=test
        Assertions.assertEquals(
            "test",
            ConfigService.get(KConfigAttr.APP_SCENE_DEFAULT),
            "Configuration has no default Scene name set");
        String[] scenes = ConfigService.get(KConfigAttr.APP_SCENES_LIST);
        Assertions.assertEquals(
            "test:my.karma.app.tests.scenes.TestScene",
            scenes[0],
            "Configuration has no default Scene name set");

    }
}

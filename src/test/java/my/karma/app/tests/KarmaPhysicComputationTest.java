package my.karma.app.tests;

import my.karma.app.KarmaPlatform;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.awt.geom.Rectangle2D;

public class KarmaPhysicComputationTest {
    KarmaPlatform app;
    KarmaPlatform.Entity e1;

    @BeforeEach
    public void setup() {
        app = new KarmaPlatform("/test-physic-config.properties");
        app.init(new String[]{});
    }

    @AfterEach
    public void tearDown() {
        app.dispose();
    }


    @Test
    public void entityPhysicIsUpdatedTest() {
        app.getSceneManager().start("test");
        e1 = new KarmaPlatform.Entity("Test");

        KarmaPlatform.World world = app.getWorld();
        Rectangle2D playArea = world.getPlayArea();
        e1.setPosition(playArea.getX(), playArea.getY())
            .setSize(32, 32);
        app.updateEntity(10.0, e1);
        Assertions.assertEquals(e1.getPosition().getX(), app.getWorld().getPlayArea().getX());
        Assertions.assertEquals(e1.getPosition().getY(), app.getWorld().getPlayArea().getY());
        Assertions.assertEquals(e1.getPosition().getY(), app.getWorld().getPlayArea().getY());
        Assertions.assertEquals(0.0, e1.getVelocity().getX());
        Assertions.assertEquals(0.0, e1.getVelocity().getY());

    }

}

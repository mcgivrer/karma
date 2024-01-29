package my.karma.app.tests;

import my.karma.app.KarmaPlatform;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class KarmaSceneTest {
    static KarmaPlatform app;
    static KarmaPlatform.Scene test;
    static KarmaPlatform.SceneManager scm;

    @BeforeAll
    public static void setUp() {
        app = new KarmaPlatform("/test-config.properties");
        scm = app.getSceneManager();
        test = scm.getCurrent();
    }

    @AfterAll
    public static void tearDown() {
        app.dispose();
    }

    @org.junit.jupiter.api.Test
    public void karmaHasSceneTest() {
        app.run(new String[]{"app.test.mode=true"});
        Assertions.assertTrue(!scm.getScenes().isEmpty(), "Karma has no Scene !");
    }

    @Test
    public void karmaSceneHasEntitiesTest() {
        app.run(new String[]{"app.test.mode=true"});
        Assertions.assertTrue(!scm.getCurrent().getEntities().isEmpty(), "Scene has no entity !");
    }

    @Test
    public void karmaTestSceneHasEntityET01Test() {
        app.run(new String[]{"app.test.mode=true"});
        assertNotNull(scm.getCurrent().getEntities().stream().filter(e -> e.name.equals("et_01")).findFirst(), "Scene has no entity 'et_01'!");
    }
}
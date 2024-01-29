package my.karma.app.tests;

import my.karma.app.KarmaPlatform;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class KarmaSceneTest {
    KarmaPlatform app;
    KarmaPlatform.Scene test;
    KarmaPlatform.SceneManager scm;

    @Before
    public void setUp() {
        app = new KarmaPlatform("/test-config.properties");
        scm = app.getSceneManager();
        test = scm.getCurrent();
    }

    @After
    public void tearDown() {
        app.dispose();
    }
    @Test
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
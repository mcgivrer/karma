package my.karma.app.tests.scenes;

import my.karma.app.KarmaPlatform;

public class TestScene extends KarmaPlatform.AbstractScene {
    public TestScene(KarmaPlatform app) {
        super(app);
    }

    @Override
    public String getTitle() {
        return "test";
    }

    @Override
    public void create(KarmaPlatform app) {
        KarmaPlatform.Entity entity = new KarmaPlatform.Entity("et_01");
        addEntity(entity);
    }

    @Override
    public String getName() {
        return "test";
    }
}

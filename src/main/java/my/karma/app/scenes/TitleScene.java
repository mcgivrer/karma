package my.karma.app.scenes;

import my.karma.app.KarmaPlatform;

import java.awt.*;
import java.awt.event.KeyEvent;

public class TitleScene extends KarmaPlatform.AbstractScene {
    public TitleScene(KarmaPlatform app) {
        super(app);
    }

    @Override
    public String getTitle() {
        return "title";
    }

    @Override
    public void create(KarmaPlatform app) {
        Font fl = app.getGraphics().getFont().deriveFont(Font.BOLD, 18.0f);
        String titleMsg = app.getMessage("app.main.title");
        int titleWidth = app.getGraphics().getFontMetrics().stringWidth(titleMsg);
        KarmaPlatform.TextObject titleTxt = (KarmaPlatform.TextObject) new KarmaPlatform.TextObject("title")
                .setText(titleMsg)
                .setFont(fl)
                .setTextColor(Color.WHITE)
                .setPosition((int) ((app.getScreenSize().width - titleWidth) * 0.34), (int) (app.getScreenSize().height * 0.25))
                .setPhysicType(KarmaPlatform.PhysicType.NONE)
                .setPriority(1);
        addEntity(titleTxt);


        Font flMsg = app.getGraphics().getFont().deriveFont(Font.BOLD, 12.0f);
        String startMsg = app.getMessage("app.main.start");
        int startWidth = app.getGraphics().getFontMetrics().stringWidth(startMsg);
        KarmaPlatform.TextObject startTxt = (KarmaPlatform.TextObject) new KarmaPlatform.TextObject("startMessage")
                .setText(startMsg)
                .setFont(flMsg)
                .setTextColor(Color.WHITE)
                .setPosition((int) ((app.getScreenSize().width - startWidth) * 0.46), (int) (app.getScreenSize().height * 0.70))
                .setPhysicType(KarmaPlatform.PhysicType.NONE)
                .setPriority(1);
        addEntity(startTxt);

    }


    @Override
    public void input(KarmaPlatform app) {
        if (app.isKeyPressed(KeyEvent.VK_ENTER) || app.isKeyPressed(KeyEvent.VK_SPACE)) {
            app.getSceneManager().activate("play");
        }
    }
}

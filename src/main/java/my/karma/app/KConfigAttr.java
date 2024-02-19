package my.karma.app;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.function.Function;

public enum KConfigAttr {

    CONFIG_FILE_PATH("configFile",
        "cf",
        "app.config.filepath",
        "The properties file path to be loaded as configuration",
        (v) -> v,
        "/config.properties"),
    APP_TITLE("appTitle",
        "t",
        "app.title",
        "Set the application title",
        (v) -> v, "KarmaPlatform"),
    TEST_MODE("testMode",
        "mode",
        "app.test.mode",
        "Test mode only auto exit after initialization",
        Boolean::parseBoolean,
        false),
    APP_EXIT("exit",
        "exit",
        "app.exit",
        "Test mode only auto exit after initialization",
        Boolean::parseBoolean,
        false),
    TEST_MODE_COUNTER("testCounter",
        "tc",
        "app.test.looping.counter",
        "Define a number of loop execution during the main process.",
        Integer::parseInt, -1),
    DEBUG_LEVEL("debugLevel",
        "dl",
        "app.debug.level",
        "Set the level of debugging information on  log",
        Integer::parseInt, 0),
    DEBUG_FILTER("debugFilter",
        "df",
        "app.debug.filter",
        "Set the level of debugging information on  log",
        (v) -> v, ""),
    APP_WINDOW_SIZE("windowSize",
        "ws",
        "app.window.size",
        "Set the size of the game's window",
        KConfigAttr::getAsDimension, "640x400"),
    RENDERING_BUFFER_SIZE("renderBufferSize",
        "rbf",
        "app.rendering.buffer",
        "Set the size of the rendering buffer screen",
        KConfigAttr::getAsDimension, "320x200"),
    RENDERING_BUFFER_STRATEGY("bufferStrategy",
        "bs",
        "app.rendering.strategy",
        "Set the number of buffer into the rendering startegy",
        Integer::parseInt, "2"),
    WORLD_PLAY_AREA("worldPlayArea",
        "wpa",
        "app.physic.world.play.area",
        "Set the play area size",
        KConfigAttr::getAsRectangle2D, "1000x1000"),
    WORLD_GRAVITY("worldGravity",
        "wg",
        "app.physic.world.gravity",
        "Set the world gravity",
        KConfigAttr::getVector2D, "(0,-0.981)"),
    PHYSIC_ACCELERATION_MAX("accMax",
        "am",
        "app.physic.acceleration.max",
        "Set the physic engine maximum acceleration",
        KConfigAttr::getVector2D, "(0.012,0.012)"),
    PHYSIC_VELOCITY_MAX("velMax",
        "vm",
        "app.physic.velocity.max",
        "Set the physic engine maximum velocity",
        KConfigAttr::getVector2D, "(0.2,0.2)"),
    PHYSIC_PARTITION_MAX_LEVEL("partitionMaxLevel",
        "pml",
        "app.physic.partitioning.max.level",
        "Set the physic space partitioning maximum level",
        Integer::parseInt, "5"),
    PHYSIC_PARTITION_MAX_CELL_PER_LEVEL("partitionMaxCellPerLevel",
        "pmcpl",
        "app.physic.partitioning.max.node.per.level",
        "Set the physic space partitioning maximum cells per level",
        Integer::parseInt, "5"),
    APP_SCENES_LIST("appScenesList",
        "scnlist",
        "app.scenes.list",
        "Set the list of scene implementation (format: 'scn1:path.to.Scene1,scn2:path.to.Scene2,...')",
        (v) -> v.split(","), "test:SceneTest"),
    APP_SCENE_DEFAULT("appSceneDefault",
        "scndef",
        "app.scenes.default",
        "Set the default key scene to be activated from the scene list",
        (v) -> v, "test:SceneTest");

    private final String attrName;
    private final String cliArgName;
    private final String configAttrName;
    private final String helpDescription;
    private final Object defaultValue;
    private final Function<String, Object> parserFunction;

    KConfigAttr(String attrName,
                String cliArgName,
                String configAttrName,
                String HelpDescription,
                Function<String, Object> parserFunc,
                Object defaultValue) {
        this.attrName = attrName;
        this.cliArgName = cliArgName;
        this.configAttrName = configAttrName;
        this.helpDescription = HelpDescription;
        this.parserFunction = parserFunc;
        this.defaultValue = defaultValue;
    }

    public String getAttrName() {
        return attrName;
    }

    public String getCliArgName() {
        return cliArgName;
    }

    public String getConfigAttrName() {
        return configAttrName;
    }

    public String getHelpDescription() {
        return helpDescription;
    }

    public Function<String, Object> getParserFunction() {
        return parserFunction;
    }

    public Object getDefaultValue() {
        return this.defaultValue;
    }


    /**
     * Retrieve a value as Dimension from {@link ConfigService} from a string format "dim([width],[height])".
     *
     * @param value value of the configuration key to be loaded
     * @return the corresponding Dimension value
     */
    public static Dimension getAsDimension(String value) {
        String[] k = value.split("x");
        int width = Integer.valueOf(k[0]);
        int height = Integer.valueOf(k[1]);
        return new Dimension(width, height);
    }

    /**
     * Retrieve a value as {@link Rectangle2D} from {@link ConfigService} from a string format "[width]x[height]".
     *
     * @param value name of the configuration key to be loaded
     * @return the corresponding {@link Rectangle2D} value
     */
    public static Rectangle2D getAsRectangle2D(String value) {
        String[] k = value.split("x");
        double width = Double.valueOf(k[0]);
        double height = Double.valueOf(k[1]);
        return new Rectangle2D.Double(0, 0, width, height);
    }

    /**
     * Retrieve a value as {@link my.karma.app.KarmaPlatform.Vector2D} from {@link ConfigService} from a string format "([x],[y])".
     *
     * @param value name of the configuration key to be loaded
     * @return the corresponding {@link my.karma.app.KarmaPlatform.Vector2D} value
     */
    public static KarmaPlatform.Vector2D getVector2D(String value) {
        String[] vals = value.substring("(".length(), value.length() - ")".length()).split(",");
        return new KarmaPlatform.Vector2D(Double.parseDouble(vals[0]), Double.parseDouble(vals[1]));
    }
}

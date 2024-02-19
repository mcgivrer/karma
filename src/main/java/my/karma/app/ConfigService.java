package my.karma.app;

import java.io.IOException;
import java.util.*;

public class ConfigService {

    private static final Map<KConfigAttr, Object> configValues = new HashMap<>();
    private boolean found = false;
    private int debug;

    public ConfigService(KConfigAttr[] attributes) {
        loadDefaultConfigurationValues(attributes);
    }

    private void loadDefaultConfigurationValues(KConfigAttr[] values) {
        for (KConfigAttr ca : values) {
            configValues.put(ca, ca.getDefaultValue());
        }
    }

    public void parseArguments(List<String> lArgs) {
        lArgs.forEach(s -> {
            String[] keyValue = s.split("=");
            System.out.println(I18n.getMessage("app.message.execution.argument", keyValue[0], keyValue[1]));
            extractConfigurationValue(keyValue[0], keyValue[1]);
        });
    }

    private void extractConfigurationValue(String key, String value) {

        Arrays.stream(KConfigAttr.values()).forEach(ca -> {
            if (key.equals(ca.getAttrName()) || key.equals(ca.getConfigAttrName()) || key.equals(ca.getCliArgName())) {
                configValues.put(ca, ca.getParserFunction().apply(value));
                System.out.println(
                    I18n.getMessage("app.message.configuration.attribute",
                        ca.getAttrName(),
                        configValues.get(ca),
                        ca.getHelpDescription(),
                        ca.getDefaultValue())
                );
                found = true;
            }
        });
        debug = (int) configValues.get(KConfigAttr.DEBUG_LEVEL);
        if (!found) {
            System.out.println(I18n.getMessage("app.message.argument.unknown", key, value));
        }
    }

    public boolean loadFrom(String filePath) {
        boolean status = false;
        Properties config = new Properties();
        try {
            System.out.println(I18n.getMessage("app.message.configuration.load", filePath));
            config.load(this.getClass().getResourceAsStream(filePath));
            config.entrySet().stream()
                .filter(e -> e.getKey() != null)
                .forEach(e -> extractConfigurationValue((String) e.getKey(), (String) e.getValue()));
            status = true;
        } catch (IOException e) {
            System.out.println(I18n.getMessage("app.message.error.configuration.reading", e.getMessage()));
        }
        return status;
    }

    public void dispose() {
        configValues.clear();
    }

    public static <T> T get(KConfigAttr configAttribute) {
        return (T) configValues.get(configAttribute);
    }

    public <T> void setAttribute(KConfigAttr attrName, T attrValue) {
        configValues.put(attrName, attrValue);
    }

    public static Map<KConfigAttr, Object> getAttributes() {
        return configValues;
    }

}

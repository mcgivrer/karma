package my.karma.app;

import java.util.ResourceBundle;
import java.util.StringJoiner;
import java.util.StringTokenizer;

public class I18n {
    private static final ResourceBundle messages = ResourceBundle.getBundle("i18n.messages");

    private I18n() {
        // prevent from instantiate this utility class.
    }

    /**
     * return the I18n translated message entry for the <code>key</code>.
     *
     * @param keyMsg the key for translated I18n message to be retrieved.
     * @return the string corresponding to the I18n translated message.
     */
    public static String getMessage(String keyMsg) {
        return replaceTemplate(messages.getString(keyMsg), messages);
    }

    /**
     * return the I18n translated message entry for the <code>key</code> where <code>args</code> have been
     * replaced.
     *
     * @param keyMsg the key for translated I18n message to be retrieved.
     * @param args   an unlimited list of arguments to be used into the I18n translated messages.
     * @return the string corresponding to the I18n translated message with replaced arguments.
     */
    public static String getMessage(String keyMsg, Object... args) {
        return String.format(getMessage(keyMsg), args);
    }


    /**
     * Process template text to replace "${specific.key}" by their translated values.
     *
     * @param template the template text to be parsed and where keys must be translated.
     * @param values   the list of translated keys/values.
     * @return the key translated resulting message.
     */
    public static String replaceTemplate(String template, ResourceBundle values) {
        StringTokenizer tokenizer = new StringTokenizer(template, "${}", true);
        StringJoiner joiner = new StringJoiner("");

        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();

            if (token.equals("$")) {
                if (tokenizer.hasMoreTokens()) {
                    token = tokenizer.nextToken();

                    if (token.equals("{") && tokenizer.hasMoreTokens()) {
                        String key = tokenizer.nextToken();

                        if (tokenizer.hasMoreTokens() && tokenizer.nextToken().equals("}")) {
                            String value = values.containsKey(key) ? values.getString(key) : "${" + key + "}";
                            joiner.add(value);
                        }
                    }
                }
            } else {
                joiner.add(token);
            }
        }

        return joiner.toString();
    }
}
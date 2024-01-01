package my.applicationname.app;
   
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.ResourceBundle;

/**
 * main class for project Karma
 *
 * @author Frédéric Delorme
 * @since 1.0.0
 */
public class KarmaApp{

    private ResourceBundle messages = ResourceBundle.getBundle("i18n/messages");
    private Properties config = new Properties();
    private boolean exit = false;

    public KarmaApp(){
        System.out.println(String.format("Initialization application %s (%s)",
                messages.getString("app.name"),
                messages.getString("app.version")));    }

    public void run(String[] args){
        init(args);
        loop();
        dispose();
    }

    private void init(String[] args){
        List<String> lArgs = Arrays.asList(args);
        try {
            config.load(this.getClass().getResourceAsStream("/config.properties"));

            exit = Boolean.parseBoolean(config.getProperty("app.exit"));
        } catch (IOException e) {
            System.out.println(String.format("unable to read configuration file: %s", e.getMessage()));
        }

        lArgs.forEach(s -> {
            System.out.println(String.format("- arg: %s", s));
        });  
    }
    private void loop(){
        while(!exit){
            // will loop until exit=true or CTRL+C
        }
    }
    private void dispose(){
        System.out.println("End of application Karma");
    }

    public static void main(String[] argc){
        KarmaApp app = new KarmaApp();
        app.run(argc);
    }
}

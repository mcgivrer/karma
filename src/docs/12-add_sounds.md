# Adding some Sounds

A platform game can not exist without some lovely sounds and cheap tunes !

This is what we are going to add to the KarmaPlatform.

Java natively support sounds with SoundClip and is able to load some OS supported files.
Default OS sound file on the Windows Operating System is _.WAV_ file.

We must implement The simplest sound manager we can with Java audio API: `SoundManager`.

We need a simple internal API:

- load .WAV sound file,
- play a loaded sound,
- stop playing a sound,
- stop playing any currently playing sounds,
- release all resources.

This is corresponding to the following implementation :

![The SoundManager signature](http://www.plantuml.com/plantuml/svg/VOzDQiCm44RtEeMwSsbz0L6CIvV5fU0JJAknBHZze1a5GiZTunX38WXPlkyzXAPdWIp5atgH1JtgHE22-YTWrj-GFVmIo1IpLukpM4yQHBBxAuAi7scFiaJBUdzkZ2NO0GBCc4zAl_6LsGoGFaR9BiplN-HIjngAOBUw2U3Hx7PoX3ytVZM7wEnQ9u9ZLLUM9QRdzfDe2xZPvxDIzI5jsrM_Dtm7giTWYwSB "the SoundManager signature")

_figure 12.1 - the SoundManager signature_

Go to the code side now:

## SoundManager

the java class will be as simple as the following piece of code:

```java
public static class SoundManager {
    // <1>
    private KarmaPlatform app;
    // <2>
    private Map<String, Clip> soundClips;

    // <3>
    public SoundManager(KarmaPlatform app) {
        this.app = app;
        soundClips = new HashMap<>();
    }

    // <4>
    public void loadSound(String name, String filePath) {
        try {
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(Objects.requireNonNull(SoundManager.class.getResourceAsStream(filePath)));
            Clip clip = AudioSystem.getClip();
            clip.open(audioInputStream);
            soundClips.put(name, clip);
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            error("Unable to load sound %s from file %s: %s", name, filePath, e.getMessage());
        }
    }

    // <5>
    public void playSound(String name) {
        Clip clip = soundClips.get(name);
        if (clip != null) {
            if (!clip.isRunning())
                clip.setFramePosition(0);
            clip.start();
        }
    }

    // <6>
    public void stopSound(String name) {
        Clip clip = soundClips.get(name);
        if (clip != null && clip.isRunning()) {
            clip.stop();
            clip.setFramePosition(0);
        }
    }

    // <7>
    public void stopAllSounds() {
        for (Clip clip : soundClips.values()) {
            if (clip.isRunning()) {
                clip.stop();
                clip.setFramePosition(0);
            }
        }
    }
}
```

the class' attributes are:

1. `app` is the parent `KarmaPlatform` instance for this `SoundManager`
2. `soundClips` are the loaded java
   sound [javax.sound.sampled.Clip](https://docs.oracle.com/javase/8/docs/api/javax/sound/sampled/Clip.html "go to official JDK Clip documentation")
3. Create the instance of this service with the parent app instance,
4. load a specific .WAV sound file to the defined clip name,
5. plat the sound corresponding to the clip name,
6. Stop playing the clip name sound,
7. Stop playing all tge sounds.


And now, how to use it ?


## Usage

In the main KarmaPlatform initialization, we will add the SoundManager instance:

```java
public class KarmaPlatform   {
    //...
    // only one instance for the Sound Manager
    private static SoundManager soundManager;    
    //...
    private void init(String[] args) {
       //...   
       // prepare sound manager
       soundManager = new SoundManager(this);
    }
    //...
    
    public static SoundManager getSoundManager() {
        return soundManager;
    }
    //...
}
```


And to play a specific sound when _balls_ are colliding other `Entity` :

```java
public class PlaySoundOnCollisionBehavior implements KarmaPlatform.Behavior<KarmaPlatform.Entity> {
    private String soundName;
    private KarmaPlatform app;
    private List<KarmaPlatform.CollisionEvent> events = new ArrayList<>();
    // prepare
    public PlaySoundOnCollisionBehavior(KarmaPlatform app, String soundName) {
        this.app = app;
        this.soundName = soundName;
    }
    // play sound on collision
    @Override
    public void onCollision(KarmaPlatform.CollisionEvent ce) {
        if (ce.getDst().name.startsWith("platform")) {
            if (!events.contains(ce)) {
                events.add(ce);
                // play the sound name
                KarmaPlatform.getSoundManager().playSound(soundName);
            }
        }
    }
}
```

And the `PlayScene` implements the corresponding `Behavior` for all ball entity:

```java
public class PlayScene extends AbstractScene {
   //...
   @Override
   public void initialize(KarmaPlatform app) {
      //...
      // load all required Sound files for this PlayScene.
      getSoundManager().loadSound("click", "/res/sounds/clic.wav");
      getSoundManager().loadSound("toc", "/res/sounds/toc.wav");
      getSoundManager().loadSound("tic", "/res/sounds/tic.wav");
   }
    //...
    private void generateNRJBalls(String entityRootName, int nbBalls) {
       KarmaPlatform.Material ballMat = new KarmaPlatform.Material("BALL_MAT", 1.0, 1.0, 0.99);
       for (int i = 0; i < nbBalls; i++) {
          addEntity(
            new KarmaPlatform.Entity(entityRootName + "_" + i)
              //...
              .addBehavior(new PlaySoundOnCollisionBehavior(app, "toc"))
              //...
          );
       }
    }
   //...
}
```

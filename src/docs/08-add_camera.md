# Add Camera, Action !

## Required

Add a `Camera` Object to move game view according to a target position in the game play area. the target must be
an `Entity`.

- add a smooth effect between target and the `Camera` move.

![Adding a camera will help to implement a platformer]( https://docs.google.com/drawings/d/e/2PACX-1vSWff4JKLV2ZThVlxOQqF0Cnr-zMMUBiKa3b0NFGl_LuFx4HvWrp11RVMtupieJSdXqI6cU7wTCoK0B/pub?w=619&h=404 "Adding a camera will help to implement a platformer")

_figure 8.1 - What is a Camera in a game ?_

Here are:

- the play area at (0,0) with a size of 500 x 400,
- a player `Entity` at (p.x,p.y) with a size of 16x16,
- the `Camera` object is targeting the player entity with a specific tween factor used to add some elasticity and delay
  in
  the tracking operation.

## Implementation proposal

The Camera object will drive the rendering view port to follow a `GameObject` `target`, with a certain delay fixed by
the `tween` factor value.

### The Camera object

The `Camera` class will inherit from `Entity` and introduce 3 new attributes:

![The Camera mechanism implementation](https://www.plantuml.com/plantuml/png/LK_BJiCm4BpxAnPn0Q5UEFg0Ye0xeb_WsaDYoTv4zgQXGlrtd6ogYjlEU1wUVHLNPCh9t4cF31gJYspw640PqW-X6fzShVJ14kLCluRkAGPBaMKvsMLOEaY9t9dBfdrhP7647m67gxKvTzJQEeXLPYLcW6qOfcF2WayHypWKiL_muhXBU7vlkQqDvTjvqC63iNo3dgY3QDN3ezMZIMi_pAhQ5kWQPPn8YST9S9trUgnId1TzZdbCo62fgbzh_9fyAzYSdycTrQKx_sMfTKVL3mKsigmenHFgYYxcPj-za6PRRJ9xS9Xo-WC0
"The Camera mechanism implementation")

_figure 8.2 - Camera and Scene, how to interact with ?_

Some java code :

```java
public static class Camera extends Entity {
    private Entity target;
    private double tween;
    private Rectangle2D viewport;
}
```

All the mechanics is hidden in the update method:

In short the position of the camera is shortly delayed one based on the target position.
This delay is computed upon the tween factor that is set between 0.0 to 1.0.

```text
CameraPos = CameraPos + distance (CameraPos,TargetPos) * tween * dt
```

where

- `CameraPos` is the current camera position,
- `TargetPos` is the position of the tracked target,
- `tween` is the delay factor,
- `dt` is the elapsed time since previous call.

The corresponding simplified code :

```java
public static class Camera extends Entity {
    //...
    public void update(long dt) {
        this.position.x += Math
                .ceil((target.position.x + (target.w * 0.5) - ((viewport.getWidth()) * 0.5) - this.position.x)
                        * tween * Math.min(dt, 10));
        this.position.y += Math
                .ceil((target.position.y + (target.h * 0.5) - ((viewport.getHeight()) * 0.5) - this.position.y)
                        * tween * Math.min(dt, 10));

        this.viewport.setRect(this.position.x, this.position.y, this.viewport.getWidth(),
                this.viewport.getHeight());
    }
}
```

## Usage

TODO

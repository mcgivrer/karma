package my.karma.app.behaviors;

import my.karma.app.KarmaPlatform;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

public class WaterSimulationBehavior implements KarmaPlatform.Behavior<KarmaPlatform.Entity> {

    private KarmaPlatform.World world;
    private double waveResolution = 0.05;
    private double waterLevel = 0.05;
    private List<Shape> slices = new ArrayList<>();
    private List<Color> colors = new ArrayList<>();

    public WaterSimulationBehavior(double waveResolution, double waterLevel) {
        setWaterResolution(waveResolution);
        setWaterLevel(waterLevel);
    }

    public WaterSimulationBehavior(double waveResolution, double waterLevel, KarmaPlatform.World world) {
        Rectangle2D playArea = world.getPlayArea();
        setWaterResolution(waveResolution);
        setWaterLevel(waterLevel);
        setWaterSurface(playArea);
        this.world = world;
    }

    public WaterSimulationBehavior setWaterLevel(double waterLevel) {
        this.waterLevel = waterLevel;
        return this;
    }

    public WaterSimulationBehavior setWaterSurface(Rectangle2D playArea) {
        int nbSlices = (int) (playArea.getWidth() / (playArea.getWidth() * waveResolution));

        for (int i = 0; i < nbSlices; i += 1) {
            Polygon polygon = new Polygon();
            polygon.addPoint((int) (playArea.getWidth() * waveResolution * i), 0);
            polygon.addPoint((int) (playArea.getWidth() * waveResolution * (i + 1)), 0);
            polygon.addPoint((int) (playArea.getWidth() * waveResolution * (i + 1)), (int) (playArea.getHeight() * waterLevel));
            polygon.addPoint((int) (playArea.getWidth() * waveResolution * i), (int) (playArea.getHeight() * waterLevel));
            slices.add(polygon);
            colors.add(Color.GREEN);
        }
        return this;
    }

    public WaterSimulationBehavior setWaterResolution(double waveResolution) {
        this.waveResolution = waveResolution;
        return this;
    }

    @Override
    public void onUpdate(KarmaPlatform a, KarmaPlatform.Entity e, double d) {

    }

    @Override
    public void onDraw(KarmaPlatform a, Graphics2D g, KarmaPlatform.Entity e) {

        g.translate(e.getPosition().getX(), e.getPosition().getY());
        for (int ci = 0; ci < slices.size(); ci++) {
            Shape s = slices.get(ci);
            g.setColor(e.getBackgroundColor());
            g.fill(s);
            if (a.getDebugLevel() > 2) {
                g.setColor(colors.get(ci));
                g.draw(s);
            }
            e.box = e.box.createUnion(s.getBounds2D());
        }
        g.translate(-e.getPosition().getX(), -e.getPosition().getY());
    }

    @Override
    public void onCollision(KarmaPlatform.CollisionEvent ce) {
        if ("player_,ball_".contains(ce.getDst().name)) {
            KarmaPlatform.Entity collidingEntity = ce.getDst();
            for (int i = 0; i < colors.size(); i++) {
                colors.set(i, Color.GRAY);
            }
            double x = ce.getDst().getPosition().getX();
            int n = (int) (ce.getSrc().box.getWidth() / (ce.getSrc().box.getWidth() * waveResolution));
            double surface = ce.getPenetrationDepth() * ce.getDst().w;
            KarmaPlatform.Vector2D resultingForce =
                world.getGravity()
                    .multiply(-1.0 / ce.getDst().getMass())
                    .multiply(surface);
            ce.getDst().addForce(resultingForce);
            Shape s = slices.get(n - 1);
            colors.set(n - 1, Color.LIGHT_GRAY);
        }
    }
}

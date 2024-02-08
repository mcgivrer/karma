package my.karma.app.behaviors;

import my.karma.app.KarmaPlatform;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

public class WaterSimulationBehavior implements KarmaPlatform.Behavior<KarmaPlatform.Entity> {

    private double waveResolution = 0.05;
    private double waterLevel = 0.05;
    private List<Shape> slices = new ArrayList<>();

    public WaterSimulationBehavior(double waveResolution, double waterLevel) {
        setWaterResolution(waveResolution);
        setWaterLevel(waterLevel);
    }

    public WaterSimulationBehavior(double waveResolution, double waterLevel, KarmaPlatform.World world) {
        Rectangle2D playArea = world.getPlayArea();
        setWaterResolution(waveResolution);
        setWaterLevel(waterLevel);
        setWaterSurface(playArea);
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
        g.setColor(e.getForegroundColor());
        for (Shape s : slices) {
            g.fill(s);
        }
    }
}

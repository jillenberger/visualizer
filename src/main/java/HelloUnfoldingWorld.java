import de.fhpotsdam.unfolding.UnfoldingMap;
import de.fhpotsdam.unfolding.geo.Location;
import de.fhpotsdam.unfolding.providers.Google;
import de.fhpotsdam.unfolding.utils.MapUtils;
import processing.core.PApplet;

/**
 * Hello Unfolding World.
 * 
 * Download the distribution with examples for many more examples and features.
 */
public class HelloUnfoldingWorld extends PApplet {

	UnfoldingMap map;

	public void settings() {
		size(800, 600, "processing.opengl.PGraphics3D");
	}

	public void setup() {

		map = new UnfoldingMap(this, new Google.GoogleMapProvider());
		map.zoomAndPanTo(10, new Location(52.5f, 13.4f));

		MapUtils.createDefaultEventDispatcher(this, map);
	}

	public void draw() {
		background(0);
		map.draw();
	}

}

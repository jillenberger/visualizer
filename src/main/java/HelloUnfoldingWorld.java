import de.fhpotsdam.unfolding.UnfoldingMap;
import de.fhpotsdam.unfolding.data.Feature;
import de.fhpotsdam.unfolding.data.GeoJSONReader;
import de.fhpotsdam.unfolding.data.ShapeFeature;
import de.fhpotsdam.unfolding.geo.Location;
import de.fhpotsdam.unfolding.marker.Marker;
import de.fhpotsdam.unfolding.marker.SimpleLinesMarker;
import de.fhpotsdam.unfolding.providers.Google;
import de.fhpotsdam.unfolding.utils.MapUtils;
import processing.core.PApplet;

import java.util.ArrayList;
import java.util.List;

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

		List<Feature> features = GeoJSONReader.loadData(this, "/Users/jillenberger/work/matrix2014/data/network/network.geojson");

		List<Marker> markers = new ArrayList<Marker>(features.size());
		for (Feature feature : features) {
			ShapeFeature lineFeature = (ShapeFeature) feature;

			SimpleLinesMarker m = new SimpleLinesMarker(lineFeature.getLocations());
			m.setStrokeWeight(1);
			markers.add(m);
		}

		map.addMarkers(markers);
	}

	public void draw() {
		background(0);
		map.draw();
	}

}

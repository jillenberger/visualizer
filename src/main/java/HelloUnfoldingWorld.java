import com.github.davidmoten.rtree.Entry;
import com.github.davidmoten.rtree.RTree;
import com.github.davidmoten.rtree.geometry.*;
import de.fhpotsdam.unfolding.UnfoldingMap;
import de.fhpotsdam.unfolding.data.Feature;
import de.fhpotsdam.unfolding.data.GeoJSONReader;
import de.fhpotsdam.unfolding.data.ShapeFeature;
import de.fhpotsdam.unfolding.geo.Location;
import de.fhpotsdam.unfolding.marker.Marker;
import de.fhpotsdam.unfolding.marker.SimpleLinesMarker;
import de.fhpotsdam.unfolding.providers.Google;
import de.fhpotsdam.unfolding.utils.MapUtils;
import org.apache.log4j.Logger;
import processing.core.PApplet;
import processing.core.PShape;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Hello Unfolding World.
 * 
 * Download the distribution with examples for many more examples and features.
 */
public class HelloUnfoldingWorld extends PApplet {

	private static final Logger logger = Logger.getLogger(HelloUnfoldingWorld.class);

	UnfoldingMap map;

	private RTree<Marker, Line> rTree;

	private List<Marker> markers;

	public void settings() {
		size(800, 600, "processing.opengl.PGraphics3D");
	}

	public void setup() {

		map = new UnfoldingMap(this, new Google.GoogleMapProvider());
		map.zoomAndPanTo(10, new Location(52.5f, 13.4f));

		MapUtils.createDefaultEventDispatcher(this, map);

		logger.info("Loading geojson...");
		List<Feature> features = GeoJSONReader.loadData(this, "/Users/johannes/gsv/matrix2014/vis/network.geojson");

		logger.info("Building rtree...");
		rTree = RTree.star().maxChildren(6).create();

		markers = new ArrayList<Marker>(features.size());
		for (Feature feature : features) {
			ShapeFeature lineFeature = (ShapeFeature) feature;

			SimpleLinesMarker m = new SimpleLinesMarker(lineFeature.getLocations());
			m.setStrokeWeight(1);
			markers.add(m);

			Line line = Geometries.line(m.getLocation(0).getLat(),
					m.getLocation(0).getLon(),
					m.getLocation(1).getLat(),
					m.getLocation(1).getLon());
			rTree = rTree.add(m, line);
		}
		logger.info(String.format("Done adding %s lines.", features.size()));


//		map.addMarkers(markers);
	}

	boolean firstDraw = true;

	public void draw() {
//		logger.info("Drawing map");


		Location blLoc = map.mapDisplay.getLocationFromObjectPosition(0.0F, map.mapDisplay.getHeight());
		Location trLoc = map.mapDisplay.getLocationFromObjectPosition(map.mapDisplay.getWidth(), 0.0F);

//		logger.info(String.format("%s. %s, %s, %s", blLoc.getLat(), blLoc.getLon(), trLoc.getLat(), trLoc.getLon()));
//		Rectangle geometry = Geometries.rectangleGeographic(blLoc.getLon(), blLoc.getLat(), trLoc.getLon(), trLoc.getLat());
		Rectangle geometry = Geometries.rectangleGeographic(blLoc.getLat(),
				blLoc.getLon(),
				trLoc.getLat(),
				trLoc.getLon());

		List<Entry<Marker, Line>> it = rTree.search(geometry).toList().toBlocking().single();
//		Point point = Geometries.pointGeographic(52.5f, 13.4f);
//		Iterator<Entry<Marker, Line>> it = rTree.search(point, 1).toBlocking().toIterable().iterator();
//		List<Entry<Marker, Line>> it = rTree.entries().;

		background(0);
		map.draw();
		if(firstDraw) {
			for (Marker m : markers) m.draw(map);
			firstDraw = false;
		} else {
			for (Entry<Marker, Line> entry : it) {
				Marker m = entry.value();
				m.draw(map);
			}
			logger.info(String.format("Found %s lines", it.size()));
		}

//		int cnt = 0;
//		while(it.hasNext()) {
//			Marker m = it.next().value();
//			m.draw(map);
//			cnt++;
//		}
//		logger.info(String.format("Found %s lines", cnt));
	}

}

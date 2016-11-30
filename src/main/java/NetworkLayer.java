/* *********************************************************************** *
 * project: org.matsim.*
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2016 by the members listed in the COPYING,        *
 *                   LICENSE and WARRANTY file.                            *
 * email           : info at matsim dot org                                *
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *   See also COPYING, LICENSE and WARRANTY file                           *
 *                                                                         *
 * *********************************************************************** */

import com.github.davidmoten.rtree.Entry;
import com.github.davidmoten.rtree.RTree;
import com.github.davidmoten.rtree.geometry.Geometries;
import com.github.davidmoten.rtree.geometry.Line;
import com.github.davidmoten.rtree.geometry.Rectangle;
import de.fhpotsdam.unfolding.UnfoldingMap;
import de.fhpotsdam.unfolding.data.Feature;
import de.fhpotsdam.unfolding.data.ShapeFeature;
import de.fhpotsdam.unfolding.events.MapEvent;
import de.fhpotsdam.unfolding.events.MapEventListener;
import de.fhpotsdam.unfolding.geo.Location;
import de.fhpotsdam.unfolding.marker.Marker;
import de.fhpotsdam.unfolding.marker.MarkerManager;
import de.fhpotsdam.unfolding.marker.SimpleLinesMarker;
import gnu.trove.list.array.TDoubleArrayList;
import gnu.trove.map.TObjectDoubleMap;
import gnu.trove.map.hash.TObjectDoubleHashMap;
import org.apache.log4j.Logger;
import org.matsim.contrib.common.stats.Discretizer;
import org.matsim.contrib.common.stats.FixedSampleSizeDiscretizer;
import processing.core.PApplet;
import rx.Observable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author johannes
 */
public class NetworkLayer implements Layer, MapEventListener {

    private static final Logger logger = Logger.getLogger(NetworkLayer.class);

    private UnfoldingMap map;

    private MarkerManager mManager;

    private RTree<Marker, Line> rTree;

    public NetworkLayer(HelloUnfoldingWorld applet, Collection<Feature> features) {
        this.map = applet.map;
        mManager = new MarkerManager();
        map.addMarkerManager(mManager);

        rTree = RTree.star().maxChildren(6).create();

        TObjectDoubleMap<String> volumes = getAttributeValues(features, "volume");
        Discretizer volumeCategories = FixedSampleSizeDiscretizer.create(volumes.values(), 1, 10);

        TObjectDoubleMap<String> capacities = getAttributeValues(features, "capacity");
        Discretizer capacityCategories = FixedSampleSizeDiscretizer.create(capacities.values(), 1, 10);

        for (Feature feature : features) {
            ShapeFeature lineFeature = (ShapeFeature) feature;

            SimpleLinesMarker m = new SimpleLinesMarker(lineFeature.getLocations());
            m.setId(lineFeature.getId());

            double capacity = capacities.get(feature.getId());
            int capacityCategory = capacityCategories.index(capacity);
            m.setStrokeWeight(capacityCategory + 1);

            double volume = volumes.get(feature.getId());
            int volumeCategory = volumeCategories.index(volume);
            int alpha = 255 - (volumeCategory * 20);
            m.setColor(applet.color(237, 181, 0, alpha));

            Line line = Geometries.line(m.getLocation(0).getLat(),
                    m.getLocation(0).getLon(),
                    m.getLocation(1).getLat(),
                    m.getLocation(1).getLon());

            rTree = rTree.add(m, line);
        }
    }

    public String getId() {
        return null;
    }

    public void onManipulation(MapEvent event) {
//        logger.info("onManipulation");
//        if(searchThread != null) {
//            searchThread.interrupt();
//        }
//
//        searchThread = new SearchThread();
//        searchThread.start();
    }

    public void draw() {
        //do nothing
    }

    public void update() {
        Location bottomLeft = map.mapDisplay.getLocationFromObjectPosition(0.0F, map.mapDisplay.getHeight());
        Location topRight = map.mapDisplay.getLocationFromObjectPosition(map.mapDisplay.getWidth(), 0.0F);

        Rectangle geometry = Geometries.rectangleGeographic(bottomLeft.getLat(),
                bottomLeft.getLon(),
                topRight.getLat(),
                topRight.getLon());

        Observable<Entry<Marker, Line>> result = rTree.search(geometry);
        List<Entry<Marker, Line>> entries = new ArrayList<Entry<Marker, Line>>(result.toList().toBlocking().single());
        List<Marker> markers = new ArrayList<Marker>(entries.size());
        for(Entry<Marker, Line> entry : entries) markers.add(entry.value());
        updateMarkers(markers);
    }

    private synchronized void updateMarkers(List<Marker> markers) {
        logger.info("updateMarkers");
        mManager.clearMarkers();
        mManager.addMarkers(markers);
    }

    private TObjectDoubleMap<String> getAttributeValues(Collection<Feature> features, String key) {
        TObjectDoubleMap<String> values = new TObjectDoubleHashMap<String>(features.size());

        for(Feature feature : features) {
            Object value = feature.getProperty(key);
            if(value != null) {
                if (value instanceof Double) values.put(feature.getId(), (Double) value);
                else if (value instanceof String) values.put(feature.getId(), Double.parseDouble((String) value));
                else logger.warn("Unknown class.");
            }
        }

        return values;
    }


}

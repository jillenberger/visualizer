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

package de.db.dbanalytics.vis.util;

import com.github.filosganga.geogson.gson.GeometryAdapterFactory;
import com.github.filosganga.geogson.model.Coordinates;
import com.github.filosganga.geogson.model.Feature;
import com.github.filosganga.geogson.model.LineString;
import com.github.filosganga.geogson.model.positions.LinearPositions;
import com.github.filosganga.geogson.model.positions.SinglePosition;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.stream.JsonWriter;
import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.network.io.NetworkReaderMatsimV1;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.collections.Tuple;
import org.matsim.core.utils.geometry.CoordinateTransformation;
import org.matsim.core.utils.geometry.transformations.TransformationFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author johannes
 */
public class MatsimNetwork2GeoJSON {

    private static final Logger logger = Logger.getLogger(MatsimNetwork2GeoJSON.class);

    public static void main(String args[]) throws IOException {
        String networkFile = args[0];
        String crsId = args[1];
        String attributesFile = args[2];
        String jsonFile = args[3];
        /*
        Load network...
         */
        logger.info("Loading network....");
        Scenario scenario = ScenarioUtils.createScenario(ConfigUtils.createConfig());
        CoordinateTransformation transform = TransformationFactory.getCoordinateTransformation("EPSG:" + crsId, TransformationFactory.WGS84);
        NetworkReaderMatsimV1 reader = new NetworkReaderMatsimV1(transform, scenario.getNetwork());
        reader.readFile(networkFile);
        /*
        Load attributes...
         */
        logger.info("Loading link attributes...");
        Map<String, List<Tuple<String, String>>> linkAttrs = new HashMap<String, List<Tuple<String, String>>>();
        BufferedReader attrsReader = new BufferedReader(new FileReader(attributesFile));
        String header[] = attrsReader.readLine().split("\t");

        String line = null;
        while((line = attrsReader.readLine()) != null) {
            String tokens[] = line.split("\t");
            String id = tokens[0];
            List<Tuple<String, String>> attrs = new ArrayList<Tuple<String, String>>();
            for(int i = 1 ; i < tokens.length; i++) {
                attrs.add(new Tuple<String, String>(header[i], tokens[i]));
            }
            linkAttrs.put(id, attrs);
        }
        /*
        Write geojson...
         */
        logger.info("Writing geojson...");
        write(scenario.getNetwork(), linkAttrs, jsonFile);
        logger.info("Done.");
    }

    public static void write(Network network, Map<String, List<Tuple<String, String>>> linkAttrs, String filename) throws IOException {
        /*
        Open writers.
         */
        BufferedWriter fileWriter = new BufferedWriter(new FileWriter(filename));
        JsonWriter jsonWriter = new JsonWriter(fileWriter);
        /*
        Begin json document.
         */
        jsonWriter.beginObject();
        jsonWriter.name("type").value("FeatureCollection");

        jsonWriter.name("features");
        jsonWriter.beginArray();
        /*
        Create json builder.
         */
        Gson gson = new GsonBuilder()
                .registerTypeAdapterFactory(new GeometryAdapterFactory())
                .setPrettyPrinting()
                .create();

        List<Link> links = new ArrayList<Link>(network.getLinks().values());

        for(int i = 0; i < links.size(); i++) {
            Link link = links.get(i);
            /*
            Create geometry.
             */
            Coordinates fromCoord = Coordinates.of(
                    link.getFromNode().getCoord().getX(),
                    link.getFromNode().getCoord().getY());
            Coordinates toCoord = Coordinates.of(
                    link.getToNode().getCoord().getX(),
                    link.getToNode().getCoord().getY());

            ImmutableList<SinglePosition> positions = ImmutableList.<SinglePosition>builder()
                    .add(new SinglePosition(fromCoord))
                    .add(new SinglePosition(toCoord))
                    .build();

            LineString line = new LineString(new LinearPositions(positions));
            /*
            Build attributes.
             */
            Map<String, JsonElement> jsonAttrs = new HashMap<String, JsonElement>();
            jsonAttrs.put("capacity", new JsonPrimitive(link.getCapacity()));
            jsonAttrs.put("lanes", new JsonPrimitive(link.getNumberOfLanes()));
            jsonAttrs.put("freespeed", new JsonPrimitive(link.getFreespeed()));

            List<Tuple<String, String>> attrsList = linkAttrs.get(link.getId().toString());
            if(attrsList != null) {
                for(Tuple<String, String> attr : attrsList)
                    jsonAttrs.put(attr.getFirst(), new JsonPrimitive(attr.getSecond()));
            }

            ImmutableMap<String, JsonElement> attrs = ImmutableMap.<String, JsonElement>builder().putAll(jsonAttrs).build();
            /*
            Build and write feature.
             */
            Feature feature = new Feature(line, attrs, Optional.of(link.getId().toString()));
            gson.toJson(feature, fileWriter);
            /*
            Write separator.
             */
            if(i < links.size() - 1) fileWriter.write(",");

            fileWriter.newLine();
        }
        /*
        End json document.
         */
        jsonWriter.endArray();
        jsonWriter.endObject();

        fileWriter.close();
    }
}

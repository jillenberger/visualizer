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
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonWriter;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.utils.collections.Tuple;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

/**
 * @author johannes
 */
public class MatsimNetwork2GeoJSON {

    public static void main(String args[]) {
        String networkFile = args[0];
        String crsId = args[1];
        String attributesFile = args[2];
        String jsonFile = args[3];
    }

    public static void write(Network network, Map<String, Tuple<String, String>> attributes, String filename) throws IOException {
        Gson gson = new GsonBuilder()
                .registerTypeAdapterFactory(new GeometryAdapterFactory())
                .setPrettyPrinting()
                .create();

        BufferedWriter fileWriter = new BufferedWriter(new FileWriter(filename));
        JsonWriter jsonWriter = new JsonWriter(fileWriter);

        jsonWriter.beginObject();
        jsonWriter.name("type").value("FeatureCollection");

        jsonWriter.name("features");
        jsonWriter.beginArray();

        for(Link link : network.getLinks().values()) {

        }

        jsonWriter.endArray();
        jsonWriter.endObject();

    }
}

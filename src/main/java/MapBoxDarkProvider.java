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

import de.fhpotsdam.unfolding.core.Coordinate;
import de.fhpotsdam.unfolding.providers.OpenStreetMap;

/**
 * @author johannes
 */
public class MapBoxDarkProvider extends OpenStreetMap.GenericOpenStreetMapProvider {

    public static final String URL = "http://api.mapbox.com/v4/mapbox.dark/";

    public static final String TOKEN = "access_token=sk.eyJ1Ijoibmljb2t1ZWhuZWwiLCJhIjoiY2l1OW5yeG14MDAxaTJ6bGk5YXRjc3FrbCJ9.W6S9QbS7byui-nhNs95gXw";

    private static final String PNG = ".png?";

    public String[] getTileUrls(Coordinate coordinate) {
        StringBuilder builder = new StringBuilder(1024);
        builder.append(URL);
        builder.append(getZoomString(coordinate));
        builder.append(PNG);
        builder.append(TOKEN);
        return new String[] {builder.toString()};
    }
}

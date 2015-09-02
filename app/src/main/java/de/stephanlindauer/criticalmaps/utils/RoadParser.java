package de.stephanlindauer.criticalmaps.utils;

import android.content.Context;

import org.osmdroid.bonuspack.routing.OSRMRoadManager;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.util.GeoPoint;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class RoadParser {

    public static Road getRoadFor(Context context, int resourceId) {
        InputStream inputStream = context.getResources().openRawResource(resourceId);
        BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
        String readLine = null;

        ArrayList<GeoPoint> waypoints = new ArrayList<>();

        try {
            while ((readLine = br.readLine()) != null) {
                GeoPoint coord = parseLine(readLine);
                waypoints.add(coord);
            }

            inputStream.close();
            br.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        RoadManager roadManager = new OSRMRoadManager();
        Road road = roadManager.getRoad(waypoints);

        return road;
    }

    private static GeoPoint parseLine(String readLine) {
        String[] splitLine = readLine.split(",");

        String latitudeRaw = splitLine[1];
        String longitudeRaw = splitLine[0];

        int latitude = parseCoordParameter(latitudeRaw);
        int longitude = parseCoordParameter(longitudeRaw);

        return new GeoPoint(latitude, longitude);
    }

    private static int parseCoordParameter(String coordString) {
        coordString = coordString.replaceAll("\\.", "");

        coordString = padCoordStringTo8Digits(coordString);

        int coordInt = Integer.parseInt(coordString);
        return coordInt;
    }

    private static String padCoordStringTo8Digits(String coordString) {
        int digitsMissing = 8 - coordString.length();
        while (digitsMissing > 0) {
            coordString += "0";
            digitsMissing--;
        }
        return coordString;
    }
}

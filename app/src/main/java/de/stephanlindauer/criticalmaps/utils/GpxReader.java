/*
package de.stephanlindauer.criticalmaps.utils;

import org.jetbrains.annotations.NotNull;
import org.osmdroid.util.GeoPoint;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import de.stephanlindauer.criticalmaps.model.gpx.GpxModel;
import de.stephanlindauer.criticalmaps.model.gpx.GpxPoi;
import de.stephanlindauer.criticalmaps.model.gpx.GpxTrack;

public class GpxReader {

    private static final String ELEMENT_TRK = "trk";
    private static final String ELEMENT_NAME = "name";
    private static final String ELEMENT_TRKSEG = "trkseg";
    private static final String ELEMENT_TRKPT = "trkpt";
    private static final String ATTRIBUTE_LAT = "lat";
    private static final String ATTRIBUTE_LON = "lon";
    private static final String ELEMENT_ELE = "ele";
    private static final String ELEMENT_WPT = "wpt";

    private final GpxModel gpxModel;

    @Inject
    public GpxReader(GpxModel gpxModel) {
        this.gpxModel = gpxModel;
    }

    public void readDataFromStream(InputStream gpxInputStream, String uri) throws IOException, SAXException, ParserConfigurationException {
        gpxModel.clear();
        readGpxFile(gpxInputStream);
        gpxModel.setUri(uri);
    }

    private void readGpxFile(InputStream gpxInputStream) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        Document gpxDocument = documentBuilder.parse(gpxInputStream);
        Element gpxElement = gpxDocument.getDocumentElement();
        readTracks(gpxElement);
        readWaypoints(gpxElement);
    }

    private void readWaypoints(Element gpxElement) {
        NodeList wptList = gpxElement.getElementsByTagName(ELEMENT_WPT);
        for (int i = 0; i < wptList.getLength(); i++) {
            Element wpt = (Element) wptList.item(i);
            GeoPoint location = parsePoint(wpt);
            String pointName = parseName(wpt);
            gpxModel.getPoiList().add(new GpxPoi(pointName, location));
        }
    }

    private void readTracks(Element gpxElement) {
        NodeList trkList = gpxElement.getElementsByTagName(ELEMENT_TRK);
        for (int i = 0; i < trkList.getLength(); i++) {
            Element track = (Element) trkList.item(i);
            List<GeoPoint> trackPoints = getTrackPoints(track);
            String trackName = parseName(track);
            gpxModel.getTracks().add(new GpxTrack(trackName, trackPoints));
        }
    }

    @NotNull
    private List<GeoPoint> getTrackPoints(Element track) {
        List<GeoPoint> trackPoints = new ArrayList<>();
        NodeList trksegList = track.getElementsByTagName(ELEMENT_TRKSEG);
        for (int j = 0; j < trksegList.getLength(); j++) {
            Element trkseg = (Element) trksegList.item(j);
            NodeList trkptList = trkseg.getElementsByTagName(ELEMENT_TRKPT);
            for (int k = 0; k < trkptList.getLength(); k++) {
                Element trkpt = (Element) trkptList.item(k);
                trackPoints.add(parsePoint(trkpt));
            }
        }
        return trackPoints;
    }

    private String parseName(Element track) {
        NodeList nameList = track.getElementsByTagName(ELEMENT_NAME);
        if (nameList.getLength() > 0) {
            return nameList.item(0).getTextContent();
        }
        return null;
    }

    @NotNull
    private GeoPoint parsePoint(Element point) {
        GeoPoint newPoint;
        double lat = Double.parseDouble(point.getAttributes().getNamedItem(ATTRIBUTE_LAT).getNodeValue());
        double lon = Double.parseDouble(point.getAttributes().getNamedItem(ATTRIBUTE_LON).getNodeValue());

        NodeList eleList = point.getElementsByTagName(ELEMENT_ELE);
        if (eleList.getLength() > 0) {
            double ele = Double.parseDouble(eleList.item(0).getTextContent());
            newPoint = new GeoPoint(lat, lon, ele);
        } else {
            newPoint = new GeoPoint(lat, lon);
        }
        return newPoint;
    }

}
*/

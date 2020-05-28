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

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import de.stephanlindauer.criticalmaps.model.gpx.GpxModel;
import de.stephanlindauer.criticalmaps.model.gpx.GpxTrack;

public class GpxReader {

    private static final String ELEMENT_TRK = "trk";
    private static final String ELEMENT_NAME = "name";
    private static final String ELEMENT_TRKSEG = "trkseg";
    private static final String ELEMENT_TRKPT = "trkpt";
    private static final String ATTRIBUTE_LAT = "lat";
    private static final String ATTRIBUTE_LON = "lon";
    private static final String ELEMENT_ELE = "ele";

    private GpxReader() {
    }

    public static void readTrackFromGpx(InputStream gpxInputStream, GpxModel gpxModel, String uri) {
        gpxModel.clear();
        try {
            readGpxFile(gpxInputStream, gpxModel);
        } catch (ParserConfigurationException | IOException | SAXException e) {
            e.printStackTrace();
            // TODO
        }
        gpxModel.setUri(uri);
    }

    private static void readGpxFile(InputStream gpxInputStream, GpxModel gpxModel) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        Document gpxDocument = documentBuilder.parse(gpxInputStream);

        Element gpxElement = gpxDocument.getDocumentElement();
        readTracks(gpxModel, gpxElement);
        readWaypoints(gpxModel, gpxElement);
    }

    private static void readWaypoints(GpxModel gpxModel, Element gpxElement) {

    }

    private static void readTracks(GpxModel gpxModel, Element gpxElement) {
        NodeList trkList = gpxElement.getElementsByTagName(ELEMENT_TRK);
        for (int i = 0; i < trkList.getLength(); i++) {
            Element track = (Element) trkList.item(i);
            List<GeoPoint> trackPoints = getTrackPoints(track);
            NodeList nameList = track.getElementsByTagName(ELEMENT_NAME);
            String trackName = null;
            if (nameList.getLength() > 0) {
                trackName = nameList.item(0).getTextContent();
            }
            gpxModel.getTracks().add(new GpxTrack(trackName, trackPoints));
        }
    }

    @NotNull
    private static List<GeoPoint> getTrackPoints(Element track) {
        List<GeoPoint> trackPoints = new ArrayList<>();
        NodeList trksegList = track.getElementsByTagName(ELEMENT_TRKSEG);
        for (int j = 0; j < trksegList.getLength(); j++) {
            Element trkseg = (Element) trksegList.item(j);
            NodeList trkptList = trkseg.getElementsByTagName(ELEMENT_TRKPT);
            for (int k = 0; k < trkptList.getLength(); k++) {
                Element trkpt = (Element) trkptList.item(k);
                trackPoints.add(parseTrackpoint(trkpt));
            }
        }
        return trackPoints;
    }

    @NotNull
    private static GeoPoint parseTrackpoint(Element trkpt) {
        GeoPoint newPoint;
        double lat = Double.parseDouble(trkpt.getAttributes().getNamedItem(ATTRIBUTE_LAT).getNodeValue());
        double lon = Double.parseDouble(trkpt.getAttributes().getNamedItem(ATTRIBUTE_LON).getNodeValue());

        NodeList eleList = trkpt.getElementsByTagName(ELEMENT_ELE);
        if (eleList.getLength() > 0) {
            double ele = Double.parseDouble(eleList.item(0).getTextContent());
            newPoint = new GeoPoint(lat, lon, ele);
        } else {
            newPoint = new GeoPoint(lat, lon);
        }
        return newPoint;
    }

}

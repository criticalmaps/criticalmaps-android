package de.stephanlindauer.criticalmaps.model.gpx;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class GpxModel {

    private String uri;
    private List<GpxTrack> tracks = new ArrayList<>();
    private List<GpxPoi> poiList = new ArrayList<>();

    @Inject
    public GpxModel() {
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public List<GpxTrack> getTracks() {
        return tracks;
    }

    public void setTracks(List<GpxTrack> tracks) {
        this.tracks = tracks;
    }

    public List<GpxPoi> getPoiList() {
        return poiList;
    }

    public void setPoiList(List<GpxPoi> poiList) {
        this.poiList = poiList;
    }

    public void clear() {
        tracks.clear();
        poiList.clear();
        uri = null;
    }
}

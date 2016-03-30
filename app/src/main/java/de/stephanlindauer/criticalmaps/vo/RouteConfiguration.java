package de.stephanlindauer.criticalmaps.vo;

public class RouteConfiguration {
    private final int resourceId;
    private final String name;
    private final String category;

    public RouteConfiguration(String category, String name, int resourceId) {
        this.category = category;
        this.name = name;
        this.resourceId = resourceId;
    }
}

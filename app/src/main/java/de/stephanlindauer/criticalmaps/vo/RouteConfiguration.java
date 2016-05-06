package de.stephanlindauer.criticalmaps.vo;

public class RouteConfiguration {
    private final int resourceId;
    private final String name;

    public RouteConfiguration(String name, int resourceId) {
        this.name = name;
        this.resourceId = resourceId;
    }

    public String getName() {
        return name;
    }
}

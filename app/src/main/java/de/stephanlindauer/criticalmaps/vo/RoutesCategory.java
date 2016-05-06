package de.stephanlindauer.criticalmaps.vo;


import java.util.ArrayList;

public class RoutesCategory {
    private final String name;
    private ArrayList<RouteConfiguration> routeConfigurations= new ArrayList<RouteConfiguration>();

    public RoutesCategory(String name) {
        this.name = name;
    }

    public RoutesCategory add(RouteConfiguration routeConfiguration) {
        routeConfigurations.add(routeConfiguration);
        return this;
    }

    public ArrayList<RouteConfiguration> getRouteConfigurations(){
        return routeConfigurations;
    }

    public String getName() {
        return name;
    }
}

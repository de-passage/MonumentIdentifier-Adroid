package com.example.watchapplication;

public class Coordinate {
    double lat;
    double lon;

    public Coordinate(double lat, double lon) {
        this.lat = lat;
        this.lon = lon;
    }

    @Override
    public String toString() {
        return lat + "," + lon;
    }
}

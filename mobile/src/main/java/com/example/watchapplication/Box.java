package com.example.watchapplication;

public class Box {
    double left;
    double bottom;
    double right;
    double top;

    public Box(double left, double bottom, double right, double top) {
        this.left = left;
        this.bottom = bottom;
        this.right = right;
        this.top = top;
    }

    @Override
    public String toString() {
        return left + "," + bottom + "," + right + "," + top;
    }
}

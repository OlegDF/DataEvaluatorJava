package com.DataObjects;

public class Slice {

    public final String[] colNames;
    public final String[] labels;
    public final SlicePoint[] points;

    public Slice(String[] colNames, String[] labels) {
        this.colNames = colNames;
        this.labels = labels;
        this.points = new SlicePoint[0];
    }

    public Slice(String[] colNames, String[] labels, SlicePoint[] points) {
        this.colNames = colNames;
        this.labels = labels;
        this.points = points;
    }

    public Slice getAccumulation() {
        SlicePoint[] pointsAccumulated = new SlicePoint[points.length];
        long accumulatedValue = 0;
        for(int i = 0; i < points.length; i++) {
            accumulatedValue += points[i].value * points[i].amount;
            pointsAccumulated[i] = new SlicePoint(accumulatedValue, 1, points[i].date);
        }
        return new Slice(colNames, labels, pointsAccumulated);
    }

}

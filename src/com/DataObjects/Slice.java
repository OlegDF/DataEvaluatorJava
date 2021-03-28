package com.DataObjects;

import java.math.BigDecimal;

public class Slice {

    public final String[] colNames;
    public final String[] labels;
    public final SlicePoint[] points;
    public final long valueRange;

    public Slice(String[] colNames, String[] labels) {
        this.colNames = colNames;
        this.labels = labels;
        this.points = new SlicePoint[0];
        this.valueRange = getValueRange();
    }

    public Slice(String[] colNames, String[] labels, SlicePoint[] points) {
        this.colNames = colNames;
        this.labels = labels;
        this.points = points;
        this.valueRange = getValueRange();
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

    public boolean isIntervalDecreasing(int pos1, int pos2) {
        return points[pos2].value < points[pos1].value;
    }

    public double getDecreaseScore(int pos1, int pos2) {
        if(pos1 < 0 || pos1 >= points.length || pos2 < 0 || pos2 >= points.length) {
            return -1;
        }
        double res = (double)(points[pos2].value - points[pos1].value) * (points[pos2].value - points[pos1].value) *
                (points[pos2].date.getTime() - points[pos1].date.getTime()) /
                (points[points.length - 1].date.getTime() - points[0].date.getTime()) / valueRange / valueRange;
        return res;
    }

    private long getValueRange() {
        if(points.length == 0) {
            return 0;
        }
        long min = points[0].value * points[0].amount;
        long max = points[0].value * points[0].amount;
        for(SlicePoint point: points) {
            if(point.value < min) {
                min = point.value * point.amount;
            }
            if(point.value > max) {
                max = point.value * point.amount;
            }
        }
        return max - min;
    }

}

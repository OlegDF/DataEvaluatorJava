package com.Model.Intervals;

import com.DataObjects.Slice;
import com.DataObjects.SuspiciousInterval;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class SimpleIntervalFinder implements IntervalFinder {

    public SimpleIntervalFinder() {}

    @Override
    public List<SuspiciousInterval> getDecreasingIntervals(List<Slice> slices) {
        List<SuspiciousInterval> res = new ArrayList<>();
        for(Slice slice: slices) {
            final int chunkLength = Integer.max(slice.points.length / 16, 1);
            final int maxIntervalLength = slice.points.length / 4;
            for(int pos1 = 0; pos1 < slice.points.length - 1; pos1 += chunkLength) {
                for(int pos2 = pos1 + 1; pos2 < slice.points.length && pos2 <= pos1 + maxIntervalLength; pos2 += chunkLength) {
                    if(slice.isIntervalDecreasing(pos1, pos2)) {
                        res.add(new SuspiciousInterval(slice, pos1, pos2));
                    }
                }
            }
        }
        res.sort(Comparator.comparing(SuspiciousInterval::getDecreaseScore).reversed());
        return res.subList(0, 32);
    }

}

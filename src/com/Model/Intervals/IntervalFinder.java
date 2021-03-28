package com.Model.Intervals;

import com.DataObjects.Slice;
import com.DataObjects.SuspiciousInterval;

import java.util.List;

public interface IntervalFinder {

    public List<SuspiciousInterval> getDecreasingIntervals(List<Slice> slices);

}

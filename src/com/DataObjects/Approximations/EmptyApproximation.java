package com.DataObjects.Approximations;

import com.DataObjects.Slice;

/**
 * Базовая функция приближения, которая равна значениям среза на каждой точке.
 */
public class EmptyApproximation implements Approximation {

    public EmptyApproximation() {
    }

    @Override
    public long getApproximate(Slice slice, int pos) {
        return slice.points[pos].value;
    }

    @Override
    public double getSigma() {
        return 0;
    }

    @Override
    public ApproximationType getType() {
        return ApproximationType.EMPTY;
    }

    @Override
    public double getAngleMultiplier(Slice slice) {
        return (double) (slice.points[slice.points.length - 1].value - slice.points[0].value) / slice.dateRange;
    }


}

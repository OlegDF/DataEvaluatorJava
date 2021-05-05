package com.DataObjects.Approximations;

import com.DataObjects.Slice;

/**
 * Функция приближения среза, на которой значение в точке i равно среднему значению всех точек от i-k до i+k
 * (k - ширина интервала усреднения)
 */
public class AveragesApproximation implements Approximation {

    private long[] approximateValues;
    private double sigma;

    public AveragesApproximation(Slice slice) {
        calculateApproximation(slice);
    }

    @Override
    public long getApproximate(Slice slice, int pos) {
        return approximateValues[pos];
    }

    @Override
    public double getSigma() {
        return sigma;
    }

    @Override
    public ApproximationType getType() {
        return ApproximationType.AVERAGES;
    }

    /**
     * Получает скользящую среднюю на срезе в виде массива значений.
     */
    private void calculateApproximation(Slice slice) {
        int averageWindow = Math.max(8, (int) Math.sqrt(slice.points.length));
        approximateValues = new long[slice.points.length];
        for (int i = 0; i < slice.points.length; i++) {
            long avg = 0;
            int pointCount = 0;
            for (int j = Math.max(i - averageWindow, 0); j < Math.min(i + averageWindow, slice.points.length); j++) {
                avg += slice.points[j].value * slice.points[j].amount;
                pointCount++;
            }
            if (pointCount > 0) {
                avg /= pointCount;
            }
            approximateValues[i] = avg;
        }
        calculateSigma(slice);
    }

    /**
     * Вычисляет среднеквадратичное отклонение среза на основе полученной скользящей средней.
     */
    private void calculateSigma(Slice slice) {
        double varianceSum = 0;
        for (int i = 0; i < slice.points.length; i++) {
            varianceSum = varianceSum + Math.pow(slice.points[i].value * slice.points[i].amount - getApproximate(slice, i), 2);
        }
        varianceSum = Math.sqrt(varianceSum / slice.points.length);
        sigma = varianceSum;
    }

    @Override
    public double getAngleMultiplier(Slice slice) {
        return (double) (slice.points[slice.points.length - 1].value - slice.points[0].value) / slice.dateRange;
    }

}

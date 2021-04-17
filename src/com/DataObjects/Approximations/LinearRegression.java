package com.DataObjects.Approximations;

import com.DataObjects.Slice;
import com.DataObjects.SlicePoint;

/**
 * Функция приближения среза, вычисляемая методом наименьших квадратов как линейная функция от времени.
 */
public class LinearRegression implements Approximation {

    private double approximationAngle, approximationOffset;
    private double sigma;

    public LinearRegression(Slice slice) {
        calculateApproximation(slice);
    }

    @Override
    public long getApproximate(Slice slice, int pos) {
        return (long)((slice.points[pos].date.getTime() - slice.points[0].date.getTime()) * approximationAngle + approximationOffset);
    }

    @Override
    public double getSigma() {
        return sigma;
    }

    @Override
    public ApproximationType getType() {
        return ApproximationType.LINEAR;
    }

    /**
     * Получает линейную регрессию данных на срезе в виде y = approximationAngle + approximationOffset.
     */
    private void calculateApproximation(Slice slice) {
        long firstTime = slice.points[0].date.getTime();
        double sumX = 0;
        double sumY = 0;
        double sumXsq = 0;
        double sumXY = 0;
        for(SlicePoint point: slice.points) {
            long elapsedTime = point.date.getTime() - firstTime;
            sumX += elapsedTime;
            sumY += point.value * point.amount;
            sumXsq += Math.pow(elapsedTime, 2);
            sumXY += elapsedTime * point.value * point.amount;
        }
        approximationAngle = (sumXY * slice.points.length - sumX * sumY) / (sumXsq * slice.points.length - sumX * sumX);
        approximationOffset = (sumY * sumXsq - sumX * sumXY) / (sumXsq * slice.points.length - sumX * sumX);
        calculateSigma(slice);
    }

    /**
     * Вычисляет среднеквадратичное отклонение среза на основе полученной регрессии.
     */
    private void calculateSigma(Slice slice) {
        double varianceSum = 0;
        for(int i = 0; i < slice.points.length; i++) {
            varianceSum = varianceSum + Math.pow(slice.points[i].value * slice.points[i].amount - getApproximate(slice, i), 2);
        }
        varianceSum = Math.sqrt(varianceSum / slice.points.length);
        sigma = varianceSum;
    }

}

package com.DataObjects.Approximations;

import com.DataObjects.Slice;

/**
 * Функция приближения среза, вычисляемая методом наименьших квадратов как линейная функция от времени.
 */
public class LinearRegression implements Approximation {

    private double approximationAngle, approximationOffset;
    private double sigma;

    public LinearRegression(Slice slice, int start, int end) {
        calculateApproximation(slice, start, end);
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
     *
     * @param slice - срез, на котором находится регрессия
     * @param start - индекс первой точки регрессии
     * @param end - индекс последней точки регрессии
     */
    private void calculateApproximation(Slice slice, int start, int end) {
        long firstTime = slice.points[0].date.getTime();
        double sumX = 0;
        double sumY = 0;
        double sumXsq = 0;
        double sumXY = 0;
        for(int i = Math.max(0, start); i < slice.points.length && i <= end; i++) {
            long elapsedTime = slice.points[i].date.getTime() - firstTime;
            sumX += elapsedTime;
            sumY += slice.points[i].value * slice.points[i].amount;
            sumXsq += Math.pow(elapsedTime, 2);
            sumXY += elapsedTime * slice.points[i].value * slice.points[i].amount;
        }
        approximationAngle = (sumXY * slice.points.length - sumX * sumY) / (sumXsq * slice.points.length - sumX * sumX);
        approximationOffset = (sumY * sumXsq - sumX * sumXY) / (sumXsq * slice.points.length - sumX * sumX);
        calculateSigma(slice, start, end);
    }

    /**
     * Получает среднеквадратичное отклонение среза на основе полученной регрессии.
     *
     * @param slice - срез, на котором находится регрессия
     * @param start - индекс первой точки регрессии
     * @param end - индекс последней точки регрессии
     */
    private void calculateSigma(Slice slice, int start, int end) {
        double varianceSum = 0;
        for(int i = Math.max(0, start); i < slice.points.length && i <= end; i++) {
            varianceSum = varianceSum + Math.pow(slice.points[i].value * slice.points[i].amount - getApproximate(slice, i), 2);
        }
        varianceSum = Math.sqrt(varianceSum / slice.points.length);
        sigma = varianceSum;
    }

    @Override
    public double getAngleMultiplier(Slice slice) {
        return approximationAngle;
    }
}

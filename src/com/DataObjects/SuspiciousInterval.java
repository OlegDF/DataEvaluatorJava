package com.DataObjects;

import com.DataObjects.Approximations.Approximation;
import com.DataObjects.Approximations.LinearRegression;

/**
 * Интервал определенного разреза, на котором значение уменьшается или незначительно изменяется между первой и последней точкой.
 */
public class SuspiciousInterval {

    public final Slice slice;
    public final int pos1, pos2;
    private final Approximation partialApproximation;

    public SuspiciousInterval(Slice slice, int pos1, int pos2) {
        this.slice = slice;
        this.pos1 = pos1;
        this.pos2 = pos2;
        this.partialApproximation = getPartialApproximation();
    }

    /**
     * Вычисляет меру уменьшения значения на интервале, равную квадрату отношения разности крайних значений интервала к
     * ширине интервала и среднеквадратичному отклонению отрезка. Также, если перед интервалом находится не менее 20%
     * среза, а последняя точка интервала имеет значительно меньшее значение, чем функция приближения первой части
     * среза в этой же точке, то мера уменьшения увеличивается.
     *
     * @return меру уменьшения значения
     */
    public double getDecreaseScore() {
        if(pos1 < 0 || pos1 >= slice.points.length || pos2 < 0 || pos2 >= slice.points.length) {
            return -1;
        }
        double res = getRelativeDiff() * getRelativeDiff() / (getRelativeWidth());
        double relativeSigma = slice.getSigma() / slice.valueRange;
        if(relativeSigma != 0) {
            res /= relativeSigma;
        }
        res = getComparisonToApproximation(res);
        return res;
    }

    /**
     * Вычисляет меру значимости интервала без увеличения, равную его ширине. Также, если перед интервалом находится не
     * менее 20% среза, а последняя точка интервала имеет значительно меньшее значение, чем функция приближения первой
     * части среза в этой же точке, то мера уменьшения увеличивается.
     *
     * @return меру значимости интервала
     */
    public double getFlatnessScore() {
        double res = getRelativeWidth();
        res = getComparisonToApproximation(res);
        return res;
    }

    /**
     * Вычисляет коэффициент и умножает на него меру значимости. Коэффициент увеличивается, если последняя точка интервала
     * имеет значительно меньшее значение, чем функция приближения первой части среза в этой же точке, а также если в
     * последней точке среза частичное приближение меньше общего.
     *
     * @param res - мера значимости
     * @return новая мера значимости
     */
    private double getComparisonToApproximation(double res) {
        if(partialApproximation != null) {
            if(partialApproximation.getSigma() != 0) {
                double diffWithExpectation = (slice.points[pos2].value - partialApproximation.getApproximate(slice, pos2));
                if(-diffWithExpectation > partialApproximation.getSigma()) {
                    res *= (Math.abs(diffWithExpectation) / partialApproximation.getSigma());
                }
                double diffAtTheEnd = (slice.getApproximate(slice.points.length - 1) -
                        partialApproximation.getApproximate(slice, slice.points.length - 1));
                if(-diffAtTheEnd > partialApproximation.getSigma()) {
                    res *= (Math.abs(diffAtTheEnd) / partialApproximation.getSigma());
                } else if(diffAtTheEnd > 0) {
                    res /= ((1 + diffAtTheEnd) / partialApproximation.getSigma());
                }
            }
        }
        return res;
    }

    /**
     * Проверяет, пересекается ли этот интервал с другим.
     *
     * @param secondInterval - второй интервал
     * @return true, если интервалы пересекаются, и false, если они не пересекаются или если они взяты из разных разрезов
     */
    public boolean intersects(SuspiciousInterval secondInterval) {
        return slice.equals(secondInterval.slice) &&
                ((pos1 > secondInterval.pos1 && pos1 < secondInterval.pos2) || (pos2 > secondInterval.pos1 && pos2 < secondInterval.pos2) ||
                        (secondInterval.pos1 > pos1 && secondInterval.pos1 < pos2) || (secondInterval.pos2 > pos1 && secondInterval.pos2 < pos2) ||
                        (secondInterval.pos1 == pos1 && secondInterval.pos2 == pos2));
    }

    /**
     * Вычисляет отношение между количеством времени в интервале и длинной временного промежутка оригинального разреза.
     *
     * @return отношение длин отрезков времени (ожидаемые значения - между 0 и 1)
     */
    public double getRelativeWidth() {
        return (double) slice.getDateDistance(pos1, pos2) / slice.dateRange;
    }

    /**
     * Вычисляет отношение между разностью последнего и первого значения на интервале и разностью максимума и минимума
     * на всем отрезке.
     *
     * @return отношение разностей значений (ожидаемые значения - между -1 и 1)
     */
    public double getRelativeDiff() {
        return (double)(slice.points[pos2].value - slice.points[pos1].value) / (slice.valueRange);
    }

    /**
     * Вычисляет отношение между разностью максимального и минимального значения на интервале и разностью максимума и минимума
     * на всем отрезке.
     *
     * @return отношение разностей значений (ожидаемые значения - между -1 и 1)
     */
    public double getRelativeValueRange() {
        return (double)(slice.getLocalValueRange(pos1, pos2)) / (slice.valueRange);
    }
    /**
     * Получает значение частичной функции приближения в определенной точке во времени.
     *
     * @param pos - номер точки среза
     * @return значение приближения
     */
    public long getPartialApproximate(int pos) {
        return partialApproximation != null ? partialApproximation.getApproximate(slice, pos) : 0;
    }

    /**
     * Получает среднеквадратичное отклонение относительно частичной функции приближения.
     *
     * @return среднеквадратичное отклонение
     */
    public double getPartialSigma() {
        return partialApproximation != null ? partialApproximation.getSigma() : 0;
    }

    public boolean hasPartialApproximation() {
        return partialApproximation != null;
    }

    /**
     * Получает функцию приближения относительно начальной части среза, от первой точки среза до первой точки данного
     * интервала Получает частичное приближение, только если рассматриваемая начальная часть среза имеет достаточную длину.
     *
     * @return частичная функция приближения
     */
    private Approximation getPartialApproximation() {
        if(slice.getDateDistance(0, pos1) >= slice.dateRange * 0.2) {
            return new LinearRegression(slice, 0, pos1);
        } else {
            return null;
        }
    }

}

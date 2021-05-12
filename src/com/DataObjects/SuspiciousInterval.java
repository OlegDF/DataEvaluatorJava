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

    public SuspiciousInterval(Slice slice, int pos1, int pos2, double minStartDate) {
        this.slice = slice;
        this.pos1 = pos1;
        this.pos2 = pos2;
        this.partialApproximation = getPartialApproximation(minStartDate);
    }

    public SuspiciousInterval(SuspiciousInterval originalInterval, int newPos2) {
        this.slice = originalInterval.slice;
        this.pos1 = originalInterval.pos1;
        this.pos2 = newPos2;
        this.partialApproximation = originalInterval.partialApproximation;
    }

    /**
     * Вычисляет меру уменьшения значения на интервале, равную квадрату отношения разности крайних значений интервала к
     * ширине интервала и среднеквадратичному отклонению отрезка. Также, если перед интервалом находится не менее 5%
     * среза, а последняя точка интервала имеет значительно меньшее значение, чем функция приближения первой части
     * среза в этой же точке, то мера уменьшения увеличивается.
     *
     * @return меру уменьшения значения
     */
    public double getDecreaseScore() {
        if (pos1 < 0 || pos1 >= slice.points.length || pos2 < 0 || pos2 >= slice.points.length) {
            return -1;
        }
        double res = Math.sqrt(slice.dateRange) * Math.pow(getRelativeDiff(), 2);
        if (getRelativeWidth() != 0) {
            res /= Math.pow(getRelativeWidth(), 2);
        }
        double relativeSigma = slice.getSigma() / slice.valueRange;
        if (relativeSigma != 0) {
            res /= relativeSigma;
        }
        res = getComparisonToApproximation(res);
        res = compareApproximations(res);
        return res;
    }

    /**
     * Вычисляет меру значимости интервала без увеличения, равную его ширине. Также, если перед интервалом находится не
     * менее 5% среза, а последняя точка интервала имеет значительно меньшее значение, чем функция приближения первой
     * части среза в этой же точке, то мера уменьшения увеличивается.
     *
     * @return меру значимости интервала
     */
    public double getFlatnessScore() {
        double res = getRelativeWidth();
        res = getComparisonToApproximation(res);
        res = compareApproximations(res);
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
        if (partialApproximation != null) {
            if (partialApproximation.getSigma() != 0) {
                double diffWithExpectation = (slice.points[pos2].value - partialApproximation.getApproximate(slice, pos2));
                if (-diffWithExpectation >= partialApproximation.getSigma()) {
                    res *= (Math.abs(diffWithExpectation) / partialApproximation.getSigma());
                } else {
                    res /= ((diffWithExpectation / partialApproximation.getSigma()) + 1);
                    res /= 10;
                }
                double diffAtTheEnd = (slice.getApproximate(slice.points.length - 1) -
                        partialApproximation.getApproximate(slice, slice.points.length - 1));
                if (-diffAtTheEnd >= partialApproximation.getSigma()) {
                    res *= (Math.abs(diffAtTheEnd) / partialApproximation.getSigma());
                } else {
                    res /= ((diffAtTheEnd / partialApproximation.getSigma()) + 1);
                    res /= 10;
                }
            }
        }
        return res;
    }

    /**
     * Вычисляет коэффициент и умножает на него меру значимости. Коэффициент увеличивается, если частичное приближение
     * имеет значительный наклон вверх или полное приближение имеет значительный наклон вниз, в противном случае он уменьшается.
     *
     * @param res - мера значимости
     * @return новая мера значимости
     */
    private double compareApproximations(double res) {
        if (partialApproximation != null) {
            double approximationAngle = slice.getApproximationAngle();
            double partialApproximationAngle = partialApproximation.getAngleMultiplier(slice);
            if (approximationAngle > 0) {
                res /= (approximationAngle * 100 + 1);
            } else if (approximationAngle < 0) {
                res *= (-approximationAngle * 100 + 1) * (-approximationAngle * 100 + 1);
            }
            if (partialApproximationAngle > 0) {
                res *= (partialApproximationAngle * 100 + 1) * (partialApproximationAngle * 100 + 1);
            } else if (partialApproximationAngle < 0) {
                res /= (-partialApproximationAngle * 100 + 1);
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
        return (double) (slice.points[pos2].value - slice.points[pos1].value - slice.getApproximate(pos2) + slice.getApproximate(pos1)) / (slice.valueRange);
    }

    /**
     * Вычисляет отношение между разностью максимального и минимального значения на интервале и разностью максимума и минимума
     * на всем отрезке.
     *
     * @return отношение разностей значений (ожидаемые значения - между -1 и 1)
     */
    public double getRelativeValueRange() {
        return (double) (slice.getLocalValueRange(pos1, pos2)) / (slice.valueRange);
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

    public SlicePoint getFirstPoint() {
        return slice.points[pos1];
    }

    public SlicePoint getLastPoint() {
        return slice.points[pos2];
    }

    /**
     * Получает функцию приближения относительно начальной части среза, от первой точки среза до первой точки данного
     * интервала Получает частичное приближение, только если рассматриваемая начальная часть среза имеет достаточную длину.
     *
     * @param minStartDate - множитель минимальной длины начальной части среза (от 0 до 1)
     * @return частичная функция приближения
     */
    private Approximation getPartialApproximation(double minStartDate) {
        if (slice.getDateDistance(0, pos1) >= slice.dateRange * minStartDate) {
            return new LinearRegression(slice, 0, pos1);
        } else {
            return null;
        }
    }

}

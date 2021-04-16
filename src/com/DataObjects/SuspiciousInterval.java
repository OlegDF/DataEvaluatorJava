package com.DataObjects;

/**
 * Интервал определенного разреза, на котором значение уменьшается или незначительно изменяется между первой и последней точкой.
 */
public class SuspiciousInterval {

    public final Slice slice;
    public final int pos1, pos2;

    public SuspiciousInterval(Slice slice, int pos1, int pos2) {
        this.slice = slice;
        this.pos1 = pos1;
        this.pos2 = pos2;
    }

    /**
     * Вычисляет меру уменьшения значения на интервале, равную квадрату отношения разности крайних значений интервала к
     * разности между максимальным и минимальным значениями на отрезке.
     *
     * @return меру уменьшения значения
     */
    public double getDecreaseScore() {
        if(pos1 < 0 || pos1 >= slice.points.length || pos2 < 0 || pos2 >= slice.points.length) {
            return -1;
        }
        double relativeSigma = slice.getSigma() / slice.valueRange;
        if(relativeSigma != 0) {
            return getRelativeDiff() * getRelativeDiff() / (getRelativeWidth()) / (slice.getSigma() / slice.valueRange);
        } else {
            return getRelativeDiff() * getRelativeDiff() / (getRelativeWidth());
        }
    }

    /**
     * Проверяет, пересекается ли этот интервал с другим.
     *
     * @param secondInterval - второй интервал
     * @return true, если интервалы пересекаются, и false, если они не пересекаются или если они взяты из разных разрезов
     */
    public boolean intersects(SuspiciousInterval secondInterval) {
        return slice == secondInterval.slice &&
                ((pos1 > secondInterval.pos1 && pos1 < secondInterval.pos2) || (pos2 > secondInterval.pos1 && pos2 < secondInterval.pos2) ||
                        (secondInterval.pos1 > pos1 && secondInterval.pos1 < pos2) || (secondInterval.pos2 > pos1 && secondInterval.pos2 < pos2) ||
                        (secondInterval.pos1 == pos1 && secondInterval.pos1 == pos2));
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

}

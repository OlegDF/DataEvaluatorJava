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
        return (double)(slice.points[pos2].value - slice.points[pos1].value) *
                (slice.points[pos2].value - slice.points[pos1].value) /
                (slice.valueRange * slice.valueRange);
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
                        (secondInterval.pos1 > pos1 && secondInterval.pos1 < pos2) || (secondInterval.pos2 > pos1 && secondInterval.pos2 < pos2));
    }

}

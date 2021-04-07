package com.DataObjects;

/**
 * Объект, содержащий в себе данные о разрезе - список точек с датами и соответствующими значениями, а также названия и
 * значения столбцов, по которым сделан разрез.
 */
public class Slice {

    public final String tableName;
    public final String[] colNames;
    public final String[] labels;
    public final SlicePoint[] points;
    public final long valueRange, totalAmount;

    /**
     * Конструктор пустого разреза, для которого не найдено подходящих точек.
     *
     * @param colNames - названия столбцов, по которым создается разрез
     * @param labels - значения соответствующих столбцов
     */
    public Slice(String tableName, String[] colNames, String[] labels) {
        this.tableName = tableName;
        this.colNames = colNames;
        this.labels = labels;
        this.points = new SlicePoint[0];
        this.valueRange = getValueRange();
        this.totalAmount = getTotalAmount();
    }

    /**
     * Конструктор разреза, содержащего набор точек.
     *
     * @param colNames - названия столбцов, по которым создается разрез
     * @param labels - значения соответствующих столбцов
     * @param points - список точек разреза
     */
    public Slice(String tableName, String[] colNames, String[] labels, SlicePoint[] points) {
        this.tableName = tableName;
        this.colNames = colNames;
        this.labels = labels;
        this.points = points;
        this.valueRange = getValueRange();
        this.totalAmount = getTotalAmount();
    }

    /**
     * Генерирует версию данного разреза с накоплением, т. е. значение i-й точки в новом разрезе равно сумме значений
     * точек с 0 по i в текущем разрезе.
     * @return новый разрез с накоплением
     */
    public Slice getAccumulation() {
        SlicePoint[] pointsAccumulated = new SlicePoint[points.length];
        long accumulatedValue = 0;
        for(int i = 0; i < points.length; i++) {
            accumulatedValue += points[i].value * points[i].amount;
            pointsAccumulated[i] = new SlicePoint(accumulatedValue, 1, points[i].date);
        }
        return new Slice(tableName, colNames, labels, pointsAccumulated);
    }

    /**
     * Проверяет, верно ли, что между двумя точками значение разреза уменьшается не менее чем на определенную величину
     *
     * @param pos1 - номер первой точки
     * @param pos2 - номер второй точки
     * @param threshold - необходимое уменьшение значения
     * @return true, если уменьшение достаточно велико, иначе false
     */
    public boolean isIntervalDecreasing(int pos1, int pos2, long threshold) {
        return points[pos2].value * points[pos2].amount - points[pos1].value * points[pos1].amount < -threshold;
    }

    /**
     * Получает общее количество операций в разрезе.
     *
     * @return значение количества операций
     */
    private long getTotalAmount() {
        long res = 0;
        for(SlicePoint point: points) {
            res += point.amount;
        }
        return res;
    }

    /**
     * Получает разность между максимальным и минимальным значениями на отрезке.
     *
     * @return значение разности
     */
    private long getValueRange() {
        if(points.length == 0) {
            return 0;
        }
        long min = points[0].value * points[0].amount;
        long max = points[0].value * points[0].amount;
        for(SlicePoint point: points) {
            if(point.value < min) {
                min = point.value * point.amount;
            }
            if(point.value > max) {
                max = point.value * point.amount;
            }
        }
        return max - min;
    }

}

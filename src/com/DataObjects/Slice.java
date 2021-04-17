package com.DataObjects;

import com.DataObjects.Approximations.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Объект, содержащий в себе данные о разрезе - список точек с датами и соответствующими значениями, а также названия и
 * значения столбцов, по которым сделан разрез.
 */
public class Slice {

    public final String tableName;
    public final String[] colNames;
    public final String[] labels;
    public final SlicePoint[] points;
    public final long valueRange, dateRange, totalAmount;
    private final Approximation approximation;

    /**
     * Конструктор пустого разреза, для которого не найдено подходящих точек.
     *
     * @param tableName - таблица, из которой получен разрез
     * @param colNames - названия столбцов, по которым создается разрез
     * @param labels - значения соответствующих столбцов
     */
    public Slice(String tableName, String[] colNames, String[] labels) {
        this.tableName = tableName;
        this.colNames = colNames;
        this.labels = labels;
        this.points = new SlicePoint[0];
        this.valueRange = 0;
        this.dateRange = 0;
        this.totalAmount = 0;
        this.approximation = new EmptyApproximation();
    }

    /**
     * Конструктор разреза, содержащего набор точек.
     *
     * @param tableName - таблица, из которой получен разрез
     * @param colNames - названия столбцов, по которым создается разрез
     * @param labels - значения соответствующих столбцов
     * @param points - список точек разреза
     * @param approximationType - тип функции приближения
     */
    public Slice(String tableName, String[] colNames, String[] labels, SlicePoint[] points, ApproximationType approximationType) {
        this.tableName = tableName;
        this.colNames = colNames;
        this.labels = labels;
        this.points = points;
        if(points.length > 0) {
            this.valueRange = getValueRange();
            this.dateRange = getDateRange();
            this.totalAmount = getTotalAmount();
        } else {
            this.valueRange = 0;
            this.dateRange = 0;
            this.totalAmount = 0;
        }
        switch(approximationType) {
            case EMPTY:
                this.approximation = new EmptyApproximation();
                break;
            case LINEAR:
                this.approximation = new LinearRegression(this);
                break;
            case AVERAGES:
                this.approximation = new AveragesApproximation(this);
                break;
            default:
                this.approximation = new EmptyApproximation();
        }
    }

    /**
     * Генерирует версию данного разреза с накоплением, т. е. значение i-й точки в новом разрезе равно сумме значений
     * точек с 0 по i в текущем разрезе.
     * @return новый разрез с накоплением
     */
    public Slice getAccumulation() {
        List<SlicePoint> pointsTruncated = new ArrayList<>();
        pointsTruncated.add(new SlicePoint(points[0].value * points[0].amount, 1, points[0].date));
        for(int i = 1; i < points.length; i++) {
            SlicePoint newPoint = new SlicePoint(pointsTruncated.get(pointsTruncated.size() - 1).value + points[i].value * points[i].amount,
                    1, points[i].date);
            if(points[i].date.getTime() == pointsTruncated.get(pointsTruncated.size() - 1).date.getTime()) {
                pointsTruncated.remove(pointsTruncated.size() - 1);
            }
            pointsTruncated.add(newPoint);
        }
        return new Slice(tableName, colNames, labels, pointsTruncated.toArray(new SlicePoint[0]), approximation.getType());
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
     * Проверяет, верно ли, что между двумя точками значение разреза колеблется не менее чем на определенную величину
     *
     * @param pos1 - номер первой точки
     * @param pos2 - номер второй точки
     * @param threshold - максимальное изменение значения
     * @return true, если изменение достаточно мало, иначе false
     */
    public boolean isIntervalConstant(int pos1, int pos2, long threshold) {
        return getLocalValueRange(pos1, pos2) < threshold;
    }

    /**
     * Получает разность между максимальным и минимальным значениями на фрагменте разреза.
     *
     * @param pos1 - номер первой точки
     * @param pos2 - номер второй точки
     * @return значение разности
     */
    public long getLocalValueRange(int pos1, int pos2) {
        long min = points[pos1].value * points[pos1].amount;
        long max = points[pos1].value * points[pos1].amount;
        for(int i = pos1; i <= pos2; i++) {
            if(points[i].value * points[i].amount < min) {
                min = points[i].value * points[i].amount;
            }
            if(points[i].value > max) {
                max = points[i].value * points[i].amount;
            }
        }
        return Math.abs(max - min);
    }

    /**
     * Получает расстояние во времени между двумя точками разреза.
     *
     * @param pos1 - номер первой точки
     * @param pos2 - номер второй точки
     * @return количество единиц времени между двумя точками
     */
    public long getDateDistance(int pos1, int pos2) {
        return points[pos2].date.getTime() - points[pos1].date.getTime();
    }

    /**
     * Получает среднеквадратичное отклонение относительно функции приближения.
     *
     * @return среднеквадратичное отклонение
     */
    public double getSigma() {
        return approximation.getSigma();
    }

    /**
     * Получает среднеквадратичное отклонение относительно функции приближения, деленное на разность максимума и минимума
     * на всем отрезке.
     *
     * @return относительное среднеквадратичное отклонение
     */
    public double getRelativeSigma() {
        double res = approximation.getSigma() / getValueRange();
        return res == 0 ? 1 : res;
    }

    /**
     * Получает значение функции линейной регрессии в определенной точке во времени.
     *
     * @param pos - номер точки среза
     * @return значение регрессии
     */
    public long getApproximate(int pos) {
        return approximation.getApproximate(this, pos);
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
     * Получает разность между максимальным и минимальным значениями на разрезе.
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
            if(point.value * point.amount < min) {
                min = point.value * point.amount;
            }
            if(point.value * point.amount > max) {
                max = point.value * point.amount;
            }
        }
        return max - min;
    }

    /**
     * Получает расстояние во времени между первой и последней точками разреза.
     *
     * @return количество единиц времени между первой и последней точками
     */
    private long getDateRange() {
        return points[points.length - 1].date.getTime() - points[0].date.getTime();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Slice slice = (Slice) o;
        return Arrays.equals(points, slice.points);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(points);
    }

}

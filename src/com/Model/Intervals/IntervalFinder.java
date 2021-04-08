package com.Model.Intervals;

import com.DataObjects.Slice;
import com.DataObjects.SuspiciousInterval;

import java.util.List;

/**
 * Интерфейс для классов, которые получают интервалы с уменьшением или недостаточным увеличением значения.
 */
public interface IntervalFinder {

    /**
     * Получает из списка разрезов данных список интервалов, на которых наблюдается убывание значение, с минимальными
     * порогами длины интервала и меры уменьшения значения. Также сортирует интервалы по величине уменьшения.
     *
     * @param slices - список разрезов, на которых ведется поиск
     * @return список интервалов
     */
    public List<SuspiciousInterval> getDecreasingIntervals(List<Slice> slices, double minIntervalMult, double thresholdMult);

}

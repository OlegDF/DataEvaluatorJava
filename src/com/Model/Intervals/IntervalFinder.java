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
     * @param minIntervalMult - минимальная длина интервалов, которые будут рассматриваться (измеряется как доля длины
     *                        временного промежутка всего разреза, от 0 до 1)
     * @param thresholdMult - минимальная разность между первой и последней величиной для интервалов, которые будут
     *                        рассматриваться (измеряется как доля разности между максимальным и минимальным значением
     *                        на всем разрезе, от 0 до 1)
     * @param maxIntervals - максимальное количество интервалов, которые вернет метод
     * @param removeIntersections - если true, то из списка будут убраны интервалы, которые пересекаются с другими
     * @return список интервалов
     */
    List<SuspiciousInterval> getDecreasingIntervals(List<Slice> slices, double minIntervalMult, double thresholdMult,
                                                           int maxIntervals, boolean removeIntersections);

    /**
     * Убирает из отсортированного списка интервалы, которые пересекаются друг с другом (в списке остаются интеравлы
     * с большим уменьшением).
     *
     * @param intervals - список интервалов
     */
    void removeIntersectingIntervals(List<SuspiciousInterval> intervals);

}

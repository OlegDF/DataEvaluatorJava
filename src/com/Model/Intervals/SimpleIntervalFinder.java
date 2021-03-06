package com.Model.Intervals;

import com.DataObjects.Slice;
import com.DataObjects.SuspiciousInterval;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Искатель интервалов, в котором пороги длины интервала определяются как доля от длины разреза, а пороги уменьшения
 * значения - как доля от разницы максимума и минимума на разрезе.
 */
public class SimpleIntervalFinder implements IntervalFinder {

    public SimpleIntervalFinder() {
    }

    /**
     * Получает из списка разрезов данных список интервалов, на которых наблюдается убывание значение, с минимальными
     * порогами длины интервала и меры уменьшения значения. Также сортирует интервалы по величине уменьшения.
     *
     * @param slices              - список разрезов, на которых ведется поиск
     * @param minIntervalMult     - минимальная длина интервалов, которые будут рассматриваться (измеряется как доля длины
     *                            временного промежутка всего разреза, от 0 до 1)
     * @param thresholdMult       - минимальная разность между первой и последней величиной для интервалов, которые будут
     *                            рассматриваться (измеряется как доля среднеквадратического отклонения)
     * @param maxIntervals        - максимальное количество интервалов, которые вернет метод
     * @param removeIntersections - если true, то из списка будут убраны интервалы, которые пересекаются с другими
     * @return список интервалов
     */
    @Override
    public List<SuspiciousInterval> getDecreasingIntervals(List<Slice> slices, double minIntervalMult, double thresholdMult,
                                                           int maxIntervals, boolean removeIntersections) {
        List<SuspiciousInterval> res = new ArrayList<>();
        for (Slice slice : slices) {
            final int chunkLength = Integer.max(slice.points.length / 128, 1);
            final int minIntervalLength = Integer.max((int) Math.floor(slice.dateRange * minIntervalMult), 1);
            final long threshold = (long) Math.floor(slice.getSigma() * thresholdMult);
            for (int pos1 = 0; pos1 < slice.points.length - 1; pos1 += chunkLength) {
                SuspiciousInterval newInterval = null;
                for (int pos2 = pos1 + chunkLength; pos2 < slice.points.length; pos2 += chunkLength) {
                    if (slice.isIntervalDecreasing(pos1, pos2, threshold) && slice.getDateDistance(pos1, pos2) >= minIntervalLength) {
                        if (newInterval == null) {
                            newInterval = new SuspiciousInterval(slice, pos1, pos2, 0.2);
                        } else {
                            SuspiciousInterval secondInterval = new SuspiciousInterval(newInterval, pos2);
                            if (secondInterval.getDecreaseScore() > newInterval.getDecreaseScore()) {
                                newInterval = secondInterval;
                            }
                        }
                    }
                }
                if (newInterval != null) {
                    res.add(newInterval);
                }
            }
        }
        res.sort(Comparator.comparing(SuspiciousInterval::getDecreaseScore).reversed());
        if (removeIntersections) {
            removeIntersectingIntervals(res);
        }
        return res.size() >= maxIntervals ? res.subList(0, maxIntervals) : res;
    }

    /**
     * Получает из списка разрезов данных список интервалов, на которых не наблюдается значительного изменения значения, с минимальным
     * порогом длины интервала и максимальным изменением значения. Также сортирует интервалы по длине.
     *
     * @param slices              - список разрезов, на которых ведется поиск
     * @param minIntervalMult     - минимальная длина интервалов, которые будут рассматриваться (измеряется как доля длины
     *                            временного промежутка всего разреза, от 0 до 1)
     * @param thresholdMult       - максимальная разность между максимальной и минимальной величиной для интервалов, которые будут
     *                            рассматриваться (измеряется как доля среднеквадратического отклонения)
     * @param maxIntervals        - максимальное количество интервалов, которые вернет метод
     * @param removeIntersections - если true, то из списка будут убраны интервалы, которые пересекаются с другими
     * @return список интервалов
     */
    @Override
    public List<SuspiciousInterval> getConstantIntervals(List<Slice> slices, double minIntervalMult, double thresholdMult,
                                                         int maxIntervals, boolean removeIntersections) {
        List<SuspiciousInterval> res = new ArrayList<>();
        for (Slice slice : slices) {
            final int chunkLength = Integer.max(slice.points.length / 128, 1);
            final int minIntervalLength = Integer.max((int) Math.floor(slice.dateRange * minIntervalMult), 1);
            final long threshold = (long) Math.floor(slice.getSigma() * thresholdMult);
            for (int pos1 = 0; pos1 < slice.points.length - 1; pos1 += chunkLength) {
                SuspiciousInterval newInterval = null;
                for (int pos2 = pos1 + chunkLength; pos2 < slice.points.length; pos2 += chunkLength) {
                    if (slice.isIntervalConstant(pos1, pos2, threshold) && slice.getDateDistance(pos1, pos2) >= minIntervalLength) {
                        if (newInterval == null) {
                            newInterval = new SuspiciousInterval(slice, pos1, pos2, 0.2);
                        } else {
                            SuspiciousInterval secondInterval = new SuspiciousInterval(newInterval, pos2);
                            if (secondInterval.getFlatnessScore() > newInterval.getFlatnessScore()) {
                                newInterval = secondInterval;
                            }
                        }
                    }
                }
                if (newInterval != null) {
                    res.add(newInterval);
                }
            }
        }
        res.sort(Comparator.comparing(SuspiciousInterval::getFlatnessScore).reversed());
        if (removeIntersections) {
            removeIntersectingIntervals(res);
        }
        return res.size() >= maxIntervals ? res.subList(0, maxIntervals) : res;
    }

    /**
     * Убирает из отсортированного списка интервалы, которые пересекаются друг с другом (в списке остаются интеравлы
     * с большим уменьшением).
     *
     * @param intervals - список интервалов
     */
    @Override
    public void removeIntersectingIntervals(List<SuspiciousInterval> intervals) {
        for (int i = 0; i < intervals.size() - 1; i++) {
            for (int j = intervals.size() - 1; j > i; j--) {
                if (intervals.get(i).intersects(intervals.get(j))) {
                    intervals.remove(j);
                }
            }
        }
    }

}

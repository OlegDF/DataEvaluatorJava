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

    public SimpleIntervalFinder() {}

    /**
     * Получает из списка разрезов данных список интервалов, на которых наблюдается убывание значение, с минимальными
     * порогами длины интервала и меры уменьшения значения. Также сортирует интервалы по величине уменьшения.
     *
     * @param slices - список разрезов, на которых ведется поиск
     * @return список интервалов
     */
    @Override
    public List<SuspiciousInterval> getDecreasingIntervals(List<Slice> slices) {
        List<SuspiciousInterval> res = new ArrayList<>();
        for(Slice slice: slices) {
            final int chunkLength = Integer.max(slice.points.length / 1024, 1);
            final int minIntervalLength = Integer.max(slice.points.length / 16, 1);
            final int maxIntervalLength = Integer.max(slice.points.length / 4, 1);
            final long threshold = slice.valueRange / 16;
            for(int pos1 = 0; pos1 < slice.points.length - 1; pos1 += chunkLength) {
                for(int pos2 = pos1 + minIntervalLength; pos2 < slice.points.length && pos2 <= pos1 + maxIntervalLength; pos2 += chunkLength) {
                    if(slice.isIntervalDecreasing(pos1, pos2, threshold)) {
                        res.add(new SuspiciousInterval(slice, pos1, pos2));
                    }
                }
            }
        }
        res.sort(Comparator.comparing(SuspiciousInterval::getDecreaseScore).reversed());
        removeIntersectingIntervals(res);
        return res.size() >= 32 ? res.subList(0, 32) : res;
    }

    /**
     * Убирает из отсортированного списка интервалы, которые пересекаются друг с другом (в списке остаются интеравлы
     * с большим уменьшением).
     *
     * @param intervals - список интервалов
     */
    private void removeIntersectingIntervals(List<SuspiciousInterval> intervals) {
        for(int i = 0; i < intervals.size() - 1; i++) {
            for(int j = i + 1; j < intervals.size(); j++) {
                if(intervals.get(i).intersects(intervals.get(j))) {
                    intervals.remove(j);
                    j--;
                }
            }
        }
    }

}
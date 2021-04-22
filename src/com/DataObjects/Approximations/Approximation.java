package com.DataObjects.Approximations;

import com.DataObjects.Slice;

/**
 * Функция приближения среза, также включающая в себя дисперсию.
 */
public interface Approximation {

    /**
     * Получает значение функции приближения в определенной точке во времени.
     *
     * @param pos - номер точки среза
     * @return значение регрессии
     */
    long getApproximate(Slice slice, int pos);

    /**
     * Получает среднеквадратичное отклонение относительно среза.
     *
     * @return среднеквадратичное отклонение
     */
    double getSigma();

    /**
     * Получает тип функции приближения.
     *
     * @return тип приближения в виде enum
     */
    ApproximationType getType();

    /**
     * Получает множитель наклона функции (чем он выше, тем больше наклон, знак означает направление наклона)
     *
     * @return множитель наклона
     */
    double getAngleMultiplier(Slice slice);

}

package com.DataObjects;

import java.util.Date;
import java.util.Objects;

/**
 * Точка разреза, состоящая из значения, количества предметов и даты/времени, в которое значение зафиксировано
 */
public class SlicePoint {

    public final long value;
    public final long amount;
    public final Date date;

    public SlicePoint(long value, long amount, Date date) {
        this.value = value;
        this.amount = amount;
        this.date = date;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SlicePoint point = (SlicePoint) o;
        return value == point.value &&
                amount == point.amount &&
                date.getTime() == point.date.getTime();
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, amount, date.getTime());
    }

}

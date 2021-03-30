package com.DataObjects;

import java.util.Date;

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
}

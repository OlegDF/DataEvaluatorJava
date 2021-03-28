package com.DataObjects;

import java.math.BigDecimal;

public class SuspiciousInterval {

    public final Slice slice;
    public final int moment1, moment2;

    public SuspiciousInterval(Slice slice, int moment1, int moment2) {
        this.slice = slice;
        this.moment1 = moment1;
        this.moment2 = moment2;
    }

    public double getDecreaseScore() {
        return slice.getDecreaseScore(moment1, moment2);
    }

}

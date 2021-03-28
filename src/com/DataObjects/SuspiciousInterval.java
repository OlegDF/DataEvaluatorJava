package com.DataObjects;

public class SuspiciousInterval {

    public final Slice slice;
    public final int pos1, pos2;

    public SuspiciousInterval(Slice slice, int pos1, int pos2) {
        this.slice = slice;
        this.pos1 = pos1;
        this.pos2 = pos2;
    }

    public double getDecreaseScore() {
        if(pos1 < 0 || pos1 >= slice.points.length || pos2 < 0 || pos2 >= slice.points.length) {
            return -1;
        }
        return (double)(slice.points[pos2].value - slice.points[pos1].value) *
                (slice.points[pos2].value - slice.points[pos1].value) /
                (slice.valueRange * slice.valueRange);
    }

    public boolean intersects(SuspiciousInterval secondInterval) {
        return slice == secondInterval.slice &&
                ((pos1 > secondInterval.pos1 && pos1 < secondInterval.pos2) || (pos2 > secondInterval.pos1 && pos2 < secondInterval.pos2) ||
                        (secondInterval.pos1 > pos1 && secondInterval.pos1 < pos2) || (secondInterval.pos2 > pos1 && secondInterval.pos2 < pos2));
    }

}

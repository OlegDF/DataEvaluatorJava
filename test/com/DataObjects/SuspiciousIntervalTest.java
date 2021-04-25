package com.DataObjects;

import com.DataObjects.Approximations.ApproximationType;
import com.SupportClasses.ConsoleLogger;
import com.SupportClasses.Logger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SuspiciousIntervalTest {

    private Slice upwardSlice;
    private SuspiciousInterval interval1, interval2, interval3;
    private Logger logger;

    @BeforeAll
    void setUp() {
        logger = new ConsoleLogger();
        logger.logMessage("Начинается тест объекта интервала...");
        final String tableName = "data_test";
        final String[] colNames = {"category_1", "category_2"};
        final String[] labels = {"type_1", "source_1"};
        final SlicePoint[] pointsAccumulated = new SlicePoint[5];
        pointsAccumulated[0] = new SlicePoint(500, 1, new Date(10000));
        pointsAccumulated[1] = new SlicePoint(950, 1, new Date(10010));
        pointsAccumulated[2] = new SlicePoint(150, 1, new Date(10030));
        pointsAccumulated[3] = new SlicePoint(1150, 1, new Date(10040));
        pointsAccumulated[4] = new SlicePoint(1650, 1, new Date(10050));
        upwardSlice = new Slice(tableName, colNames, labels, pointsAccumulated, ApproximationType.LINEAR);
        interval1 = new SuspiciousInterval(upwardSlice, 1, 2, 0.05);
        interval2 = new SuspiciousInterval(upwardSlice, 0, 2, 0.05);
        interval3 = new SuspiciousInterval(upwardSlice, 2, 4, 0.05);
    }

    @AfterAll
    void tearDown() {
        logger.logMessage("Закончился тест объекта интервала.");
    }

    @Test
    void getDecreaseScore() {
        assertTrue(interval1.getDecreaseScore() > interval2.getDecreaseScore());
        assertTrue(interval2.getDecreaseScore() > interval3.getDecreaseScore());
    }

    @Test
    void getFlatnessScore() {
        assertTrue(interval1.getFlatnessScore() > interval2.getFlatnessScore());
        assertTrue(interval2.getFlatnessScore() > interval3.getFlatnessScore());
    }

    @Test
    void intersects() {
        SuspiciousInterval interval4 = new SuspiciousInterval(upwardSlice, 3, 4, 0.05);
        assertTrue(interval2.intersects(interval1));
        assertFalse(interval2.intersects(interval3));
        assertFalse(interval2.intersects(interval4));
    }

    @Test
    void getRelativeWidth() {
        assertEquals(0.4, interval1.getRelativeWidth(), 0.001);
        assertEquals(0.6, interval2.getRelativeWidth(), 0.001);
        assertEquals(0.4, interval3.getRelativeWidth(), 0.001);
    }

    @Test
    void getRelativeDiff() {
        assertEquals(-0.53333, interval1.getRelativeDiff(), 0.001);
        assertEquals(-0.23333, interval2.getRelativeDiff(), 0.001);
        assertEquals(1, interval3.getRelativeDiff(), 0.001);
    }

    @Test
    void getRelativeValueRange() {
        assertEquals(0.53333, interval1.getRelativeValueRange(), 0.001);
        assertEquals(0.53333, interval2.getRelativeValueRange(), 0.001);
        assertEquals(1, interval3.getRelativeValueRange(), 0.001);
    }

}
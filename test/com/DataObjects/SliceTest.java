package com.DataObjects;

import com.DataObjects.Approximations.ApproximationType;
import com.SupportClasses.ConsoleLogger;
import com.SupportClasses.Logger;
import org.junit.jupiter.api.*;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SliceTest {

    private Slice upwardSlice;
    private Slice upwardSliceAccumulated;
    private Logger logger;

    @BeforeAll
    void setUp() {
        logger = new ConsoleLogger();
        logger.logMessage("Начинается тест объекта среза...");
        final String tableName = "data_test";
        final String valueName = "value_1";
        final String[] colNames = {"category_1", "category_2"};
        final String[] labels = {"type_1", "source_1"};
        final SlicePoint[] points = new SlicePoint[5];
        points[0] = new SlicePoint(100, 5, new Date(10000));
        points[1] = new SlicePoint(150, 3, new Date(10010));
        points[2] = new SlicePoint(-200, 4, new Date(10030));
        points[3] = new SlicePoint(50, 20, new Date(10040));
        points[4] = new SlicePoint(250, 2, new Date(10050));
        upwardSlice = new Slice(tableName, valueName, colNames, labels, points, ApproximationType.LINEAR);
        final SlicePoint[] pointsAccumulated = new SlicePoint[5];
        pointsAccumulated[0] = new SlicePoint(500, 1, new Date(10000));
        pointsAccumulated[1] = new SlicePoint(950, 1, new Date(10010));
        pointsAccumulated[2] = new SlicePoint(150, 1, new Date(10030));
        pointsAccumulated[3] = new SlicePoint(1150, 1, new Date(10040));
        pointsAccumulated[4] = new SlicePoint(1650, 1, new Date(10050));
        upwardSliceAccumulated = new Slice(tableName, valueName, colNames, labels, pointsAccumulated, ApproximationType.LINEAR);
    }

    @AfterAll
    void tearDown() {
        logger.logMessage("Закончился тест объекта среза.");
    }

    @Test
    void getAccumulation() {
        Slice upwardSliceAccumulatedTest = upwardSlice.getAccumulation();
        assertEquals(upwardSliceAccumulated.points.length, upwardSliceAccumulatedTest.points.length);
        for(int i = 0; i < upwardSliceAccumulatedTest.points.length; i++) {
            assertEquals(upwardSliceAccumulated.points[i].value, upwardSliceAccumulatedTest.points[i].value);
            assertEquals(upwardSliceAccumulated.points[i].amount, upwardSliceAccumulatedTest.points[i].amount);
            assertEquals(upwardSliceAccumulated.points[i].date, upwardSliceAccumulatedTest.points[i].date);
        }
    }

    @Test
    void isIntervalDecreasing() {
        assertTrue(upwardSliceAccumulated.isIntervalDecreasing(1, 2, 100));
        assertFalse(upwardSliceAccumulated.isIntervalDecreasing(1, 2, 1000));
        assertFalse(upwardSliceAccumulated.isIntervalDecreasing(0, 4, 100));
    }

    @Test
    void isIntervalConstant() {
        assertTrue(upwardSliceAccumulated.isIntervalConstant(0, 2, 850));
        assertFalse(upwardSliceAccumulated.isIntervalConstant(0, 2, 400));
    }

    @Test
    void getLocalValueRange() {
        assertEquals(1000, upwardSliceAccumulated.getLocalValueRange(1, 3));
    }

    @Test
    void getDateDistance() {
        assertEquals(30, upwardSliceAccumulated.getDateDistance(1, 3));
    }

    @Test
    void getRelativeSigma() {
        assertEquals(upwardSliceAccumulated.getSigma(), upwardSliceAccumulated.getRelativeSigma() * upwardSliceAccumulated.valueRange, 0.0001);
    }

    @Test
    void equals() {
        final String tableName = "data_test";
        final String valueName = "value_1";
        final String[] colNames = {"category_1", "category_2"};
        final String[] labels = {"type_1", "source_1"};
        final SlicePoint[] points = new SlicePoint[5];
        points[0] = new SlicePoint(100, 5, new Date(10000));
        points[1] = new SlicePoint(150, 3, new Date(10010));
        points[2] = new SlicePoint(-200, 4, new Date(10030));
        points[3] = new SlicePoint(50, 20, new Date(10040));
        points[4] = new SlicePoint(250, 2, new Date(10050));
        Slice upwardSliceSecond = new Slice(tableName, valueName, colNames, labels, points, ApproximationType.LINEAR);
        assertEquals(upwardSlice, upwardSliceSecond);
        assertNotEquals(upwardSliceAccumulated, upwardSliceSecond);
    }
}
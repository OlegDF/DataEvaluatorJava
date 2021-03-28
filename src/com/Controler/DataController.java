package com.Controler;

import com.DataObjects.SuspiciousInterval;
import com.Model.DataRetriever;
import com.Model.DatabaseService;
import com.DataObjects.Slice;
import com.Model.Intervals.IntervalFinder;
import com.Model.Intervals.SimpleIntervalFinder;
import com.Model.SliceRetriever;
import com.View.GraphExporter;

import java.util.List;

public class DataController {

    private DataRetriever dataRetriever;
    private SliceRetriever sliceRetriever;
    private GraphExporter graphExporter;
    private IntervalFinder intervalFinder;

    private DatabaseService dbService;

    public DataController() {
        dbService = new DatabaseService("evaluatordb", "evaluator", "comparison419");
        dataRetriever = new DataRetriever(dbService);
        sliceRetriever = new SliceRetriever(dbService);
        graphExporter = new GraphExporter();
        intervalFinder = new SimpleIntervalFinder();
    }

    public void parseCsv() {
        dataRetriever.csvToDatabase("data_v1.csv");
    }

    public void exportTypeUnitGraphs() {
        List<Slice> slices = sliceRetriever.getTypeUnitSlices();
        for(Slice slice: slices) {
            graphExporter.exportGraphToPng(slice);
        }
    }

    public void exportTypeUnitGraphsAccumulated() {
        List<Slice> slices = sliceRetriever.getTypeUnitSlicesAccumulated();
        for(Slice slice: slices) {
            graphExporter.exportGraphToPng(slice);
        }
    }

    public void exportTypeUnitDecreases() {
        List<Slice> slices = sliceRetriever.getTypeUnitSlicesAccumulated();
        List<SuspiciousInterval> intervals = intervalFinder.getDecreasingIntervals(slices);
        int intervalId = 0;
        for(SuspiciousInterval interval: intervals) {
            graphExporter.exportDecreaseGraphToPng(interval, intervalId);
            intervalId++;
        }
    }

    public void close() {
        dbService.closeConnection();
    }

}

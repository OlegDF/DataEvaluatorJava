package com.Controler;

import com.Model.DataRetriever;
import com.Model.DatabaseService;
import com.DataObjects.Slice;
import com.Model.SliceRetriever;
import com.View.GraphExporter;

import java.util.List;

public class DataController {

    private DataRetriever dataRetriever;
    private SliceRetriever sliceRetriever;
    private GraphExporter graphExporter;

    private DatabaseService dbService;

    public DataController() {
        dbService = new DatabaseService("evaluatordb", "evaluator", "comparison419");
        dataRetriever = new DataRetriever(dbService);
        sliceRetriever = new SliceRetriever(dbService);
        graphExporter = new GraphExporter();
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

    public void close() {
        dbService.closeConnection();
    }

}

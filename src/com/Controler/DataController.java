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

/**
 * Класс, который имеет доступ к остальным элементам программы и может вызывать их методы.
 */
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

    /**
     * Получает разрезы данных, сгруппированных по типу и единице измерения, генерирует граф из каждого разреза и
     * сохраняет граф в виде изображения .png.
     */
    public void exportTypeUnitGraphs() {
        List<Slice> slices = sliceRetriever.getTypeUnitSlices();
        for(Slice slice: slices) {
            graphExporter.exportGraphToPng(slice);
        }
    }

    /**
     * Получает разрезы данных, сгруппированных по типу и единице измерения, делает накопление для каждого разреза,
     * генерирует граф из каждого разреза и сохраняет граф в виде изображения .png.
     */
    public void exportTypeUnitGraphsAccumulated() {
        List<Slice> slices = sliceRetriever.getTypeUnitSlicesAccumulated();
        for(Slice slice: slices) {
            graphExporter.exportGraphToPng(slice);
        }
    }

    /**
     * Получает разрезы данных, сгруппированных по типу и единице измерения, делает накопление для каждого разреза,
     * получает список интервалов, на которых значение убывает, сортирует его по величине убывания, генерирует граф из
     * каждого разреза и сохраняет граф в виде изображения .png.
     */
    public void exportTypeUnitDecreases() {
        List<Slice> slices = sliceRetriever.getTypeUnitSlicesAccumulated();
        List<SuspiciousInterval> intervals = intervalFinder.getDecreasingIntervals(slices);
        int intervalId = 0;
        for(SuspiciousInterval interval: intervals) {
            graphExporter.exportDecreaseGraphToPng(interval, intervalId);
            intervalId++;
        }
    }

    /**
     * Метод, завершающий работу компонентов.
     */
    public void close() {
        dbService.closeConnection();
    }

}

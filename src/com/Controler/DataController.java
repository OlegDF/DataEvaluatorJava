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

    public void parseCsv(String fileName) {
        dataRetriever.csvToDatabase(fileName);
    }

    /**
     * Получает разрезы данных, сгруппированных по типу и единице измерения, делает накопление для каждого разреза,
     * генерирует граф из каждого разреза и сохраняет граф в виде изображения .png.
     *
     * @param tableName - название таблицы, из которой необходимо получать данные
     */
    public void exportTypeUnitGraphsAccumulated(String tableName) {
        List<Slice> slices = sliceRetriever.getTwoCategorySlicesAccumulated(tableName, "category_3", "category_4");
        for(Slice slice: slices) {
            graphExporter.exportGraphToPng(slice);
        }
    }


    /**
     * Получает разрезы данных, сгруппированных по всем значениям одной категорий, делает накопление для каждого разреза,
     * генерирует граф из каждого разреза и сохраняет граф в виде изображения .png.
     * @param tableName - название таблицы, из которой необходимо получать данные
     */
    public void exportSingleCategoryGraphsAccumulated(String tableName) {
        List<List<Slice>> slices = sliceRetriever.getSingleCategorySlicesAccumulated(tableName);
        for(List<Slice> slicesList: slices) {
            for(Slice slice: slicesList) {
                graphExporter.exportGraphToPng(slice);
            }
        }
    }


    /**
     * Получает разрезы данных, сгруппированных по всем сочетаниям двух категорий, делает накопление для каждого разреза,
     * генерирует граф из каждого разреза и сохраняет граф в виде изображения .png.
     * @param tableName - название таблицы, из которой необходимо получать данные
     */
    public void exportDoubleCombinationsGraphsAccumulated(String tableName) {
        List<List<Slice>> slices = sliceRetriever.getDoubleCombinationsSlicesAccumulated(tableName);
        for(List<Slice> slicesList: slices) {
            for(Slice slice: slicesList) {
                graphExporter.exportGraphToPng(slice);
            }
        }
    }

    /**
     * Получает разрезы данных, сгруппированных по типу и единице измерения, делает накопление для каждого разреза,
     * получает список интервалов, на которых значение убывает, сортирует его по величине убывания, генерирует граф из
     * каждого разреза и сохраняет граф в виде изображения .png.
     *
     * @param tableName - название таблицы, из которой необходимо получать данные
     */
    public void exportTypeUnitDecreaseGraphs(String tableName) {
        List<Slice> slices = sliceRetriever.getTwoCategorySlicesAccumulated(tableName, "category_3", "category_4");
        List<SuspiciousInterval> intervals = intervalFinder.getDecreasingIntervals(slices, 1d/16, 1d/8);
        int intervalId = 0;
        for(SuspiciousInterval interval: intervals) {
            graphExporter.exportDecreaseGraphToPng(interval, intervalId);
            intervalId++;
        }
    }

    /**
     * Получает разрезы данных, сгруппированных по всем значениям одной категории, делает накопление для каждого разреза,
     * получает список интервалов, на которых значение убывает, сортирует его по величине убывания, генерирует граф из
     * каждого разреза и сохраняет граф в виде изображения .png.
     *
     * @param tableName - название таблицы, из которой необходимо получать данные
     * @param minIntervalMult - минимальная длина интервалов, которые будут рассматриваться (измеряется как доля длины
     *                        временного промежутка всего разреза, от 0 до 1)
     * @param thresholdMult - минимальная разность между первой и последней величиной для интервалов, которые будут
     *                        рассматриваться (измеряется как доля разности между максимальным и минимальным значением
     *                        на всем разрезе, от 0 до 1)
     */
    public void exportSingleCategoryDecreaseGraphs(String tableName, double minIntervalMult, double thresholdMult) {
        List<List<Slice>> slices = sliceRetriever.getSingleCategorySlicesAccumulated(tableName);
        for(List<Slice> slicesList: slices) {
            List<SuspiciousInterval> intervals = intervalFinder.getDecreasingIntervals(slicesList, minIntervalMult, thresholdMult);
            int intervalId = 0;
            for(SuspiciousInterval interval: intervals) {
                graphExporter.exportDecreaseGraphToPng(interval, intervalId);
                intervalId++;
            }
        }
    }

    /**
     * Получает разрезы данных, сгруппированных по всем сочетаниям двух категорий, делает накопление для каждого разреза,
     * получает список интервалов, на которых значение убывает, сортирует его по величине убывания, генерирует граф из
     * каждого разреза и сохраняет граф в виде изображения .png.
     *
     * @param tableName - название таблицы, из которой необходимо получать данные
     * @param minIntervalMult - минимальная длина интервалов, которые будут рассматриваться (измеряется как доля длины
     *                        временного промежутка всего разреза, от 0 до 1)
     * @param thresholdMult - минимальная разность между первой и последней величиной для интервалов, которые будут
     *                        рассматриваться (измеряется как доля разности между максимальным и минимальным значением
     *                        на всем разрезе, от 0 до 1)
     */
    public void exportDoubleCombinationsDecreaseGraphs(String tableName, double minIntervalMult, double thresholdMult) {
        List<List<Slice>> slices = sliceRetriever.getDoubleCombinationsSlicesAccumulated(tableName);
        for(List<Slice> slicesList: slices) {
            List<SuspiciousInterval> intervals = intervalFinder.getDecreasingIntervals(slicesList, minIntervalMult, thresholdMult);
            int intervalId = 0;
            for(SuspiciousInterval interval: intervals) {
                graphExporter.exportDecreaseGraphToPng(interval, intervalId);
                intervalId++;
            }
        }
    }

    /**
     * Создает таблицу, в которую будут записываться интервалы с уменьшением значений для определенной таблицы.
     *
     * @param tableName - название таблицы, из будет которой необходимо получать данные
     */
    public void createDecreasesTable(String tableName) {
        List<String> categoryNames = dbService.getCategoryNames(tableName);
        final String[] colNames = new String[categoryNames.size() + 3];
        final String[] colTypes = new String[categoryNames.size() + 3];
        for(int i = 0; i < categoryNames.size(); i++) {
            colNames[i] = categoryNames.get(i);
            colTypes[i] = "varchar(255)";
        }
        colNames[colNames.length - 3] = "pos1";
        colTypes[colNames.length - 3] = "int8";
        colNames[colNames.length - 2] = "pos2";
        colTypes[colNames.length - 2] = "int8";
        colNames[colNames.length - 1] = "decrease_score";
        colTypes[colNames.length - 1] = "float";
        dbService.createTable(tableName + "_decreases", colNames, colTypes);
    }

    /**
     * Получает разрезы данных, сгруппированных по всем значениям одной категории, делает накопление для каждого разреза,
     * получает список интервалов, на которых значение убывает, сортирует его по величине убывания и записывает интервал
     * в базу данных.
     *
     * @param tableName - название таблицы, из которой необходимо получать данные
     * @param minIntervalMult - минимальная длина интервалов, которые будут рассматриваться (измеряется как доля длины
     *                        временного промежутка всего разреза, от 0 до 1)
     * @param thresholdMult - минимальная разность между первой и последней величиной для интервалов, которые будут
     *                        рассматриваться (измеряется как доля разности между максимальным и минимальным значением
     *                        на всем разрезе, от 0 до 1)
     */
    public void exportSingleCategoryDecreasesToDB(String tableName, double minIntervalMult, double thresholdMult) {
        String[] colNames = dbService.getCategoryNames(tableName).toArray(new String[0]);
        List<List<Slice>> slices = sliceRetriever.getSingleCategorySlicesAccumulated(tableName);
        for(List<Slice> slicesList: slices) {
            List<SuspiciousInterval> intervals = intervalFinder.getDecreasingIntervals(slicesList, minIntervalMult, thresholdMult);
            for(SuspiciousInterval interval: intervals) {
                Slice slice = interval.slice;
                String[] labels = new String[colNames.length];
                for(int i = 0; i < colNames.length; i++) {
                    labels[i] = dbService.labelNotPresent;
                }
                for(int i = 0; i < colNames.length; i++) {
                    for(int j = 0; j < slice.colNames.length; j++) {
                        if(colNames[i].equals(slice.colNames[j])) {
                            labels[i] = slice.labels[j];
                        }
                    }
                }
                dbService.insertDecrease(tableName + "_decreases", colNames, labels, interval.pos1, interval.pos2, interval.getDecreaseScore());
            }
        }
    }

    /**
     * Получает разрезы данных, сгруппированных по всем сочетаниям двух категорий, делает накопление для каждого разреза,
     * получает список интервалов, на которых значение убывает, сортирует его по величине убывания и записывает интервал
     * в базу данных.
     *
     * @param tableName - название таблицы, из которой необходимо получать данные
     * @param minIntervalMult - минимальная длина интервалов, которые будут рассматриваться (измеряется как доля длины
     *                        временного промежутка всего разреза, от 0 до 1)
     * @param thresholdMult - минимальная разность между первой и последней величиной для интервалов, которые будут
     *                        рассматриваться (измеряется как доля разности между максимальным и минимальным значением
     *                        на всем разрезе, от 0 до 1)
     */
    public void exportDoubleCombinationsDecreasesToDB(String tableName, double minIntervalMult, double thresholdMult) {
        String[] colNames = dbService.getCategoryNames(tableName).toArray(new String[0]);
        List<List<Slice>> slices = sliceRetriever.getDoubleCombinationsSlicesAccumulated(tableName);
        for(List<Slice> slicesList: slices) {
            List<SuspiciousInterval> intervals = intervalFinder.getDecreasingIntervals(slicesList, minIntervalMult, thresholdMult);
            for(SuspiciousInterval interval: intervals) {
                Slice slice = interval.slice;
                String[] labels = new String[colNames.length];
                for(int i = 0; i < colNames.length; i++) {
                    labels[i] = dbService.labelNotPresent;
                }
                for(int i = 0; i < colNames.length; i++) {
                    for(int j = 0; j < slice.colNames.length; j++) {
                        if(colNames[i].equals(slice.colNames[j])) {
                            labels[i] = slice.labels[j];
                        }
                    }
                }
                dbService.insertDecrease(tableName + "_decreases", colNames, labels, interval.pos1, interval.pos2, interval.getDecreaseScore());
            }
        }
    }

    /**
     * Метод, завершающий работу компонентов.
     */
    public void close() {
        dbService.closeConnection();
    }

}

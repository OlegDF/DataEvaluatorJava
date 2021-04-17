package com.Controler;

import com.DataObjects.SuspiciousInterval;
import com.Model.DataRetriever;
import com.Model.DatabaseService;
import com.DataObjects.Slice;
import com.Model.Intervals.IntervalFinder;
import com.Model.Intervals.SimpleIntervalFinder;
import com.Model.SliceRetriever;
import com.SupportClasses.Config;
import com.SupportClasses.ConsoleLogger;
import com.SupportClasses.Logger;
import com.View.GraphExporter;

import java.util.List;

/**
 * Класс, который имеет доступ к остальным элементам программы и может вызывать их методы.
 */
public class DataController {

    private final Config config;
    private final Logger logger;
    private final DataRetriever dataRetriever;
    private final SliceRetriever sliceRetriever;
    private final GraphExporter graphExporter;
    private final IntervalFinder intervalFinder;

    private final DatabaseService dbService;

    private String tableName;
    private int maxCategoriesPerCombo, maxSlicesPerCombo;

    private List<Slice> slicesCache = null;

    public DataController() {
        config = new Config();
        logger = new ConsoleLogger();
        tableName = config.getTableName();
        maxSlicesPerCombo = config.getMaxSlicesPerCombo();
        maxCategoriesPerCombo = config.getMaxCategoriesPerCombo();
        dbService = new DatabaseService(config.getDbName(), config.getUserName(), config.getPassword());
        dataRetriever = new DataRetriever(dbService);
        sliceRetriever = new SliceRetriever(dbService, config.getApproximationType());
        graphExporter = new GraphExporter();
        intervalFinder = new SimpleIntervalFinder();
    }

    public void parseCsv() {
        dataRetriever.csvToDatabase(tableName);
    }

    /**
     * Получает разрезы данных, сгруппированных по всем значениям одной или более категорий, делает накопление для
     * каждого разреза, генерирует граф из каждого разреза и сохраняет граф в виде изображения .png.
     */
    public void exportGraphsAccumulated() {
        logger.logMessage("Начинается экспорт графиков...");
        List<Slice> slices;
        if(slicesCache == null) {
            slicesCache = sliceRetriever.getSlicesAccumulated(tableName, maxCategoriesPerCombo, maxSlicesPerCombo);
        }
        slices = slicesCache;
        int graphsExported = 0;
        for(Slice slice: slices) {
            if(graphExporter.exportGraphToPng(slice)) {
                graphsExported++;
            }
            if(graphsExported % (slices.size() / 10) == 0) {
                logger.logMessage("Экспортировано " + graphsExported + " графиков");
            }
        }
        logger.logMessage("Экспортировано " + graphsExported + " графиков");
        logger.logMessage("Закончился экспорт графиков.");
    }

    /**
     * Получает разрезы данных, сгруппированных по всем значениям одной или более категорий, делает накопление для
     * каждого разреза, получает список интервалов, на которых значение убывает, сортирует его по величине убывания,
     * генерирует граф из каждого разреза и сохраняет граф в виде изображения .png.
     *
     * @param minIntervalMult - минимальная длина интервалов, которые будут рассматриваться (измеряется как доля длины
     *                        временного промежутка всего разреза, от 0 до 1)
     * @param thresholdMult - минимальная разность между первой и последней величиной для интервалов, которые будут
     *                        рассматриваться (измеряется как доля среднеквадратического отклонения)
     * @param maxIntervals - ограничение на количество интервалов, которые вернет алгоритм (выбирается начало списка)
     */
    public void exportDecreaseGraphs(double minIntervalMult, double thresholdMult, int maxIntervals) {
        logger.logMessage("Начинается экспорт графиков уменьшения...");
        List<Slice> slices;
        if(slicesCache == null) {
            slicesCache = sliceRetriever.getSlicesAccumulated(tableName, maxCategoriesPerCombo, maxSlicesPerCombo);
        }
        slices = slicesCache;
        int intervalsExported = 0;
        List<SuspiciousInterval> intervals = intervalFinder.getDecreasingIntervals(slices, minIntervalMult, thresholdMult,
                maxIntervals, true);
        int intervalId = 0;
        for(SuspiciousInterval interval: intervals) {
            if(graphExporter.exportDecreaseGraphToPng(interval, "decreases", intervalId)) {
                intervalsExported++;
            }
            intervalId++;
            if(intervalsExported % (intervals.size() / 10) == 0) {
                logger.logMessage("Экспортировано " + intervalsExported + " графиков");
            }
        }
        logger.logMessage("Экспортировано " + intervalsExported + " графиков");
        logger.logMessage("Закончился экспорт графиков уменьшения.");
    }

    /**
     * Получает разрезы данных, сгруппированных по всем значениям одной или более категорий, делает накопление для
     * каждого разреза, получает список интервалов, на которых значение не изменяется значительно, сортирует его по
     * длине, генерирует граф из каждого разреза и сохраняет граф в виде изображения .png.
     *
     * @param minIntervalMult - минимальная длина интервалов, которые будут рассматриваться (измеряется как доля длины
     *                        временного промежутка всего разреза, от 0 до 1)
     * @param thresholdMult - максимальная разность между максимальной и минимальной величиной для интервалов, которые будут
     *                        рассматриваться (измеряется как доля среднеквадратического отклонения)
     * @param maxIntervals - ограничение на количество интервалов, которые вернет алгоритм (выбирается начало списка)
     */
    public void exportConstantGraphs(double minIntervalMult, double thresholdMult, int maxIntervals) {
        logger.logMessage("Начинается экспорт графиков отсутствия роста...");
        List<Slice> slices;
        if(slicesCache == null) {
            slicesCache = sliceRetriever.getSlicesAccumulated(tableName, maxCategoriesPerCombo, maxSlicesPerCombo);
        }
        slices = slicesCache;
        int intervalsExported = 0;
        List<SuspiciousInterval> intervals = intervalFinder.getConstantIntervals(slices, minIntervalMult, thresholdMult,
                maxIntervals, true);
        int intervalId = 0;
        for(SuspiciousInterval interval: intervals) {
            if(graphExporter.exportDecreaseGraphToPng(interval, "constants", intervalId)) {
                intervalsExported++;
            }
            intervalId++;
            if(intervalsExported % (intervals.size() / 10) == 0) {
                logger.logMessage("Экспортировано " + intervalsExported + " графиков");
            }
        }
        logger.logMessage("Экспортировано " + intervalsExported + " графиков");
        logger.logMessage("Закончился экспорт графиков отсутствия роста.");
    }

    /**
     * Создает таблицу, в которую будут записываться интервалы с уменьшением значений для определенной таблицы.
     */
    public void createDecreasesTable() {
        List<String> categoryNames = dbService.getCategoryNames(tableName);
        final String[] colNames = new String[categoryNames.size() + 5];
        final String[] colTypes = new String[categoryNames.size() + 5];
        for(int i = 0; i < categoryNames.size(); i++) {
            colNames[i] = categoryNames.get(i);
            colTypes[i] = "varchar(255)";
        }
        colNames[colNames.length - 5] = "pos1";
        colTypes[colNames.length - 5] = "int8";
        colNames[colNames.length - 4] = "pos2";
        colTypes[colNames.length - 4] = "int8";
        colNames[colNames.length - 3] = "decrease_score";
        colTypes[colNames.length - 3] = "float";
        colNames[colNames.length - 2] = "relative_width";
        colTypes[colNames.length - 2] = "float";
        colNames[colNames.length - 1] = "relative_diff";
        colTypes[colNames.length - 1] = "float";
        dbService.createTable(tableName + "_decreases", colNames, colTypes);
    }

    /**
     * Получает разрезы данных, сгруппированных по всем значениям одной или более категорий, делает накопление для
     * каждого разреза, получает список интервалов, на которых значение убывает, сортирует его по величине убывания и
     * записывает интервал в базу данных.
     *
     * @param minIntervalMult - минимальная длина интервалов, которые будут рассматриваться (измеряется как доля длины
     *                        временного промежутка всего разреза, от 0 до 1)
     * @param thresholdMult - минимальная разность между первой и последней величиной для интервалов, которые будут
     *                        рассматриваться (измеряется как доля среднеквадратического отклонения)
     * @param maxIntervals - ограничение на количество интервалов, которые вернет алгоритм (выбирается начало списка)
     */
    public void exportDecreasesToDB(double minIntervalMult, double thresholdMult, int maxIntervals) {
        logger.logMessage("Начинается экспорт интервалов уменьшения...");
        String[] colNames = dbService.getCategoryNames(tableName).toArray(new String[0]);
        List<Slice> slices;
        if(slicesCache == null) {
            slicesCache = sliceRetriever.getSlicesAccumulated(tableName, maxCategoriesPerCombo, maxSlicesPerCombo);
        }
        slices = slicesCache;
        List<SuspiciousInterval> intervals = intervalFinder.getDecreasingIntervals(slices, minIntervalMult, thresholdMult,
                maxIntervals, false);
        dbService.insertDecrease(tableName + "_decreases", colNames, intervals);
        logger.logMessage("Экспортировано " + intervals.size() + " интервалов");
        logger.logMessage("Закончился экспорт интервалов уменьшения.");
    }

    /**
     * Создает таблицу, в которую будут записываться интервалы с отсутствием изменения значений для определенной таблицы.
     */
    public void createConstantsTable() {
        List<String> categoryNames = dbService.getCategoryNames(tableName);
        final String[] colNames = new String[categoryNames.size() + 4];
        final String[] colTypes = new String[categoryNames.size() + 4];
        for(int i = 0; i < categoryNames.size(); i++) {
            colNames[i] = categoryNames.get(i);
            colTypes[i] = "varchar(255)";
        }
        colNames[colNames.length - 4] = "pos1";
        colTypes[colNames.length - 4] = "int8";
        colNames[colNames.length - 3] = "pos2";
        colTypes[colNames.length - 3] = "int8";
        colNames[colNames.length - 2] = "relative_width";
        colTypes[colNames.length - 2] = "float";
        colNames[colNames.length - 1] = "relative_value_range";
        colTypes[colNames.length - 1] = "float";
        dbService.createTable(tableName + "_constants", colNames, colTypes);
    }

    /**
     * Получает разрезы данных, сгруппированных по всем значениям одной или более категорий, делает накопление для
     * каждого разреза, получает список интервалов, на которых значение не изменяется значительно, сортирует его по
     * длине и записывает интервал в базу данных.
     *
     * @param minIntervalMult - минимальная длина интервалов, которые будут рассматриваться (измеряется как доля длины
     *                        временного промежутка всего разреза, от 0 до 1)
     * @param thresholdMult - максимальная разность между максимальной и минимальной величиной для интервалов, которые будут
     *                        рассматриваться (измеряется как доля среднеквадратического отклонения)
     * @param maxIntervals - ограничение на количество интервалов, которые вернет алгоритм (выбирается начало списка)
     */
    public void exportConstantsToDB(double minIntervalMult, double thresholdMult, int maxIntervals) {
        logger.logMessage("Начинается экспорт интервалов отсутствия роста...");
        String[] colNames = dbService.getCategoryNames(tableName).toArray(new String[0]);
        List<Slice> slices;
        if(slicesCache == null) {
            slicesCache = sliceRetriever.getSlicesAccumulated(tableName, maxCategoriesPerCombo, maxSlicesPerCombo);
        }
        slices = slicesCache;
        List<SuspiciousInterval> intervals = intervalFinder.getConstantIntervals(slices, minIntervalMult, thresholdMult,
                maxIntervals, false);
        dbService.insertConstant(tableName + "_constants", colNames, intervals);
        logger.logMessage("Экспортировано " + intervals.size() + " интервалов");
        logger.logMessage("Закончился экспорт интервалов отсутствия роста.");
    }

    /**
     * Метод, завершающий работу компонентов.
     */
    public void close() {
        dbService.closeConnection();
    }

}

package com.Model;

import com.DataObjects.Slice;
import com.SupportClasses.ConsoleLogger;
import com.SupportClasses.Logger;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Класс, который получает разрезы данных.
 */
public class SliceRetriever {

    private final DatabaseService databaseService;
    private final Logger logger;

    public SliceRetriever(DatabaseService databaseService) {
        this.databaseService = databaseService;
        logger = new ConsoleLogger();
    }

    /**
     * Получает разрезы данных, сгруппированных по одной категории.
     *
     * @param tableName - название таблицы, из которой необходимо получать данные
     * @param category - название категории
     *
     * @return список разрезов
     */
    public List<Slice> getCategorySlices(String tableName, String category) {
        logger.logMessage("Начинается получение разрезов по категории " + category + "...");
        List<Slice> res = new ArrayList<>();
        String[] categoryLabels = databaseService.getUniqueLabels(tableName, category);
        String[] colNames = {category};
        for(String label: categoryLabels) {
            String[] labels = {"'" + label + "'"};
            res.add(databaseService.getSlice(tableName, colNames, labels));
        }
        res.sort(Comparator.comparingLong(o -> -o.totalAmount));
        logger.logMessage("Закончилось получение разрезов по категории " + category + ", получено " + res.size() + " разрезов.");
        return res.size() >= 10 ? res.subList(0, 10) : res;
    }

    /**
     * Получает разрезы данных, сгруппированных по двум категориям.
     *
     * @param tableName - название таблицы, из которой необходимо получать данные
     * @param category1 - название первой категории
     * @param category2 - название второй категории
     *
     * @return список разрезов
     */
    public List<Slice> getTwoCategorySlices(String tableName, String category1, String category2) {
        logger.logMessage("Начинается получение разрезов по категориям " + category1 + " и " + category2 + "...");
        List<Slice> res = new ArrayList<>();
        String[] category1Labels = databaseService.getUniqueLabels(tableName, category1);
        String[] category2Labels = databaseService.getUniqueLabels(tableName, category2);
        String[] colNames = {category1, category2};
        for(String label1: category1Labels) {
            for(String label2: category2Labels) {
                String[] labels = {"'" + label1 + "'", "'" + label2 + "'"};
                res.add(databaseService.getSlice(tableName, colNames, labels));
            }
        }
        res.sort(Comparator.comparingLong(o -> -o.totalAmount));
        logger.logMessage("Закончилось получение разрезов по категориям " + category1 + " и " + category2 + ", получено " + res.size() + " разрезов.");
        return res.size() >= 10 ? res.subList(0, 10) : res;
    }

    /**
     * Получает разрезы данных, сгруппированных по типу и единице измерения, а также делает накопление для каждого разреза.
     *
     * @param tableName - название таблицы, из которой необходимо получать данные
     * @param category - название категории
     *
     * @return список разрезов с накоплением
     */
    public List<Slice> getCategorySlicesAccumulated(String tableName, String category) {
        List<Slice> res = new ArrayList<>();
        List<Slice> slices = getCategorySlices(tableName, category);
        for(Slice slice: slices) {
            res.add(slice.getAccumulation());
        }
        return res;
    }

    /**
     * Получает разрезы данных, сгруппированных по типу и единице измерения, а также делает накопление для каждого разреза.
     *
     * @param tableName - название таблицы, из которой необходимо получать данные
     * @param category1 - название первой категории
     * @param category2 - название второй категории
     *
     * @return список разрезов с накоплением
     */
    public List<Slice> getTwoCategorySlicesAccumulated(String tableName, String category1, String category2) {
        List<Slice> res = new ArrayList<>();
        List<Slice> slices = getTwoCategorySlices(tableName, category1, category2);
        for(Slice slice: slices) {
            res.add(slice.getAccumulation());
        }
        return res;
    }

    /**
     * Получает разрезы данных, сгруппированных по всем значениям одной категории, делает накопление для каждого разреза,
     * генерирует граф из каждого разреза и сохраняет граф в виде изображения .png.
     *
     * @param tableName - название таблицы, из которой необходимо получать данные
     * @return список разрезов с накоплением
     */
    public List<List<Slice>> getSingleCategorySlicesAccumulated(String tableName) {
        List<String> categoryNames = databaseService.getCategoryNames(tableName);
        List<List<Slice>> res = new ArrayList<>();
        for(String categoryName: categoryNames) {
            res.add(getCategorySlicesAccumulated(tableName, categoryName));
        }
        return res;
    }

    /**
     * Получает разрезы данных, сгруппированных по всем сочетаниям двух категорий, делает накопление для каждого разреза,
     * генерирует граф из каждого разреза и сохраняет граф в виде изображения .png.
     *
     * @param tableName - название таблицы, из которой необходимо получать данные
     * @return список разрезов с накоплением
     */
    public List<List<Slice>> getDoubleCombinationsSlicesAccumulated(String tableName) {
        List<String> categoryNames = databaseService.getCategoryNames(tableName);
        List<List<Slice>> res = new ArrayList<>();
        for(int i = 0; i < categoryNames.size() - 1; i++) {
            for(int j = i + 1; j < categoryNames.size(); j++) {
                res.add(getTwoCategorySlicesAccumulated(tableName, categoryNames.get(i), categoryNames.get(j)));
            }
        }
        return res;
    }

}

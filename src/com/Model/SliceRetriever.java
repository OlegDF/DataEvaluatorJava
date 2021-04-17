package com.Model;

import com.DataObjects.Approximations.ApproximationType;
import com.DataObjects.Slice;
import com.SupportClasses.ConsoleLogger;
import com.SupportClasses.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * Класс, который получает разрезы данных.
 */
public class SliceRetriever {

    private final DatabaseService dbService;
    private final Logger logger;
    private final ApproximationType approximationType;

    public SliceRetriever(DatabaseService dbService, ApproximationType approximationType) {
        this.dbService = dbService;
        this.approximationType = approximationType;
        logger = new ConsoleLogger();
    }

    /**
     * Получает разрезы данных, сгруппированных по ряду категорий.
     *
     * @param tableName - название таблицы, из которой необходимо получать данные
     * @param categories - названия категорий
     * @param maxSlices - максимальное количество разрезов, возвращаемое методом
     *
     * @return список разрезов
     */
    public List<Slice> getCategorySlices(String tableName, String[] categories, int maxSlices) {
        logger.logMessage("Начинается получение разрезов по категориям " + Arrays.toString(categories) + "...");
        List<Slice> res = new ArrayList<>();
        List<String[]> labelCombinations = dbService.getLabelCombinations(tableName, categories, maxSlices);
        for(String[] combination: labelCombinations) {
            res.add(dbService.getSlice(tableName, categories, combination, approximationType));
        }
        res.sort(Comparator.comparingLong(o -> -o.totalAmount));
        logger.logMessage("Закончилось получение разрезов по категории " + Arrays.toString(categories) + ", получено " + res.size() + " разрезов.");
        return res;
    }

    /**
     * Получает разрезы данных, сгруппированных по ряду категорий, а также делает накопление для каждого разреза.
     *
     * @param tableName - название таблицы, из которой необходимо получать данные
     * @param categories - названия категорий
     * @param maxSlices - максимальное количество разрезов, возвращаемое методом
     *
     * @return список разрезов с накоплением
     */
    public List<Slice> getCategorySlicesAccumulated(String tableName, String[] categories, int maxSlices) {
        List<Slice> res = new ArrayList<>();
        List<Slice> slices = getCategorySlices(tableName, categories, maxSlices);
        for(Slice slice: slices) {
            res.add(slice.getAccumulation());
        }
        return res;
    }

    /**
     * Получает разрезы данных, сгруппированных по всем сочетаниям одной или более категорий, делает накопление для
     * каждого разреза, генерирует граф из каждого разреза и сохраняет граф в виде изображения .png.
     *
     * @param maxSlices - максимальное количество разрезов с одной комбинацией ярлыков, возвращаемое методом
     * @param maxCategories - максимальное количество категорий, по которым группируется каждый разрез.
     * @param tableName - название таблицы, из которой необходимо получать данные
     * @return список разрезов с накоплением
     */
    public List<Slice> getSlicesAccumulated(String tableName, int maxCategories, int maxSlices) {
        List<String> categoryNames = dbService.getCategoryNames(tableName);
        List<Slice> res = new ArrayList<>();
        CategoryCombination categoryCombos = new CategoryCombination(categoryNames);
        for(int i = 0; i < maxCategories; i++) {
            for(String[] categories: categoryCombos.combos) {
                res.addAll(getCategorySlicesAccumulated(tableName, categories, maxSlices));
            }
            categoryCombos.addCategory(categoryNames);
        }
        return res;
    }

}

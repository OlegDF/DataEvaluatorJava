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

    private final DatabaseService databaseService;
    private final Logger logger;
    private final ApproximationType approximationType;

    public SliceRetriever(DatabaseService databaseService, ApproximationType approximationType) {
        this.databaseService = databaseService;
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
        List<String[]> labelCombinations = databaseService.getLabelCombinations(tableName, categories, maxSlices);
        for(String[] combination: labelCombinations) {
            res.add(databaseService.getSlice(tableName, categories, combination, approximationType));
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
        List<String> categoryNames = databaseService.getCategoryNames(tableName);
        List<Slice> res = new ArrayList<>();
        List<String[]> categoryCombos = getInitialCategories(categoryNames);
        for(int i = 0; i < maxCategories; i++) {
            for(String[] categories: categoryCombos) {
                res.addAll(getCategorySlicesAccumulated(tableName, categories, maxSlices));
            }
            categoryCombos = addCategory(categoryCombos, categoryNames);
        }
        return res;
    }

    /**
     * Получает список массивов, каждый из которых содержит название одной из категорий.
     *
     * @param categoryNames - список категорий
     * @return список массивов, по 1 на категорию
     */
    private List<String[]> getInitialCategories(List<String> categoryNames) {
        List<String[]> newCombos = new ArrayList<>();
        for(String categoryName: categoryNames) {
            String[] newCombo = {categoryName};
            newCombos.add(newCombo);
        }
        return newCombos;
    }

    /**
     * Добавляет в каждый из полученных списков категорий 1 новую категорию (категории внутри списка сортируются в
     * алфавитном порядке, чтобы избежать повторений); получает список всех комбинаций старых категорий с новой,
     * удовлетворяющих этому условию.
     *
     * @param currentCombos - имеющиеся сочетания категорий
     * @param categoryNames - список категорий
     * @return сочетания категорий, в каждом из которых на 1 категорию больше, чем во входных сочетаниях
     */
    private List<String[]> addCategory(List<String[]> currentCombos, List<String> categoryNames) {
        List<String[]> newCombos = new ArrayList<>();
        for(String[] combo: currentCombos) {
            for(String categoryName: categoryNames) {
                if(categoryName.compareTo(combo[combo.length - 1]) <= 0) {
                    continue;
                }
                String[] newCombo = new String[combo.length + 1];
                System.arraycopy(combo, 0, newCombo, 0, combo.length);
                newCombo[newCombo.length - 1] = categoryName;
                newCombos.add(newCombo);
            }
        }
        return newCombos;
    }

}

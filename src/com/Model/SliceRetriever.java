package com.Model;

import com.DataObjects.Slice;
import com.DataObjects.SuspiciousInterval;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Класс, который получает разрезы данных.
 */
public class SliceRetriever {

    final String tableName = "data";

    private final DatabaseService databaseService;

    public SliceRetriever(DatabaseService databaseService) {
        this.databaseService = databaseService;
    }

    /**
     * Получает разрезы данных, сгруппированных по одной категории.
     *
     * @return список разрезов
     */
    public List<Slice> getCategorySlices(String category) {
        List<Slice> res = new ArrayList<>();
        String[] categoryLabels = databaseService.getUniqueLabels("data", category);
        String[] colNames = {category};
        for(String label: categoryLabels) {
            String[] labels = {"'" + label + "'"};
            res.add(databaseService.getSlice(tableName, colNames, labels));
        }
        res.sort(Comparator.comparingLong(o -> -o.totalAmount));
        return res.size() >= 10 ? res.subList(0, 10) : res;
    }

    /**
     * Получает разрезы данных, сгруппированных по двум категориям.
     *
     * @return список разрезов
     */
    public List<Slice> getTwoCategorySlices(String category1, String category2) {
        List<Slice> res = new ArrayList<>();
        String[] category1Labels = databaseService.getUniqueLabels("data", category1);
        String[] category2Labels = databaseService.getUniqueLabels("data", category2);
        String[] colNames = {category1, category2};
        for(String label1: category1Labels) {
            for(String label2: category2Labels) {
                String[] labels = {"'" + label1 + "'", "'" + label2 + "'"};
                res.add(databaseService.getSlice(tableName, colNames, labels));
            }
        }
        res.sort(Comparator.comparingLong(o -> -o.totalAmount));
        return res.size() >= 10 ? res.subList(0, 10) : res;
    }

    /**
     * Получает разрезы данных, сгруппированных по типу и единице измерения, а также делает накопление для каждого разреза.
     *
     * @return список разрезов с накоплением
     */
    public List<Slice> getCategorySlicesAccumulated(String category) {
        List<Slice> res = new ArrayList<>();
        List<Slice> slices = getCategorySlices(category);
        for(Slice slice: slices) {
            res.add(slice.getAccumulation());
        }
        return res;
    }

    /**
     * Получает разрезы данных, сгруппированных по типу и единице измерения, а также делает накопление для каждого разреза.
     *
     * @return список разрезов с накоплением
     */
    public List<Slice> getTwoCategorySlicesAccumulated(String category1, String category2) {
        List<Slice> res = new ArrayList<>();
        List<Slice> slices = getTwoCategorySlices(category1, category2);
        for(Slice slice: slices) {
            res.add(slice.getAccumulation());
        }
        return res;
    }

    public List<List<Slice>> getDoubleCombinationsSlices() {
        String[] colNames = databaseService.getColumnNames("data");
        List<String> categoryNames = new ArrayList<>();
        for(String colName: colNames) {
            if(colName.startsWith("category_")) {
                categoryNames.add(colName);
            }
        }
        List<List<Slice>> res = new ArrayList<>();
        for(String categoryName: categoryNames) {
            for(String categoryName2: categoryNames) {
                if(!categoryName.equals(categoryName2)) {
                    res.add(getTwoCategorySlices(categoryName, categoryName2));
                }
            }
        }
        return res;
    }

    public List<List<Slice>> getDoubleCombinationsSlicesAccumulated() {
        String[] colNames = databaseService.getColumnNames("data");
        List<String> categoryNames = new ArrayList<>();
        for(String colName: colNames) {
            if(colName.startsWith("category_")) {
                categoryNames.add(colName);
            }
        }
        List<List<Slice>> res = new ArrayList<>();
        for(String categoryName: categoryNames) {
            for(String categoryName2: categoryNames) {
                if(!categoryName.equals(categoryName2)) {
                    res.add(getTwoCategorySlicesAccumulated(categoryName, categoryName2));
                }
            }
        }
        return res;
    }

}

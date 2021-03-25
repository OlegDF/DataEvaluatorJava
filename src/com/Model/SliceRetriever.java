package com.Model;

import com.DataObjects.Slice;

import java.util.ArrayList;
import java.util.List;

public class SliceRetriever {

    final String tableName = "data";

    private final DatabaseService databaseService;

    public SliceRetriever(DatabaseService databaseService) {
        this.databaseService = databaseService;
    }

    public List<Slice> getTypeUnitSlices() {
        List<Slice> res = new ArrayList<>();
        String[] types = databaseService.getUniqueLabels("data", "category_3");
        String[] units = databaseService.getUniqueLabels("data", "category_4");
        String[] colNames = {"category_3", "category_4"};
        for(String type: types) {
            for(String unit: units) {
                String[]  labels = {"'" + type + "'", "'" + unit + "'"};
                res.add(databaseService.getSlice(tableName, colNames, labels));
            }
        }
        return res;
    }

    public List<Slice> getTypeUnitSlicesAccumulated() {
        List<Slice> res = new ArrayList<>();
        String[] types = databaseService.getUniqueLabels("data", "category_3");
        String[] units = databaseService.getUniqueLabels("data", "category_4");
        String[] colNames = {"category_3", "category_4"};
        for(String type: types) {
            for(String unit: units) {
                String[]  labels = {"'" + type + "'", "'" + unit + "'"};
                res.add(databaseService.getSlice(tableName, colNames, labels).getAccumulation());
            }
        }
        return res;
    }

}

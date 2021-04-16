package com;

import com.Controler.DataController;

public class MainExportDecreasesToDB {

    public static void main(String[] args) {

        DataController dataController = new DataController();

        dataController.createDecreasesTable();

        dataController.exportSingleCategoryDecreasesToDB(0.05, 0.01, Integer.MAX_VALUE);
        dataController.exportDoubleCombinationsDecreasesToDB(0.05, 0.01, Integer.MAX_VALUE);

        dataController.createConstantsTable();

        dataController.exportSingleCategoryConstantsToDB(0.05, 0.5, Integer.MAX_VALUE);
        dataController.exportDoubleCombinationsConstantsToDB(0.05, 0.5, Integer.MAX_VALUE);

        dataController.close();

    }

}

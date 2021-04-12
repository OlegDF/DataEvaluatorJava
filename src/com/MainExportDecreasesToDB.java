package com;

import com.Controler.DataController;

public class MainExportDecreasesToDB {

    public static void main(String[] args) {

        DataController dataController = new DataController();

        dataController.createDecreasesTable("data_v06");

        dataController.exportSingleCategoryDecreasesToDB("data_v06", 0, 0, Integer.MAX_VALUE);
        dataController.exportDoubleCombinationsDecreasesToDB("data_v06", 0, 0, Integer.MAX_VALUE);

        dataController.createConstantsTable("data_v06");

        dataController.exportSingleCategoryConstantsToDB("data_v06", 0, 1, Integer.MAX_VALUE);
        dataController.exportDoubleCombinationsConstantsToDB("data_v06", 0, 1, Integer.MAX_VALUE);

        dataController.close();

    }

}

package com;

import com.Controler.DataController;

public class MainExportDecreasesToDB {

    public static void main(String[] args) {

        DataController dataController = new DataController();
        dataController.createDecreasesTable("data_v06");

        dataController.exportSingleCategoryDecreasesToDB("data_v06", 1d/16, 1d/32);
        dataController.exportDoubleCombinationsDecreasesToDB("data_v06", 1d/16, 1d/32);

        dataController.close();

    }

}

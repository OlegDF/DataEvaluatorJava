package com;

import com.Controler.DataController;

public class MainExportGraphs {

    public static void main(String[] args) {

        DataController dataController = new DataController();

        dataController.exportSingleCategoryGraphsAccumulated("data_v06");
        dataController.exportDoubleCombinationsGraphsAccumulated("data_v06");

        dataController.exportSingleCategoryDecreaseGraphs("data_v06");
        dataController.exportDoubleCombinationsDecreaseGraphs("data_v06");

        dataController.close();

    }

}

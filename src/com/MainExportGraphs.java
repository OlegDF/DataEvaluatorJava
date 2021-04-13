package com;

import com.Controler.DataController;

public class MainExportGraphs {

    public static void main(String[] args) {

        DataController dataController = new DataController();

        dataController.exportSingleCategoryGraphsAccumulated();
        dataController.exportDoubleCombinationsGraphsAccumulated();

        dataController.exportSingleCategoryDecreaseGraphs(1d/16, 1d/32, 32);
        dataController.exportDoubleCombinationsDecreaseGraphs(1d/16, 1d/32, 32);

        dataController.close();

    }

}

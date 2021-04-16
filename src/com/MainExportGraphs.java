package com;

import com.Controler.DataController;

public class MainExportGraphs {

    public static void main(String[] args) {

        DataController dataController = new DataController();

        dataController.exportSingleCategoryGraphsAccumulated();
        dataController.exportDoubleCombinationsGraphsAccumulated();

        dataController.exportSingleCategoryDecreaseGraphs(0.1, 0.05, 32);
        dataController.exportDoubleCombinationsDecreaseGraphs(0.1, 0.05, 32);

        dataController.close();

    }

}

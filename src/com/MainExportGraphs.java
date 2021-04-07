package com;

import com.Controler.DataController;

public class MainExportGraphs {

    public static void main(String[] args) {

        DataController dataController = new DataController();

        dataController.exportDoubleCombinationsGraphsAccumulated();

        dataController.exportDoubleCombinationsDecreasesToDatabase();

        dataController.close();

    }

}

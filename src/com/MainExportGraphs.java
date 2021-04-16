package com;

import com.Controler.DataController;

public class MainExportGraphs {

    public static void main(String[] args) {

        DataController dataController = new DataController();

        dataController.exportGraphsAccumulated();

        dataController.exportDecreaseGraphs(0.1, 1, 32);
        dataController.exportConstantGraphs(0.1, 0.2, 32);

        dataController.close();

    }

}

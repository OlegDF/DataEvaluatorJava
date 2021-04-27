package com;

import com.Controler.DataController;

public class MainExportDecreasesToDB {

    public static void main(String[] args) {

        DataController dataController = new DataController();

        dataController.createDecreasesTable();

        dataController.exportDecreasesToDB(0.05, 0.5, Integer.MAX_VALUE);

        dataController.createConstantsTable();

        dataController.exportConstantsToDB(0.05, 1, Integer.MAX_VALUE);

        dataController.close();

    }

}

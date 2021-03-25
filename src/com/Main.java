package com;

import com.Controler.DataController;

public class Main {

    public static void main(String[] args) {

        DataController dataController = new DataController();

        dataController.exportTypeUnitGraphsAccumulated();

        dataController.close();

    }

}

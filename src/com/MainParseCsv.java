package com;

import com.Controler.DataController;

public class MainParseCsv {

    public static void main(String[] args) {

        DataController dataController = new DataController();

        dataController.parseCsv("data_v06");

        dataController.close();

    }

}

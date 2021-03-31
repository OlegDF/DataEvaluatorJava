package com;

import com.Controler.DataController;

public class MainParseCsv {

    public static void main(String[] args) {

        DataController dataController = new DataController();

        dataController.parseCsv();

        dataController.close();

    }

}

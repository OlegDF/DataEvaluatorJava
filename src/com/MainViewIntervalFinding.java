package com;

import com.Controler.DataController;
import com.View.IntervalFindingView;

public class MainViewIntervalFinding {

    public static void main(String[] args) {

        DataController dataController = new DataController();
        IntervalFindingView intervalFindingView = new IntervalFindingView(dataController);

    }

}

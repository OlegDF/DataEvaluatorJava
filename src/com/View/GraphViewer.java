package com.View;

import com.DataObjects.Slice;
import com.DataObjects.SuspiciousInterval;
import com.Model.DatabaseService;
import com.Model.Intervals.IntervalFinder;
import com.Model.Intervals.SimpleIntervalFinder;
import com.Model.SliceRetriever;
import org.jfree.chart.swing.ChartPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

public class GraphViewer {

    private final String undefinedCategory = "-";

    private final SliceRetriever sliceRetriever;
    private final IntervalFinder intervalFinder;
    private final GraphExporter graphExporter;
    private final DatabaseService dbService;

    private final JFrame mainFrame;
    private final JPanel buttonsPanel;
    private final JPanel graphPanel;

    private final JComboBox<String>[] categoriesBoxes;

    private final JButton startCalculationButton;

    private String tableName;
    private List<SuspiciousInterval> decreaseIntervals;

    public GraphViewer(String tableName) {
        dbService = new DatabaseService("evaluatordb", "evaluator", "comparison419");
        sliceRetriever = new SliceRetriever(dbService);
        intervalFinder = new SimpleIntervalFinder();
        graphExporter = new GraphExporter();

        mainFrame = new JFrame();
        buttonsPanel = new JPanel();
        graphPanel = new JPanel();
        startCalculationButton = new JButton();

        categoriesBoxes = new JComboBox[2];
        for(int i = 0; i < categoriesBoxes.length; i++) {
            categoriesBoxes[i] = new JComboBox<>();
        }
        initializeInterface(tableName);
        setupWindow();
    }

    private void initializeInterface(String tableName) {
        this.tableName = tableName;
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        for(JComboBox<String> categoriesBox: categoriesBoxes) {
            fillCategoriesBox(categoriesBox);
            categoriesBox.setVisible(true);
            categoriesBox.setMinimumSize(new Dimension(150, 30));
            categoriesBox.setMaximumSize(new Dimension(150, 30));
            categoriesBox.setAlignmentX(Component.CENTER_ALIGNMENT);
            buttonsPanel.add(categoriesBox, constraints);
        }

        startCalculationButton.setText("Получить графы");
        startCalculationButton.setMinimumSize(new Dimension(100, 30));
        startCalculationButton.setMaximumSize(new Dimension(100, 30));
        startCalculationButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        startCalculationButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                getGraphs();
            }
        });
        buttonsPanel.add(startCalculationButton, constraints);

        buttonsPanel.setMinimumSize(new Dimension(600, 200));
        buttonsPanel.setMaximumSize(new Dimension(600, 200));
        graphPanel.setMinimumSize(new Dimension(600, 400));
        graphPanel.setMaximumSize(new Dimension(600, 400));
        buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.Y_AXIS));

        mainFrame.add(buttonsPanel);
        mainFrame.add(graphPanel);
    }

    private void fillCategoriesBox(JComboBox categoriesBox) {
        List<String> categoryNames = dbService.getCategoryNames(tableName);
        categoriesBox.removeAllItems();
        categoriesBox.addItem("-");
        for(String category: categoryNames) {
            categoriesBox.addItem(category);
        }
    }

    private void setupWindow() {
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setSize(650, 650);
        mainFrame.setLayout(new BoxLayout(mainFrame.getContentPane(), BoxLayout.Y_AXIS));
        mainFrame.setVisible(true);
        mainFrame.revalidate();
    }

    private void getGraphs() {
        List<String> categories = new ArrayList<>();
        for(int i = 0; i < categoriesBoxes.length; i++) {
            String newCategory = (String)categoriesBoxes[i].getSelectedItem();
            if(!newCategory.equals(undefinedCategory)) {
                categories.add(newCategory);
            }
        }
        List<Slice> slices;
        if(categories.size() == 1) {
            slices = sliceRetriever.getCategorySlicesAccumulated(tableName, categories.get(0));
        } else if(categories.size() == 2) {
            slices = sliceRetriever.getTwoCategorySlicesAccumulated(tableName, categories.get(0), categories.get(1));
        } else {
            return;
        }
        decreaseIntervals = intervalFinder.getDecreasingIntervals(slices, 1d/16, 1d/32);
        ChartPanel chartPanel = new ChartPanel(graphExporter.getDecreaseChart(decreaseIntervals.get(0)));
        chartPanel.setPreferredSize(new Dimension(600, 400));
        graphPanel.add(chartPanel);
        mainFrame.revalidate();
    }

}

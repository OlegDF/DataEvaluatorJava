package com.View;

import com.Controler.DataController;
import com.Model.DatabaseService;
import com.SupportClasses.Config;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.io.File;
import java.util.Date;
import java.util.List;

public class IntervalFindingView {

    private final DataController dataController;
    private final Config config;
    private final DatabaseService dbService;

    private final JFrame mainFrame;
    private final JPanel csvParsingPanel;
    private final JPanel intervalsFindingPanel;

    private final JComboBox<String> tableBox;
    private final JTextArea csvFileBox;

    private final JSlider minDateSlider;
    private final JSlider maxDateSlider;

    private final JLabel minDateNumber;
    private final JLabel maxDateNumber;

    private final JButton startCsvParsingButton;
    private final JButton startIntervalRetrievalButton;

    private long dateLowerLimit, dateUpperLimit;
    private Date firstDate, lastDate;

    private String tableName;

    private SwingWorker<Void, Void> csvParsingWorker;
    private SwingWorker<Void, Void> intervalFindingWorker;

    public IntervalFindingView(DataController dataController) {
        this.dataController = dataController;
        config = new Config();
        tableName = config.getTableName();
        dbService = dataController.getDbService();
        getFirstLastDate();

        mainFrame = new JFrame();
        csvParsingPanel = new JPanel();
        intervalsFindingPanel = new JPanel();

        tableBox = new JComboBox<>();
        csvFileBox = new JTextArea();

        minDateSlider = new JSlider();
        maxDateSlider = new JSlider();

        minDateNumber = new JLabel();
        maxDateNumber = new JLabel();

        startIntervalRetrievalButton = new JButton();
        startCsvParsingButton = new JButton();

        initializeInterface(tableName);
        setupWindow();
    }

    private void initializeInterface(String tableName) {
        this.tableName = tableName;
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.fill = GridBagConstraints.HORIZONTAL;

        initializeTableNameBox(constraints);
        initializeSliders(constraints);
        initializeButtons(constraints);
        initializePanels();
    }

    /**
     * Задает размеры и расположение строки, предназначенной для выбора названия таблицы.
     *
     * @param constraints - параметры расположения элементов в окне
     */
    private void initializeTableNameBox(GridBagConstraints constraints) {
        setSize(csvFileBox, 150, 20);
        csvFileBox.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel csvFileLabel = new JLabel();
        csvFileLabel.setText("Название csv-файла: ");
        setSize(csvFileLabel, 150, 20);

        JPanel csvFilePanel = new JPanel();
        setSize(csvFilePanel, 600, 25);
        csvFilePanel.add(csvFileLabel, constraints);
        csvFilePanel.add(csvFileBox, constraints);

        List<String> tableNames = dbService.getTableNames();
        for(String table : tableNames) {
            tableBox.addItem(table);
            if(table.equals(tableName)) {
                tableBox.setSelectedItem(table);
            }
        }
        setSize(tableBox, 150, 20);
        tableBox.setAlignmentX(Component.CENTER_ALIGNMENT);
        tableBox.addItemListener(e -> {
            tableName = (String)tableBox.getSelectedItem();
            getFirstLastDate();
            minDateSlider.setMaximum((int)(dateUpperLimit - dateLowerLimit));
            minDateSlider.setValue(0);
            maxDateSlider.setMaximum((int)(dateUpperLimit - dateLowerLimit));
            maxDateSlider.setValue((int)(dateUpperLimit - dateLowerLimit));
        });
        JLabel tableLabel = new JLabel();
        tableLabel.setText("Название таблицы: ");
        setSize(tableLabel, 150, 20);

        JPanel tablePanel = new JPanel();
        setSize(tablePanel, 600, 25);
        tablePanel.add(tableLabel, constraints);
        tablePanel.add(tableBox, constraints);

        csvParsingPanel.add(csvFilePanel, constraints);
        intervalsFindingPanel.add(tablePanel, constraints);
    }

    /**
     * Задает размеры и расположение слайдеров, которые задают временные границы искомых интервалов.
     *
     * @param constraints - параметры расположения элементов в окне
     */
    private void initializeSliders(GridBagConstraints constraints) {
        initializeSlider(minDateSlider, 0, dateUpperLimit - dateLowerLimit, 0, minDateNumber,
                e -> {
                    if(minDateSlider.getValue() > maxDateSlider.getValue()) {
                        minDateSlider.setValue(maxDateSlider.getValue());
                    }
                    minDateNumber.setText(new Date(dateLowerLimit + minDateSlider.getValue()).toString());
                },
                "Первая дата интервала: ", constraints);
        initializeSlider(maxDateSlider, 0, dateUpperLimit - dateLowerLimit, dateUpperLimit - dateLowerLimit, maxDateNumber,
                e -> {
                    if(maxDateSlider.getValue() < minDateSlider.getValue()) {
                        maxDateSlider.setValue(minDateSlider.getValue());
                    }
                    maxDateNumber.setText(new Date(dateLowerLimit + maxDateSlider.getValue()).toString());
                },
                "Последняя дата интервала: ", constraints);
    }

    private void initializeSlider(JSlider slider, long sliderMin, long sliderMax, long initialValue, JLabel sliderNumber,
                                    ChangeListener listener, String labelText, GridBagConstraints constraints) {
        slider.setMinimum((int)sliderMin);
        slider.setMaximum((int)sliderMax);
        setSize(slider, 150, 20);
        slider.addChangeListener(listener);
        slider.setValue((int)sliderMin);
        slider.setValue((int)sliderMax);
        slider.setValue((int)initialValue);

        JLabel sliderLabel = new JLabel();
        sliderLabel.setText(labelText);
        setSize(sliderLabel, 200, 20);

        setSize(sliderNumber, 200, 20);

        JPanel sliderPanel = new JPanel();
        setSize(sliderPanel, 600, 25);
        sliderPanel.add(sliderLabel, constraints);
        sliderPanel.add(slider, constraints);
        sliderPanel.add(sliderNumber, constraints);
        intervalsFindingPanel.add(sliderPanel, constraints);
    }

    /**
     * Задает размеры и расположение кнопок, которые вычисляют интервалы или загружают исходные данные из файла.
     *
     * @param constraints - параметры расположения элементов в окне
     */
    private void initializeButtons(GridBagConstraints constraints) {
        startCsvParsingButton.setText("Записать данные в таблицу");
        setSize(startCsvParsingButton, 200, 30);
        startCsvParsingButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        startCsvParsingButton.addActionListener(e -> {
            lockInterface();
            csvParsingWorker = new SwingWorker<>() {
                @Override
                protected Void doInBackground() {
                    dataController.setTableName(csvFileBox.getText());
                    File csvFile = new File(csvFileBox.getText() + ".csv");
                    if(csvFile.exists() && csvFile.isFile()) {
                        dataController.parseCsv();
                    }
                    if(csvFileBox.getText().equals(tableName)) {
                        getFirstLastDate();
                    }
                    return null;
                }
                @Override
                public void done() {
                    unlockInterface();
                }
            };
            csvParsingWorker.execute();
        });
        csvParsingPanel.add(startCsvParsingButton, constraints);

        startIntervalRetrievalButton.setText("Начать поиск интервалов");
        setSize(startIntervalRetrievalButton, 200, 30);
        startIntervalRetrievalButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        startIntervalRetrievalButton.addActionListener(e -> {
            lockInterface();
            intervalFindingWorker = new SwingWorker<>() {
                @Override
                protected Void doInBackground() {
                    dataController.setTableName(tableName);
                    dataController.createDecreasesTable();
                    dataController.exportDecreasesToDB(0.05, 0.5, Integer.MAX_VALUE,
                            new Date(dateLowerLimit + minDateSlider.getValue()), new Date(dateLowerLimit + maxDateSlider.getValue()));
                    dataController.createConstantsTable();
                    dataController.exportConstantsToDB(0.05, 1, Integer.MAX_VALUE,
                            new Date(dateLowerLimit + minDateSlider.getValue()), new Date(dateLowerLimit + maxDateSlider.getValue()));
                    return null;
                }
                @Override
                public void done() {
                    unlockInterface();
                }
            };
            intervalFindingWorker.execute();
        });
        intervalsFindingPanel.add(startIntervalRetrievalButton, constraints);
    }

    private void lockInterface() {
        csvFileBox.setEnabled(false);
        tableBox.setEnabled(false);
        minDateSlider.setEnabled(false);
        maxDateSlider.setEnabled(false);
        startCsvParsingButton.setEnabled(false);
        startIntervalRetrievalButton.setEnabled(false);
    }

    private void unlockInterface() {
        csvFileBox.setEnabled(true);
        tableBox.setEnabled(true);
        minDateSlider.setEnabled(true);
        maxDateSlider.setEnabled(true);
        startCsvParsingButton.setEnabled(true);
        startIntervalRetrievalButton.setEnabled(true);
    }

    /**
     * Создает составные части окна, одна из которых содержит интерфейс для загрузки исходных данных из файла, а другая -
     * интерфейс для получения интервалов.
     */
    private void initializePanels() {
        setSize(csvParsingPanel, 650, 130);
        setSize(intervalsFindingPanel, 650, 250);
        csvParsingPanel.setLayout(new BoxLayout(csvParsingPanel, BoxLayout.Y_AXIS));
        intervalsFindingPanel.setLayout(new BoxLayout(intervalsFindingPanel, BoxLayout.Y_AXIS));

        mainFrame.add(csvParsingPanel);
        mainFrame.add(intervalsFindingPanel);
    }

    /**
     * Настраивает и отображает окно.
     */
    private void setupWindow() {
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setSize(650, 400);
        mainFrame.setTitle("Получение интервалов и исходных данных");
        mainFrame.setLayout(new BoxLayout(mainFrame.getContentPane(), BoxLayout.Y_AXIS));
        mainFrame.setVisible(true);
        mainFrame.revalidate();
    }

    /**
     * Получает наименьшую и наибольшую даты исходных данных из определенной таблицы.
     */
    private void getFirstLastDate() {
        List<Date> dates = dbService.getBorderDates(tableName);
        firstDate = dates.get(0);
        lastDate = dates.get(1);
        dateLowerLimit = firstDate.getTime() - 1;
        dateUpperLimit = lastDate.getTime() + 1;
    }

    private void setSize(Component component, int x, int y) {
        component.setMinimumSize(new Dimension(x, y));
        component.setPreferredSize(new Dimension(x, y));
        component.setMaximumSize(new Dimension(x, y));
    }

}

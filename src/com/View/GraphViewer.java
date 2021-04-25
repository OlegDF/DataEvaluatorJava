package com.View;

import com.DataObjects.Approximations.ApproximationType;
import com.Model.CategoryCombination;
import com.SupportClasses.Config;
import com.DataObjects.SuspiciousInterval;
import com.Model.DatabaseService;
import com.Model.Intervals.IntervalFinder;
import com.Model.Intervals.SimpleIntervalFinder;
import com.SupportClasses.ConsoleLogger;
import com.SupportClasses.Logger;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.swing.ChartPanel;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Класс, который отображает окно, способное получать и отображать графики разрезов и интервалов.
 */
public class GraphViewer {

    private final String labelAbsent = "-";
    private final String labelAll = "все значения";
    private boolean simpleMode;

    private final Config config;
    private final Logger logger;
    private final IntervalFinder intervalFinder;
    private final GraphExporter graphExporter;
    private final DatabaseService dbService;

    private final JFrame mainFrame;
    private final JPanel buttonsPanel;
    private final JPanel graphPanel;
    private final JPanel leftRightButtonsPanel;

    private final JComboBox<String> tableBox;
    private final JComboBox<String> interfaceTypeBox;
    private final JComboBox<String> graphTypeBox;

    private final JComboBox<String>[] colNameBoxes;
    private final JComboBox<String>[] labelBoxes;

    private final JSlider minIntervalMultSlider;
    private final JSlider thresholdMultSlider;
    private final JSlider maxCategoriesSlider;
    private final JSlider maxGraphsSlider;

    private final JLabel minIntervalMultNumber;
    private final JLabel thresholdMultNumber;
    private final JLabel maxCategoriesNumber;
    private final JLabel maxGraphsNumber;

    private final JLabel graphsUnavailableText;

    private final int minIntervalSliderWidth = 45, thresholdSliderWidth = 59;
    private final double minIntervalLowerLimit = 0.05, minIntervalUpperLimit = 0.5;
    private final double thresholdLowerLimit = 0.05, thresholdUpperLimit = 3;

    private final JButton startCalculationButton;
    private final JButton graphLeftButton, graphRightButton;
    private final JLabel currentGraphNumber;

    private final List<JPanel> simpleModePanels, regularModePanels;

    private String tableName;
    private ApproximationType approximationType;
    private List<SuspiciousInterval> decreaseIntervals;
    private List<JFreeChart> currentGraphs;
    private int currentInterval;

    /**
     * Создает окно, получающее интервалы из определенной таблицы.
     */
    public GraphViewer() {
        config = new Config();
        logger = new ConsoleLogger();
        tableName = config.getTableName();
        simpleMode = config.getViewerType();
        approximationType = config.getApproximationType();
        dbService = new DatabaseService(config.getDbName(), config.getUserName(), config.getPassword());
        intervalFinder = new SimpleIntervalFinder();
        graphExporter = new GraphExporter();

        mainFrame = new JFrame();
        buttonsPanel = new JPanel();
        graphPanel = new JPanel();
        leftRightButtonsPanel = new JPanel();

        startCalculationButton = new JButton();
        graphLeftButton = new JButton();
        graphRightButton = new JButton();
        currentGraphNumber = new JLabel();

        colNameBoxes = new JComboBox[config.getMaxCategoriesPerCombo()];
        labelBoxes = new JComboBox[config.getMaxCategoriesPerCombo()];
        for(int i = 0; i < colNameBoxes.length; i++) {
            colNameBoxes[i] = new JComboBox<>();
            labelBoxes[i] = new JComboBox<>();
        }

        minIntervalMultSlider = new JSlider();
        thresholdMultSlider = new JSlider();
        maxCategoriesSlider = new JSlider();
        maxGraphsSlider = new JSlider();

        minIntervalMultNumber = new JLabel();
        thresholdMultNumber = new JLabel();
        maxCategoriesNumber = new JLabel();
        maxGraphsNumber = new JLabel();

        graphsUnavailableText = new JLabel();

        simpleModePanels = new ArrayList<>();
        regularModePanels = new ArrayList<>();

        graphTypeBox = new JComboBox<>();
        tableBox = new JComboBox<>();
        interfaceTypeBox = new JComboBox<>();
        initializeInterface(tableName);
        setupWindow();
    }

    private void initializeInterface(String tableName) {
        this.tableName = tableName;
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.fill = GridBagConstraints.HORIZONTAL;

        initializeGraphTypesBox(constraints);
        initializeCategoryBoxes(constraints);
        initializeSliders(constraints);
        initializeButtons(constraints);
        initializeGraphUnavailableText();
        initializePanels();
    }

    /**
     * Задает размеры и расположение строки, предназначенной для выбора типа интервалов (уменьшение или отсутствие роста).
     *
     * @param constraints - параметры расположения элементов в окне
     */
    private void initializeGraphTypesBox(GridBagConstraints constraints) {
        List<String> tableNames = dbService.getTableNames();
        for(String table : tableNames) {
            tableBox.addItem(table);
            if(table.equals(tableName)) {
                tableBox.setSelectedItem(table);
            }
        }
        setSize(tableBox, 120, 30);
        tableBox.setAlignmentX(Component.CENTER_ALIGNMENT);
        tableBox.addItemListener(e -> {
            tableName = (String)tableBox.getSelectedItem();
            if(simpleMode) {
                fillCategoryBoxes();
            }
        });
        JLabel tableLabel = new JLabel();
        tableLabel.setText("Название таблицы: ");
        setSize(tableLabel, 120, 30);

        interfaceTypeBox.addItem("перебор комбинаций категорий");
        interfaceTypeBox.addItem("выбор среза");
        interfaceTypeBox.setSelectedIndex(simpleMode ? 1 : 0);
        setSize(interfaceTypeBox, 120, 30);
        interfaceTypeBox.setAlignmentX(Component.CENTER_ALIGNMENT);
        interfaceTypeBox.addItemListener(e -> {
            simpleMode = interfaceTypeBox.getSelectedIndex() != 0;
            for(JPanel panel: simpleModePanels) {
                panel.setVisible(simpleMode);
            }
            for(JPanel panel: regularModePanels) {
                panel.setVisible(!simpleMode);
            }
        });
        JLabel interfaceTypeLabel = new JLabel();
        interfaceTypeLabel.setText("Режим работы: ");
        setSize(interfaceTypeLabel, 120, 30);

        JPanel tablePanel = new JPanel();
        setSize(tablePanel, 600, 30);
        tablePanel.add(tableLabel, constraints);
        tablePanel.add(tableBox, constraints);
        tablePanel.add(interfaceTypeLabel, constraints);
        tablePanel.add(interfaceTypeBox, constraints);

        graphTypeBox.addItem("уменьшение");
        graphTypeBox.addItem("отсутствие роста");
        setSize(graphTypeBox, 150, 30);
        graphTypeBox.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel graphTypeLabel = new JLabel();
        graphTypeLabel.setText("Тип графиков: ");
        setSize(graphTypeLabel, 150, 30);

        JPanel graphTypePanel = new JPanel();
        setSize(graphTypePanel, 420, 30);
        graphTypePanel.add(graphTypeLabel, constraints);
        graphTypePanel.add(graphTypeBox, constraints);

        buttonsPanel.add(tablePanel, constraints);
        buttonsPanel.add(graphTypePanel, constraints);
    }

    /**
     * Задает размеры и расположение строк, предназначенных для выбора категорий и значений в режиме выбора одного среза.
     *
     * @param constraints - параметры расположения элементов в окне
     */
    private void initializeCategoryBoxes(GridBagConstraints constraints) {
        for(int i = 0; i < colNameBoxes.length; i++) {
            setSize(colNameBoxes[i], 150, 30);
            colNameBoxes[i].setAlignmentX(Component.CENTER_ALIGNMENT);
            setSize(labelBoxes[i], 150, 30);
            labelBoxes[i].setAlignmentX(Component.CENTER_ALIGNMENT);

            JLabel colNameLabel = new JLabel();
            colNameLabel.setText("Категория " + (i + 1) + ":");
            setSize(colNameLabel, 150, 30);

            JPanel colNamePanel = new JPanel();
            setSize(colNamePanel, 600, 30);
            colNamePanel.add(colNameLabel, constraints);
            colNamePanel.add(colNameBoxes[i], constraints);
            colNamePanel.add(labelBoxes[i], constraints);

            buttonsPanel.add(colNamePanel, constraints);
            simpleModePanels.add(colNamePanel);
            colNamePanel.setVisible(simpleMode);
        }
        fillCategoryBoxes();
    }

    /**
     * Заполняет списки категорий для текущей строки.
     */
    private void fillCategoryBoxes() {
        List<String> categoryNames = dbService.getCategoryNames(tableName);
        for(int i = 0; i < colNameBoxes.length; i++) {
            colNameBoxes[i].removeAllItems();
            labelBoxes[i].removeAllItems();
            colNameBoxes[i].addItem(labelAbsent);
            labelBoxes[i].addItem(labelAbsent);
            for(String category: categoryNames) {
                colNameBoxes[i].addItem(category);
            }
            colNameBoxes[i].setSelectedIndex(0);
            final int iFixed = i;
            colNameBoxes[iFixed].addItemListener(e -> {
                String colName = (String)colNameBoxes[iFixed].getSelectedItem();
                if(colName != null) {
                    if(!colName.equals(labelAbsent)) {
                        fillLabelBox(iFixed);
                    }
                }
            });
        }
    }

    /**
     * Заполняет список значений определенной категории.
     *
     * @param i - список категорий
     */
    private void fillLabelBox(int i) {
        labelBoxes[i].removeAllItems();
        labelBoxes[i].addItem(labelAbsent);
        labelBoxes[i].addItem(labelAll);
        List<String> labelsList = dbService.getLabelList(tableName, (String)colNameBoxes[i].getSelectedItem(), config.getMaxSlicesPerCombo());
        for(String label: labelsList) {
            labelBoxes[i].addItem(label);
        }
    }

    /**
     * Задает размеры и расположение слайдеров, которые задают необходимые параметры интервалов (ширину и разность значений)
     *
     * @param constraints - параметры расположения элементов в окне
     */
    private void initializeSliders(GridBagConstraints constraints) {
        initializeSlider(minIntervalMultSlider, 0, minIntervalSliderWidth, 5, minIntervalMultNumber,
                e -> minIntervalMultNumber.setText(roundNumber(getMinInterval())),
                "Ограничение на ширину: ", constraints);
        initializeSlider(thresholdMultSlider, 0, thresholdSliderWidth, 9, thresholdMultNumber,
                e -> thresholdMultNumber.setText(roundNumber(getThreshold()) + " * sigma"), "Ограничение на разность величин: ", constraints);
        JPanel maxCategoriesPanel = initializeSlider(maxCategoriesSlider, 1, config.getMaxCategoriesPerCombo(), config.getMaxCategoriesPerCombo(), maxCategoriesNumber,
                e -> maxCategoriesNumber.setText(maxCategoriesSlider.getValue() + ""), "Максимальное количество категорий: ", constraints);
        regularModePanels.add(maxCategoriesPanel);
        maxCategoriesPanel.setVisible(!simpleMode);
        initializeSlider(maxGraphsSlider, 1, 100, 16, maxGraphsNumber,
                e -> maxGraphsNumber.setText(maxGraphsSlider.getValue() + ""), "Максимальное количество графиков: ", constraints);
    }

    private String roundNumber(double num) {
        return new BigDecimal(num).setScale(2, RoundingMode.HALF_UP) + "";
    }

    private JPanel initializeSlider(JSlider slider, int sliderMin, int sliderMax, int initialValue, JLabel sliderNumber,
                                    ChangeListener listener, String labelText, GridBagConstraints constraints) {
        slider.setMinimum(sliderMin);
        slider.setMaximum(sliderMax);
        setSize(slider, 200, 30);
        slider.addChangeListener(listener);
        slider.setValue(sliderMin);
        slider.setValue(sliderMax);
        slider.setValue(initialValue);

        JLabel sliderLabel = new JLabel();
        sliderLabel.setText(labelText);
        setSize(sliderLabel, 250, 30);

        setSize(sliderNumber, 100, 30);

        JPanel sliderPanel = new JPanel();
        setSize(sliderPanel, 600, 30);
        sliderPanel.add(sliderLabel, constraints);
        sliderPanel.add(slider, constraints);
        sliderPanel.add(sliderNumber, constraints);
        buttonsPanel.add(sliderPanel, constraints);
        return sliderPanel;
    }

    /**
     * Задает размеры и расположение кнопок, которые получают интервалы или изменяют номер отображаемого графа.
     *
     * @param constraints - параметры расположения элементов в окне
     */
    private void initializeButtons(GridBagConstraints constraints) {
        startCalculationButton.setText("Получить графики");
        setSize(startCalculationButton, 200, 30);
        startCalculationButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        startCalculationButton.addActionListener(e -> getIntervals());
        buttonsPanel.add(startCalculationButton, constraints);

        graphLeftButton.setText("<");
        graphLeftButton.setFont(new Font("Arial", Font.BOLD, 32));
        setSize(graphLeftButton, 70, 40);
        graphLeftButton.addActionListener(e -> moveGraphLeft());

        graphRightButton.setText(">");
        graphRightButton.setFont(new Font("Arial", Font.BOLD, 32));
        setSize(graphRightButton, 70, 40);
        graphRightButton.addActionListener(e -> moveGraphRight());

        currentGraphNumber.setFont(new Font("Arial", Font.PLAIN, 24));
        setSize(currentGraphNumber, 90, 40);

        setSize(leftRightButtonsPanel, 300, 50);
        leftRightButtonsPanel.add(graphLeftButton);
        leftRightButtonsPanel.add(currentGraphNumber);
        leftRightButtonsPanel.add(graphRightButton);
    }

    /**
     * Создает блок текста, который отображается, если выбранные параметры не позволяют найти ни одного интервала.
     */
    private void initializeGraphUnavailableText() {
        graphsUnavailableText.setText("<html><body style='width: 400px; text-align:center'>Интервалов с заданными параметрами не обнаружено.</body></html>");
        graphsUnavailableText.setFont(new Font("Arial", Font.PLAIN, 32));
        setSize(graphsUnavailableText, 500, 400);
        graphsUnavailableText.setForeground(new Color(51, 51, 51));
        graphsUnavailableText.setBackground(new Color(238, 238, 238));
        graphsUnavailableText.setVerticalAlignment(SwingConstants.CENTER);
        graphsUnavailableText.setHorizontalAlignment(SwingConstants.CENTER);
    }

    /**
     * Создает составные части окна, одна из которых содержит кнопки с параметрами интервалов, а другая - собственно граф.
     */
    private void initializePanels() {
        setSize(buttonsPanel, 600, 300);
        setSize(graphPanel, 600, 480);
        buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.Y_AXIS));

        mainFrame.add(buttonsPanel);
        mainFrame.add(graphPanel);
    }

    /**
     * Настраивает и отображает окно.
     */
    private void setupWindow() {
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setSize(650, 800);
        mainFrame.setTitle("Графики интервалов");
        mainFrame.setLayout(new BoxLayout(mainFrame.getContentPane(), BoxLayout.Y_AXIS));
        mainFrame.setVisible(true);
        mainFrame.revalidate();
    }

    /**
     * Получает интервалы с уменьшением, сгруппированные по всем сочетаниям значений из выбранных категорий, и
     * отображает наиболее значимый из них на графике.
     */
    private void getIntervals() {
        if(simpleMode) {
            if(!getGraphsSimple()) {
                return;
            }
        } else {
            getGraphsRegular();
        }
        intervalFinder.removeIntersectingIntervals(decreaseIntervals);
        if(decreaseIntervals.size() > maxGraphsSlider.getValue()) {
            decreaseIntervals = decreaseIntervals.subList(0, maxGraphsSlider.getValue());
        }
        currentGraphs = new ArrayList<>();
        currentInterval = 0;
        if(decreaseIntervals.size() > 0) {
            currentGraphs.add(graphExporter.getDecreaseChart(decreaseIntervals.get(0)));
            drawGraph();
        } else {
            displayLackOfGraphs();
        }
    }

    private boolean getGraphsSimple() {
        List<String> colNames = new ArrayList<>();
        List<String[]> labels = new ArrayList<>();
        String colName = (String)colNameBoxes[0].getSelectedItem();
        String label = (String)labelBoxes[0].getSelectedItem();
        if(!colName.equals(labelAbsent) && !label.equals(labelAbsent)) {
            colNames.add(colName);
            if(!label.equals(labelAll)) {
                String[] labelsComboNew = new String[1];
                labelsComboNew[0] = "'" + label + "'";
                labels.add(labelsComboNew);
            } else {
                for(int j = 2; j < labelBoxes[0].getItemCount(); j++) {
                    String[] labelsComboNew = new String[1];
                    labelsComboNew[0] = "'" + labelBoxes[0].getItemAt(j) + "'";
                    labels.add(labelsComboNew);
                }
            }
        }
        for(int i = 1; i < colNameBoxes.length; i++) {
            colName = (String)colNameBoxes[i].getSelectedItem();
            label = (String)labelBoxes[i].getSelectedItem();
            List<String[]> labelsNew = new ArrayList<>();
            if(!colName.equals(labelAbsent) && !label.equals(labelAbsent)) {
                colNames.add(colName);
                if(!label.equals(labelAll)) {
                    for(String[] labelsCombo: labels) {
                        String[] labelsComboNew = Arrays.copyOf(labelsCombo, labelsCombo.length + 1);
                        labelsComboNew[labelsComboNew.length - 1] = "'" + label + "'";
                        labelsNew.add(labelsComboNew);
                    }
                } else {
                    for(String[] labelsCombo: labels) {
                        for(int j = 2; j < labelBoxes[i].getItemCount(); j++) {
                            String[] labelsComboNew = Arrays.copyOf(labelsCombo, labelsCombo.length + 1);
                            labelsComboNew[labelsComboNew.length - 1] = "'" + labelBoxes[i].getItemAt(j) + "'";
                            labelsNew.add(labelsComboNew);
                        }
                    }
                }
                labels = labelsNew;
            }
        }
        System.out.println(colNames.toString());
        System.out.println(labels.size());
        if(colNames.size() <= 0) {
            return false;
        }
        decreaseIntervals = new ArrayList<>();
        for(String[] labelsCombo: labels) {
            switch(graphTypeBox.getSelectedIndex()) {
                case 0:
                    logger.logMessage("Начинается получение графиков уменьшения...");
                    decreaseIntervals.addAll(dbService.getDecreasesSimple(tableName, colNames.toArray(new String[0]),
                            labelsCombo, approximationType, getMinInterval(), getThreshold()));
                    logger.logMessage("Закончено получение графиков уменьшения.");
                    break;
                case 1:
                    logger.logMessage("Начинается получение графиков отсутствия роста...");
                    decreaseIntervals.addAll(dbService.getConstantsSimple(tableName, colNames.toArray(new String[0]),
                            labelsCombo, approximationType, getMinInterval(), getThreshold()));
                    logger.logMessage("Закончено получение графиков отсутствия роста.");
                    break;
            }
        }
        return true;
    }

    private void getGraphsRegular() {
        List<String> categoryNames = dbService.getCategoryNames(tableName);
        List<String[]> categoryCombosFinal = new ArrayList<>();
        CategoryCombination categoryCombos = new CategoryCombination(categoryNames);
        for(int i = 1; i <= maxCategoriesSlider.getValue(); i++) {
            categoryCombosFinal.addAll(categoryCombos.combos);
            if(i < maxCategoriesSlider.getValue()) {
                categoryCombos.addCategory(categoryNames);
            }
        }
        switch(graphTypeBox.getSelectedIndex()) {
            case 0:
                logger.logMessage("Начинается получение графиков уменьшения...");
                decreaseIntervals = dbService.getDecreases(tableName, categoryCombosFinal, approximationType,
                        getMinInterval(), getThreshold());
                logger.logMessage("Закончено получение графиков уменьшения.");
                break;
            case 1:
                logger.logMessage("Начинается получение графиков отсутствия роста...");
                decreaseIntervals = dbService.getConstants(tableName, categoryCombosFinal, approximationType,
                        getMinInterval(), getThreshold());
                logger.logMessage("Закончено получение графиков отсутствия роста.");
                break;
        }
    }

    /**
     * Отображает график под текущим выбранным номером.
     */
    private void drawGraph() {
        ChartPanel chartPanel;
        if(currentInterval >= currentGraphs.size()) {
            currentGraphs.add(graphExporter.getDecreaseChart(decreaseIntervals.get(currentInterval)));
        }
        chartPanel = new ChartPanel(currentGraphs.get(currentInterval));
        setSize(chartPanel, 600, 400);
        graphPanel.removeAll();
        graphPanel.add(chartPanel);
        graphPanel.add(leftRightButtonsPanel);
        graphLeftButton.setVisible(currentInterval > 0);
        graphRightButton.setVisible(currentInterval < decreaseIntervals.size() - 1);
        currentGraphNumber.setText((currentInterval + 1) + "/" + decreaseIntervals.size());
        graphPanel.revalidate();
        mainFrame.revalidate();
        mainFrame.repaint();
    }

    /**
     * Отображает сообщение об отсутствии интервалов, удовлетворяющих выбранным параметрам.
     */
    private void displayLackOfGraphs() {
        graphPanel.removeAll();
        graphPanel.add(graphsUnavailableText);
        graphPanel.revalidate();
        mainFrame.revalidate();
        mainFrame.repaint();
    }

    /**
     * Сдвигает номер выборанного графика на -1.
     */
    private void moveGraphLeft() {
        if(currentInterval > 0) {
            currentInterval--;
            drawGraph();
        }
    }

    /**
     * Сдвигает номер выборанного графика на 1.
     */
    private void moveGraphRight() {
        if(currentInterval < decreaseIntervals.size() - 1) {
            currentInterval++;
            drawGraph();
        }
    }

    /**
     * Получает из позиции соответствующего ползунка значение множителя минимальной ширины интервалов.
     *
     * @return минимальная ширина интервалов
     */
    private double getMinInterval() {
        return minIntervalLowerLimit + (double)minIntervalMultSlider.getValue() / minIntervalSliderWidth *
                (minIntervalUpperLimit - minIntervalLowerLimit);
    }

    /**
     * Получает из позиции соответствующего ползунка значение множителя разности величин интервалов.
     *
     * @return разность величин интервалов
     */
    private double getThreshold() {
        return thresholdLowerLimit + (double)thresholdMultSlider.getValue() / thresholdSliderWidth * (
                thresholdUpperLimit - thresholdLowerLimit);
    }

    private void setSize(Component component, int x, int y) {
        component.setMinimumSize(new Dimension(x, y));
        component.setPreferredSize(new Dimension(x, y));
        component.setMaximumSize(new Dimension(x, y));
    }

}

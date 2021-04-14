package com.View;

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
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Класс, который отображает окно, способное получать и отображать графики разрезов и интервалов.
 */
public class GraphViewer {

    private final String undefinedCategory = "-";

    private final Config config;
    private final Logger logger;
    private final IntervalFinder intervalFinder;
    private final GraphExporter graphExporter;
    private final DatabaseService dbService;

    private final JFrame mainFrame;
    private final JPanel buttonsPanel;
    private final JPanel graphPanel;
    private final JPanel leftRightButtonsPanel;

    private final JComboBox<String> graphTypeBox;
    private final JComboBox<String>[] categoriesBoxes;
    private final JSlider minIntervalMultSlider;
    private final JSlider thresholdMultSlider;
    private final JLabel minIntervalMultNumber;
    private final JLabel thresholdMultNumber;

    private final JLabel graphsUnavailableText;

    private final int sliderWidth = 100;

    private final JButton startCalculationButton;
    private final JButton graphLeftButton, graphRightButton;
    private final JLabel currentGraphNumber;

    private String tableName;
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

        minIntervalMultSlider = new JSlider();
        thresholdMultSlider = new JSlider();
        minIntervalMultNumber = new JLabel();
        thresholdMultNumber = new JLabel();

        graphsUnavailableText = new JLabel();

        graphTypeBox = new JComboBox();
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

        initializeCategoriesBoxes(constraints);
        initializeSliders(constraints);
        initializeButtons(constraints);
        initializeGraphUnavailableText();
        initializePanels();
    }

    /**
     * Задает размеры и расположение строк, предназначенных для выбора категорий, по которым будут получены интервалы.
     *
     * @param constraints - параметры расположения элементов в окне
     */
    private void initializeCategoriesBoxes(GridBagConstraints constraints) {
        graphTypeBox.addItem("уменьшение");
        graphTypeBox.addItem("отсутствие роста");
        setSize(graphTypeBox, 150, 30);
        graphTypeBox.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel graphTypeLabel = new JLabel();
        graphTypeLabel.setText("Тип графиков: ");
        setSize(graphTypeLabel, 100, 30);

        JPanel graphTypePanel = new JPanel();
        setSize(graphTypePanel, 370, 30);
        graphTypePanel.add(graphTypeLabel, constraints);
        graphTypePanel.add(graphTypeBox, constraints);

        buttonsPanel.add(graphTypePanel, constraints);
        for(int i = 0; i < categoriesBoxes.length; i++) {
            fillCategoriesBox(categoriesBoxes[i]);
            setSize(categoriesBoxes[i], 150, 30);
            categoriesBoxes[i].setAlignmentX(Component.CENTER_ALIGNMENT);

            JLabel categoriesLabel = new JLabel();
            categoriesLabel.setText("Категория " + (i + 1) + ": ");
            setSize(categoriesLabel, 100, 30);

            JPanel categoriesPanel = new JPanel();
            setSize(categoriesPanel, 370, 30);
            categoriesPanel.add(categoriesLabel, constraints);
            categoriesPanel.add(categoriesBoxes[i], constraints);

            buttonsPanel.add(categoriesPanel, constraints);
        }
    }

    /**
     * Задает размеры и расположение слайдеров, которые задают необходимые параметры интервалов (ширину и разность значений)
     *
     * @param constraints - параметры расположения элементов в окне
     */
    private void initializeSliders(GridBagConstraints constraints) {
        minIntervalMultSlider.setMinimum(0);
        minIntervalMultSlider.setMaximum(sliderWidth);
        minIntervalMultSlider.setValue(sliderWidth * 10 / 100);
        setSize(minIntervalMultSlider, 200, 30);
        minIntervalMultSlider.addChangeListener(e -> minIntervalMultNumber.setText((double)minIntervalMultSlider.getValue() / sliderWidth + ""));

        JLabel minIntervalMultLabel = new JLabel();
        minIntervalMultLabel.setText("Ограничение на ширину: ");
        setSize(minIntervalMultLabel, 200, 30);

        minIntervalMultNumber.setText((double)minIntervalMultSlider.getValue() / sliderWidth + "");
        setSize(minIntervalMultNumber, 50, 30);

        JPanel minIntervalMultPanel = new JPanel();
        setSize(minIntervalMultPanel, 500, 30);
        minIntervalMultPanel.add(minIntervalMultLabel, constraints);
        minIntervalMultPanel.add(minIntervalMultSlider, constraints);
        minIntervalMultPanel.add(minIntervalMultNumber, constraints);

        thresholdMultSlider.setMinimum(0);
        thresholdMultSlider.setMaximum(sliderWidth);
        thresholdMultSlider.setValue(sliderWidth * 5 / 100);
        setSize(thresholdMultSlider, 200, 30);
        thresholdMultSlider.addChangeListener(e -> thresholdMultNumber.setText((double)thresholdMultSlider.getValue() / sliderWidth + ""));

        JLabel thresholdMultLabel = new JLabel();
        thresholdMultLabel.setText("Ограничение на разность величин: ");
        setSize(thresholdMultLabel, 200, 30);

        thresholdMultNumber.setText((double)thresholdMultSlider.getValue() / sliderWidth + "");
        setSize(thresholdMultNumber, 50, 30);

        JPanel thresholdMultPanel = new JPanel();
        setSize(thresholdMultPanel, 500, 30);
        thresholdMultPanel.add(thresholdMultLabel, constraints);
        thresholdMultPanel.add(thresholdMultSlider, constraints);
        thresholdMultPanel.add(thresholdMultNumber, constraints);

        buttonsPanel.add(minIntervalMultPanel, constraints);
        buttonsPanel.add(thresholdMultPanel, constraints);
    }

    /**
     * Задает размеры и расположение кнопок, которые получают интервалы или изменяют номер отображаемого графа.
     *
     * @param constraints - параметры расположения элементов в окне
     */
    private void initializeButtons(GridBagConstraints constraints) {
        startCalculationButton.setText("Получить графы");
        setSize(startCalculationButton, 100, 30);
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
        setSize(buttonsPanel, 600, 200);
        setSize(graphPanel, 600, 480);
        buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.Y_AXIS));

        mainFrame.add(buttonsPanel);
        mainFrame.add(graphPanel);
    }

    /**
     * Настраивает одно из меню, с помощью которых выбираются категории, и наполняет их списком категорий из
     * соответствующей таблицы (а также дополнительной строкой, обозначающей отсутствие категории).
     *
     * @param categoriesBox - меню, которое необходимо настроить
     */
    private void fillCategoriesBox(JComboBox categoriesBox) {
        List<String> categoryNames = dbService.getCategoryNames(tableName);
        categoriesBox.removeAllItems();
        categoriesBox.addItem(undefinedCategory);
        for(String category: categoryNames) {
            categoriesBox.addItem(category);
        }
    }

    /**
     * Настраивает и отображает окно.
     */
    private void setupWindow() {
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setSize(650, 700);
        mainFrame.setLayout(new BoxLayout(mainFrame.getContentPane(), BoxLayout.Y_AXIS));
        mainFrame.setVisible(true);
        mainFrame.revalidate();
    }

    /**
     * Получает интервалы с уменьшением, сгруппированные по всем сочетаниям значений из выбранных категорий, и
     * отображает наиболее значимый из них на графике.
     */
    private void getIntervals() {
        List<String> categories = new ArrayList<>();
        for(int i = 0; i < categoriesBoxes.length; i++) {
            String newCategory = (String)categoriesBoxes[i].getSelectedItem();
            if(!newCategory.equals(undefinedCategory)) {
                categories.add(newCategory);
            }
        }
        String[] colNames = categories.toArray(new String[0]);
        if(categories.size() > 0) {
            switch(graphTypeBox.getSelectedIndex()) {
                case 0:
                    logger.logMessage("Начинается получение графиков уменьшения...");
                    decreaseIntervals = dbService.getDecreases(tableName, colNames,
                            (double)minIntervalMultSlider.getValue() / sliderWidth,
                            (double)thresholdMultSlider.getValue() / sliderWidth);
                    logger.logMessage("Закончено получение графиков уменьшения.");
                    break;
                case 1:
                    logger.logMessage("Начинается получение графиков отсутствия роста...");
                    decreaseIntervals = dbService.getConstants(tableName, colNames,
                            (double)minIntervalMultSlider.getValue() / sliderWidth,
                            (double)thresholdMultSlider.getValue() / sliderWidth);
                    logger.logMessage("Закончено получение графиков отсутствия роста.");
                    break;
            }
            intervalFinder.removeIntersectingIntervals(decreaseIntervals);
            currentGraphs = new ArrayList<>();
        } else {
            return;
        }
        currentInterval = 0;
        if(decreaseIntervals.size() > 0) {
            currentGraphs.add(graphExporter.getDecreaseChart(decreaseIntervals.get(0)));
            drawGraph();
        } else {
            displayLackOfGraphs();
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

    private void setSize(Component component, int x, int y) {
        component.setMinimumSize(new Dimension(x, y));
        component.setPreferredSize(new Dimension(x, y));
        component.setMaximumSize(new Dimension(x, y));
    }

}

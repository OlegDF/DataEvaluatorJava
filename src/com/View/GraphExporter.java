package com.View;

import com.DataObjects.Slice;
import com.DataObjects.SlicePoint;
import com.DataObjects.SuspiciousInterval;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.time.Millisecond;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.time.TimeSeriesDataItem;

import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;

/**
 * Класс, который экспортирует разрезы данных и интервалы в виде изображений на диске.
 */
public class GraphExporter {

    private final Date currentDate;

    public GraphExporter() {
        currentDate = new Date();
    }

    /**
     * Генерирует граф из заданного разреза и сохраняет граф в виде изображения .png. Ничего не происходит, если
     * в разрезе менее 2 точек.
     *
     * @param slice - разрез данных
     * @return true, если экспорт прошел успешно, иначе false
     */
    public boolean exportGraphToPng(Slice slice) {
        if(slice.points.length < 2) {
            return false;
        }
        StringBuilder directoryName = new StringBuilder("graphs/" + currentDate.getTime() + "/" + slice.tableName + "/accumulated_" + slice.valueName + "/");
        for(int i = 0; i < slice.colNames.length; i++) {
            directoryName.append(slice.colNames[i]);
            if(i < slice.colNames.length - 1) {
                directoryName.append("_");
            }
        }
        directoryName.append("/");
        StringBuilder imageName = new StringBuilder(directoryName);
        for(int i = 0; i < slice.colNames.length; i++) {
            imageName.append(slice.labels[i]);
            if(i < slice.colNames.length - 1) {
                imageName.append("_");
            }
        }
        imageName.append(".png");
        JFreeChart chart = getGraph(slice);
        File path = new File(directoryName.toString());
        path.mkdirs();
        try {
            OutputStream out = new FileOutputStream(imageName.toString());
            ChartUtils.writeChartAsPNG(out,
                    chart,
                    800,
                    600);
        } catch (IOException ex) {
            ex.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * Генерирует граф из разреза, на котором расположен заданный интервал (сам интервал выделяется цветом) и сохраняет
     * граф в виде изображения .png. Ничего не происходит, если в разрезе менее 2 точек.
     *
     * @param interval - интервал
     * @param subdirectory - название директории (обычно decrease или constant)
     * @param intervalId - номер, под которым следует сохранить изображение
     * @return true, если экспорт прошел успешно, иначе false
     */
    public boolean exportDecreaseGraphToPng(SuspiciousInterval interval, String subdirectory, int intervalId) {
        Slice slice = interval.slice;
        if(slice.points.length < 2) {
            return false;
        }
        StringBuilder chartTitle = new StringBuilder();
        StringBuilder directoryName = new StringBuilder("graphs/" + currentDate.getTime() + "/" + slice.tableName +
                "/" + subdirectory + "_" + slice.valueName + "/");
        StringBuilder imageName = new StringBuilder(directoryName).append(intervalId).append("_");
        for(int i = 0; i < slice.colNames.length; i++) {
            chartTitle.append(slice.labels[i]);
            imageName.append(slice.labels[i]);
            if(i < slice.colNames.length - 1) {
                chartTitle.append("; ");
                imageName.append("_");
            }
        }
        imageName.append(".png");
        JFreeChart chart = getDecreaseChart(interval);
        File path = new File(directoryName.toString());
        path.mkdirs();
        try {
            OutputStream out = new FileOutputStream(imageName.toString());
            ChartUtils.writeChartAsPNG(out,
                    chart,
                    800,
                    600);
        } catch (IOException ex) {
            ex.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * Генерирует граф из заданного разреза.
     *
     * @param slice - разрез данных
     * @return граф
     */
    public JFreeChart getGraph(Slice slice) {
        String chartTitle = getChartTitle(slice);
        TimeSeries series = new TimeSeries("Значение");
        final int chunkLength = Integer.max(slice.points.length / 4096, 1);
        for(int i = 0; i < slice.points.length; i += chunkLength) {
            series.add(new Millisecond(slice.points[i].date), slice.points[i].value * slice.points[i].amount);
        }
        TimeSeriesCollection dataset = new TimeSeriesCollection();
        dataset.addSeries(series);
        addApproximation(slice, dataset);
        JFreeChart chart = ChartFactory.createTimeSeriesChart(chartTitle, "Date", "Value", dataset);
        XYPlot plot = (XYPlot) chart.getPlot();
        plot.getRenderer().setSeriesPaint(0, new Color(0, 0, 192));
        plot.getRenderer().setSeriesPaint(1, new Color(128, 255, 192));
        plot.getRenderer().setSeriesPaint(2, new Color(128, 255, 192));
        plot.getRenderer().setSeriesVisibleInLegend(1, Boolean.FALSE, true);
        plot.getRenderer().setSeriesVisibleInLegend(2, Boolean.FALSE, true);
        return chart;
    }

    /**
     * Генерирует граф из разреза, на котором расположен заданный интервал (сам интервал выделяется цветом).
     *
     * @param interval - интервал
     * @return граф
     */
    public JFreeChart getDecreaseChart(SuspiciousInterval interval) {
        Slice slice = interval.slice;
        String chartTitle = getChartTitle(slice);
        TimeSeries mainSeries = new TimeSeries("Значение");
        final int chunkLength = Integer.max(slice.points.length / 4096, 1);
        for(int i = 0; i <= interval.pos1; i += chunkLength) {
            mainSeries.add(new Millisecond(slice.points[i].date), slice.points[i].value * slice.points[i].amount);
        }
        TimeSeries decreaseSeries = new TimeSeries("Интервал с уменьшением");
        for(int i = interval.pos1; i <= interval.pos2; i += chunkLength) {
            decreaseSeries.add(new Millisecond(slice.points[i].date), slice.points[i].value * slice.points[i].amount);
        }
        TimeSeries mainSeries2 = new TimeSeries("Значение");
        for(int i = interval.pos2; i < slice.points.length; i += chunkLength) {
            mainSeries2.add(new Millisecond(slice.points[i].date), slice.points[i].value * slice.points[i].amount);
        }
        TimeSeriesCollection dataset = new TimeSeriesCollection();
        dataset.addSeries(mainSeries);
        dataset.addSeries(decreaseSeries);
        dataset.addSeries(mainSeries2);
        addApproximation(slice, dataset);
        if(interval.hasPartialApproximation()) {
            addPartialApproximation(interval, dataset);
        }
        JFreeChart chart = ChartFactory.createTimeSeriesChart(chartTitle, "Date", "Value", dataset);
        XYPlot plot = (XYPlot) chart.getPlot();
        plot.getRangeAxis().setRange(plot.getRangeAxis().getRange().getLowerBound(), plot.getRangeAxis().getRange().getLowerBound() + slice.valueRange * 1.5);
        plot.getRenderer().setSeriesPaint(0, new Color(0, 0, 192));
        plot.getRenderer().setSeriesPaint(1, new Color(255, 0, 0));
        plot.getRenderer().setSeriesPaint(2, new Color(0, 0, 192));
        plot.getRenderer().setSeriesPaint(3, new Color(64, 192, 192));
        plot.getRenderer().setSeriesPaint(4, new Color(64, 192, 192));
        if(interval.hasPartialApproximation()) {
            plot.getRenderer().setSeriesPaint(5, new Color(32, 192, 64));
            plot.getRenderer().setSeriesPaint(6, new Color(32, 192, 64));
        }
        plot.getRenderer().setSeriesVisibleInLegend(2, Boolean.FALSE, true);
        plot.getRenderer().setSeriesVisibleInLegend(3, Boolean.FALSE, true);
        if(interval.hasPartialApproximation()) {
            plot.getRenderer().setSeriesVisibleInLegend(5, Boolean.FALSE, true);
        }
        return chart;
    }

    /**
     * Добавляет на график линию функции регрессии и две линии, расположенные на sigma выше и на sigma ниже.
     *
     * @param slice - срез, из которого составляется график
     * @param dataset - набор данных, в который вставляются линии
     */
    private void addApproximation(Slice slice, TimeSeriesCollection dataset) {
        TimeSeries approximationLower = new TimeSeries("Приближение");
        TimeSeries approximationUpper = new TimeSeries("Приближение");
        final int chunkLength = Integer.max(slice.points.length / 4096, 1);
        for(int i = 0; i < slice.points.length; i += chunkLength) {
            approximationLower.add(new TimeSeriesDataItem(new Millisecond(slice.points[i].date),
                    slice.getApproximate(i) - slice.getSigma()));
            approximationUpper.add(new TimeSeriesDataItem(new Millisecond(slice.points[i].date),
                    slice.getApproximate(i) + slice.getSigma()));
        }
        dataset.addSeries(approximationLower);
        dataset.addSeries(approximationUpper);
    }

    /**
     * Добавляет на график линию функции частичной регрессии (от 0 до начала интервала уменьшения) и две линии,
     * расположенные на sigma выше и на sigma ниже.
     *
     * @param interval - интервал, из которого составляется график
     * @param dataset - набор данных, в который вставляются линии
     */
    private void addPartialApproximation(SuspiciousInterval interval, TimeSeriesCollection dataset) {
        TimeSeries approximationLower = new TimeSeries("Частичное приближение");
        TimeSeries approximationUpper = new TimeSeries("Частичное приближение");
        final int chunkLength = Integer.max(interval.slice.points.length / 4096, 1);
        for(int i = 0; i < interval.slice.points.length; i += chunkLength) {
            approximationLower.add(new TimeSeriesDataItem(new Millisecond(interval.slice.points[i].date),
                    interval.getPartialApproximate(i) - interval.getPartialSigma()));
            approximationUpper.add(new TimeSeriesDataItem(new Millisecond(interval.slice.points[i].date),
                    interval.getPartialApproximate(i) + interval.getPartialSigma()));
        }
        dataset.addSeries(approximationLower);
        dataset.addSeries(approximationUpper);
    }

    private String getChartTitle(Slice slice) {
        StringBuilder chartTitle = new StringBuilder();
        for(int i = 0; i < slice.colNames.length; i++) {
            chartTitle.append(slice.labels[i]);
            if(i < slice.colNames.length - 1) {
                chartTitle.append("; ");
            }
        }
        return chartTitle.toString();
    }

}

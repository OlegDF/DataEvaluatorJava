package com.View;

import com.DataObjects.Slice;
import com.DataObjects.SlicePoint;
import com.DataObjects.SuspiciousInterval;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.data.time.Millisecond;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

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
        StringBuilder directoryName = new StringBuilder("graphs/" + currentDate.getTime() + "/" + slice.tableName + "/accumulated/");
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
     * @param intervalId - номер, под которым следует сохранить изображение
     * @return true, если экспорт прошел успешно, иначе false
     */
    public boolean exportDecreaseGraphToPng(SuspiciousInterval interval, int intervalId) {
        Slice slice = interval.slice;
        if(slice.points.length < 2) {
            return false;
        }
        StringBuilder chartTitle = new StringBuilder();
        StringBuilder directoryName = new StringBuilder("graphs/" + currentDate.getTime() + "/" + slice.tableName + "/decrease/");
        for(int i = 0; i < slice.colNames.length; i++) {
            directoryName.append(slice.colNames[i]);
            if(i < slice.colNames.length - 1) {
                directoryName.append("_");
            }
        }
        directoryName.append("/");
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

    public JFreeChart getGraph(Slice slice) {
        String chartTitle = getChartTitle(slice);
        TimeSeries series = new TimeSeries("Value Over Time");
        for(SlicePoint point: slice.points) {
            series.addOrUpdate(new Millisecond(point.date), point.value * point.amount);
        }
        TimeSeriesCollection dataset = new TimeSeriesCollection();
        dataset.addSeries(series);
        return ChartFactory.createTimeSeriesChart(chartTitle, "Date", "Value", dataset);
    }

    public JFreeChart getDecreaseChart(SuspiciousInterval interval) {
        Slice slice = interval.slice;
        String chartTitle = getChartTitle(slice);
        TimeSeries mainSeries = new TimeSeries("Value Before");
        for(int i = 0; i < interval.pos1; i++) {
            mainSeries.addOrUpdate(new Millisecond(slice.points[i].date), slice.points[i].value * slice.points[i].amount);
        }
        TimeSeries decreaseSeries = new TimeSeries("Decrease");
        for(int i = interval.pos1; i <= interval.pos2; i++) {
            decreaseSeries.addOrUpdate(new Millisecond(slice.points[i].date), slice.points[i].value * slice.points[i].amount);
        }
        TimeSeries mainSeries2 = new TimeSeries("Value After");
        for(int i = interval.pos2; i < slice.points.length; i++) {
            mainSeries2.addOrUpdate(new Millisecond(slice.points[i].date), slice.points[i].value * slice.points[i].amount);
        }
        TimeSeriesCollection dataset = new TimeSeriesCollection();
        dataset.addSeries(mainSeries);
        dataset.addSeries(decreaseSeries);
        dataset.addSeries(mainSeries2);
        return ChartFactory.createTimeSeriesChart(chartTitle.toString(), "Date", "Value", dataset);
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

    private void clearDirectory(File path) {
        for(File file: path.listFiles()) {
            if(!file.isDirectory()) {
                file.delete();
            }
        }
    }

}

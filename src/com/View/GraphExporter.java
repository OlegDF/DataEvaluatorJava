package com.View;

import com.DataObjects.Slice;
import com.DataObjects.SlicePoint;
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

public class GraphExporter {

    public GraphExporter() {}

    public boolean exportGraphToPng(Slice slice) {
        if(slice.points.length < 2) {
            return false;
        }
        StringBuilder chartTitle = new StringBuilder();
        StringBuilder imageName = new StringBuilder("graphs/type_unit/");
        for(int i = 0; i < slice.colNames.length; i++) {
            chartTitle.append(slice.labels[i]);
            imageName.append(slice.labels[i]);
            if(i < slice.colNames.length - 1) {
                chartTitle.append("; ");
                imageName.append("_");
            }
        }
        imageName.append(".png");
        TimeSeries series = new TimeSeries("Value Over Time");
        for(SlicePoint point: slice.points) {
            series.addOrUpdate(new Millisecond(point.date), point.value * point.amount);
        }
        TimeSeriesCollection dataset = new TimeSeriesCollection();
        dataset.addSeries(series);
        JFreeChart chart = ChartFactory.createTimeSeriesChart(chartTitle.toString(), "Date", "Value", dataset);
        File path = new File("graphs/type_unit/");
        path.mkdirs();
        try {
            OutputStream out = new FileOutputStream(imageName.toString());
            ChartUtils.writeChartAsPNG(out,
                    chart,
                    800,
                    600);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return true;
    }

}

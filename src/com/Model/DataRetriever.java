package com.Model;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Pattern;

public class DataRetriever {

    final String tableName = "data";

    private final DatabaseService databaseService;

    public DataRetriever(DatabaseService databaseService) {
        this.databaseService = databaseService;
    }

    public void csvToDatabase(String csvPath) {
        try {
            BufferedReader lineReader = new BufferedReader(new FileReader(csvPath));

            String[] colNames = lineReader.readLine().split(";");
            String rowLine = lineReader.readLine();
            String[] row = rowLine.split(";");
            String[] colTypes = getColTypes(row);

            databaseService.createTable(tableName, colNames, colTypes);

            while(rowLine != null) {
                row = rowLine.split(";");
                databaseService.insertData(tableName, colNames, colTypes, row);
                rowLine = lineReader.readLine();
            }
        } catch (IOException ex) {
            System.err.println(ex);
        }
    }

    private String[] getColTypes(String[] firstRow) {
        String[] colTypes = new String[firstRow.length];

        final Pattern intPattern = Pattern.compile("-?\\d+");
        final Pattern floatPattern = Pattern.compile("-?\\d+(\\.\\d+)?");
        final Pattern timestampPattern = Pattern.compile("\\d{4}-\\d{2}-\\d{2} ");

        for(int i = 0; i < firstRow.length; i++) {
            if(intPattern.matcher(firstRow[i]).matches()) {
                colTypes[i] = "int8";
            } else if(floatPattern.matcher(firstRow[i]).matches()) {
                colTypes[i] = "float";
            } else if(timestampPattern.matcher(firstRow[i]).matches()) {
                colTypes[i] = "timestamptz";
            } else {
                colTypes[i] = "string";
            }
        }
        return colTypes;
    }

}

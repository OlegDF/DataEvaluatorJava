package com.Model;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Pattern;

/**
 * Класс, который считывает данные из файла и передает их на запись в базу данных.
 */
public class DataRetriever {

    private final DatabaseService databaseService;

    public DataRetriever(DatabaseService databaseService) {
        this.databaseService = databaseService;
    }

    /**
     * Обрабатывает содержимое файла csv и записывает его содержимое в базу данных, с которой установлена связь.
     * Название файла становится названием новой таблицы, в которую записываются данные.
     *
     * @param tableName - название файла csv/путь к нему
     */
    public void csvToDatabase(String tableName) {
        try {
            BufferedReader lineReader = new BufferedReader(new FileReader(tableName + ".csv"));

            String[] colNames = lineReader.readLine().split(";", -1);
            String rowLine = lineReader.readLine();
            String[] row = rowLine.split(";", -1);
            String[] colTypes = getColTypes(row);

            databaseService.createTable(tableName, colNames, colTypes);

            while(rowLine != null) {
                row = rowLine.split(";", -1);
                databaseService.insertData(tableName, colNames, colTypes, row);
                rowLine = lineReader.readLine();
            }
        } catch (IOException ex) {
            System.err.println(ex);
        }
    }

    /**
     * Определяет типы столбцов новой таблицы по формату данных в первой строке файла. Возможные типы - целое число
     * (int8), десятичное число (float), дата/время (timestamptz) и строка (varchar).
     *
     * @param firstRow - первая строка, содержащая данные для вставки в таблицу
     * @return список названий типов данных
     */
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
                colTypes[i] = "varchar(255)";
            }
        }
        return colTypes;
    }

}

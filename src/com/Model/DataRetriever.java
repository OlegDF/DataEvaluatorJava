package com.Model;

import com.SupportClasses.ConsoleLogger;
import com.SupportClasses.Logger;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Класс, который считывает данные из файла и передает их на запись в базу данных.
 */
public class DataRetriever {

    private final DatabaseService dbService;
    private final Logger logger;

    public DataRetriever(DatabaseService dbService) {
        this.dbService = dbService;
        logger = new ConsoleLogger();
    }

    /**
     * Обрабатывает содержимое файла csv и записывает его содержимое в базу данных, с которой установлена связь.
     * Название файла становится названием новой таблицы, в которую записываются данные.
     *
     * @param tableName - название файла csv/путь к нему
     */
    public void csvToDatabase(String tableName) {
        logger.logMessage("Начинается экспорт файла " + tableName + ".csv в таблицу...");
        try {
            BufferedReader lineReader = new BufferedReader(new FileReader(tableName + ".csv"));

            String[] colNames = lineReader.readLine().split(";", -1);
            String rowLine = lineReader.readLine();
            String[] row = rowLine.split(";", -1);
            String[] colTypes = getColTypes(colNames, row);

            dbService.createTable(tableName, colNames, colTypes);

            int rowsExported = 0;
            List<String[]> rows = new ArrayList<>();
            while(rowLine != null) {
                row = rowLine.split(";", -1);
                rows.add(row);
                rowLine = lineReader.readLine();
                rowsExported++;
                if(rowsExported % 1000 == 0) {
                    dbService.insertData(tableName, colNames, colTypes, rows);
                    rows = new ArrayList<>();
                    logger.logMessage("Экспортировано " + rowsExported + " строк");
                }
            }
            dbService.insertData(tableName, colNames, colTypes, rows);
            logger.logMessage("Экспортировано " + rowsExported + " строк");

            String[] colNamesLabels = {"category", "label"};
            String[] colTypesLabels = {"varchar(255)", "varchar(255)"};
            dbService.createTable(tableName + "_labels", colNamesLabels, colTypesLabels);
            dbService.insertLabelList(tableName);
            logger.logMessage("Закончен экспорт файла " + tableName + ".csv в таблицу.");
        } catch (IOException ex) {
            System.err.println(ex);
            logger.logError("Не удалось экспортировать файл" + tableName + ".csv");
        }
    }

    /**
     * Определяет типы столбцов новой таблицы по формату данных в первой строке файла. Возможные типы - целое число
     * (int8), десятичное число (float), дата/время (timestamptz) и строка (varchar).
     *
     * @param firstRow - первая строка, содержащая данные для вставки в таблицу
     * @return список названий типов данных
     */
    private String[] getColTypes(String[] colNames, String[] firstRow) {
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
            if(colNames[i].startsWith("category") || colNames[i].startsWith("version")) {
                colTypes[i] = "varchar(255)";
            }
        }
        return colTypes;
    }

}

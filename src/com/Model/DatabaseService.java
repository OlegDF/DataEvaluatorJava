package com.Model;

import com.DataObjects.Approximations.ApproximationType;
import com.DataObjects.Slice;
import com.DataObjects.SlicePoint;
import com.DataObjects.SuspiciousInterval;
import com.SupportClasses.ConsoleLogger;
import com.SupportClasses.Logger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Сервис, который управляет запросами к базе данных Postgres.
 */
public class DatabaseService {

    private final String labelNotPresent = "NOT_APPLICABLE";

    private Connection connection = null;

    private final Logger logger;

    /**
     * Конструктор, устанавливающий соединение с базой данных с указанным названием, именем пользователя и паролем.
     *
     * @param db       - название базы данных
     * @param user     - имя пользователя
     * @param password - пароль
     */
    public DatabaseService(String address, String db, String user, String password) {
        logger = new ConsoleLogger();
        openConnection(address, db, user, password);
    }

    private void openConnection(String address, String db, String user, String password) {
        final String url = "jdbc:postgresql://" + address + "/" + db + "?user=" + user + "&password=" + password;
        try {
            connection = DriverManager.getConnection(url);
            logger.logMessage("Установлено подключение к базе данных " + db);
        } catch (SQLException ex) {
            logger.logError("Не удалось подключиться к базе данных " + db);
            handleSQLException(ex);
        }
    }

    /**
     * Создает таблицу с указанным названием, в которой будут храниться результаты работы. Также удаляет существующую
     * таблицу с таким же названием, если она существует.
     *
     * @param tableName - название таблицы
     * @param colNames  - названия столбцов таблицы
     * @param colTypes  - типы данных в соответствующих столбцах
     */
    public void createTable(String tableName, String[] colNames, String[] colTypes) {
        if(connection == null) {
            return;
        }
        StringBuilder query = new StringBuilder();
        try {
            connection.createStatement().executeUpdate("DROP TABLE IF EXISTS " + tableName + ";");
            query.append("CREATE TABLE ").append(tableName).append(" (");
            for (int i = 0; i < colNames.length; i++) {
                query.append(colNames[i]).append(" ").append(colTypes[i]);
                if (i < colNames.length - 1) {
                    query.append(", ");
                }
            }
            query.append(");");
            connection.createStatement().executeUpdate(query.toString());
            logger.logMessage("Создана таблица: " + tableName);
        } catch (SQLException ex) {
            logger.logError("Не удалось создать таблицу по запросу: " + query);
            handleSQLException(ex);
        }
    }

    /**
     * Вставляет в таблицу новуые строки с указанными значениями данных.
     *
     * @param tableName - название таблицы
     * @param colNames  - названия столбцов таблицы
     * @param colTypes  - типы данных в соответствующих столбцах
     * @param rows      - значения в новоых строках в строковом виде
     */
    public void insertData(String tableName, String[] colNames, String[] colTypes, List<String[]> rows) {
        if(connection == null) {
            return;
        }
        StringBuilder query = new StringBuilder();
        query.append("INSERT INTO ").append(tableName).append("(");
        for (int i = 0; i < colNames.length; i++) {
            query.append(colNames[i]);
            if (i < colNames.length - 1) {
                query.append(", ");
            }
        }
        query.append(") VALUES (");
        final Pattern floatPattern = Pattern.compile("-?\\d+(\\.\\d+)?");
        for (int k = 0; k < rows.size(); k++) {
            String[] row = rows.get(k);
            if(row.length != colNames.length) {
                continue;
            }
            for (int i = 0; i < row.length; i++) {
                if (colTypes[i].equals("varchar(255)") || colTypes[i].equals("timestamptz")) {
                    query.append("'").append(row[i]).append("'");
                } else {
                    if (floatPattern.matcher(row[i]).matches()) {
                        query.append(row[i]);
                    } else {
                        query.append(0);
                    }
                }
                if (i < row.length - 1) {
                    query.append(", ");
                }
            }
            if (k < rows.size() - 1) {
                query.append("),(");
            }
        }
        query.append(");");
        try {
            connection.createStatement().executeUpdate(query.toString());
        } catch (SQLException ex) {
            logger.logError("Не удалось вставить строку данных по запросу: " + query);
            handleSQLException(ex);
        }
    }

    /**
     * Получает разрез - набор данных, в которых один или более столбцов равны заданным значениям.
     *
     * @param tableName         - название таблицы
     * @param valueName         - название ряда данных
     * @param colNames          - названия столбцов, по которым отбираются данные
     * @param labels            - значения в соответствующих столбцах в строковом виде
     * @param approximationType - тип функции приближения
     * @return объект-разрез
     */
    public Slice getSlice(String tableName, String valueName, String[] colNames, String[] labels, ApproximationType approximationType,
                          Date minDate, Date maxDate) {
        if(connection == null) {
            return new Slice(tableName, valueName, colNames, labels);
        }
        StringBuilder query = new StringBuilder();
        try {
            query.append("SELECT * FROM ").append(tableName).append(" WHERE ");
            for (int i = 0; i < colNames.length; i++) {
                query.append(colNames[i]).append("=");
                query.append(labels[i]);
                if (i < colNames.length - 1) {
                    query.append(" AND ");
                }
            }
            query.append(" AND first_date >= '").append(minDate).append("' AND first_date <= '").append(maxDate).append("'");
            query.append(" ORDER BY first_date;");
            ResultSet res = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY).executeQuery(query.toString());
            res.last();
            int length = res.getRow();
            SlicePoint[] points = new SlicePoint[length];
            res.beforeFirst();
            int i = 0;
            while (res.next()) {
                points[i] = new SlicePoint(res.getLong(valueName), res.getLong("amount"), res.getTimestamp("first_date"));
                i++;
            }
            return new Slice(tableName, valueName, colNames, labels, points, approximationType);
        } catch (SQLException ex) {
            logger.logError("Не удалось получить разрез по запросу: " + query);
            handleSQLException(ex);
        }
        return new Slice(tableName, valueName, colNames, labels);
    }

    /**
     * Возвращает наименьшую и наибольшую даты исходных данных из определенной таблицы.
     *
     * @param tableName - название таблицы
     * @return список с 2 датами - наименьшей и наибольшей
     */
    public List<Date> getBorderDates(String tableName) {
        if(connection == null) {
            List<Date> dates = new ArrayList<>();
            dates.add(new Date());
            dates.add(new Date());
            return dates;
        }
        String query = "";
        try {
            query = "SELECT MIN(first_date) AS min_date, MAX(first_date) AS max_date FROM " + tableName + ";";
            ResultSet res = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY).executeQuery(query);
            List<Date> dates = new ArrayList<>();
            res.next();
            dates.add(res.getTimestamp("min_date"));
            dates.add(res.getTimestamp("max_date"));
            return dates;
        } catch (SQLException ex) {
            logger.logError("Не удалось получить список столбцов по запросу: " + query);
            handleSQLException(ex);
        }
        return new ArrayList<>();
    }

    /**
     * Получает список уникальных значений, которые принимают данные в указанных столбцах.
     *
     * @param tableName - название таблицы
     * @param colNames  - названия столбцов
     * @return список значений в строковом виде
     */
    public List<String[]> getLabelCombinations(String tableName, String[] colNames, int maxCount) {
        if(connection == null) {
            return new ArrayList<>();
        }
        StringBuilder query = new StringBuilder();
        try {
            query.append("SELECT ");
            for (int i = 0; i < colNames.length; i++) {
                query.append(colNames[i]);
                if (i < colNames.length - 1) {
                    query.append(", ");
                }
            }
            query.append(" FROM ").append(tableName).append(" GROUP BY ");
            for (int i = 0; i < colNames.length; i++) {
                query.append(colNames[i]);
                if (i < colNames.length - 1) {
                    query.append(", ");
                }
            }
            query.append(" ORDER BY sum(amount) DESC LIMIT ").append(maxCount).append(";");
            ResultSet res = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY).executeQuery(query.toString());
            res.beforeFirst();
            List<String[]> labelCombinations = new ArrayList<>();
            while (res.next()) {
                String[] combination = new String[colNames.length];
                for (int i = 0; i < colNames.length; i++) {
                    combination[i] = "'" + res.getString(colNames[i]) + "'";
                }
                labelCombinations.add(combination);
            }
            return labelCombinations;
        } catch (SQLException ex) {
            logger.logError("Не удалось получить разрез по запросу: " + query);
            handleSQLException(ex);
        }
        return new ArrayList<>();
    }

    /**
     * Получает список значений категорий из таблицы исходных данных и записывает их в новую таблицу.
     *
     * @param tableName - название таблицы
     */
    public void insertLabelList(String tableName) {
        if(connection == null) {
            return;
        }
        StringBuilder query = new StringBuilder();
        try {
            List<String> categories = getCategoryNames(tableName);
            for (String category : categories) {
                query = new StringBuilder();
                query.append("SELECT ").append(category).append(" FROM ").append(tableName).append(" GROUP BY ").
                        append(category).append(" ORDER BY sum(amount) DESC;");
                ResultSet res = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY).executeQuery(query.toString());
                res.beforeFirst();
                List<String> labels = new ArrayList<>();
                while (res.next()) {
                    String colName = res.getString(category);
                    labels.add(colName);
                }
                query = new StringBuilder();
                query.append("INSERT INTO ").append(tableName).append("_labels(category, label) VALUES(");
                for (int i = 0; i < labels.size(); i++) {
                    query.append("'").append(category).append("','").append(labels.get(i)).append("'");
                    if (i < labels.size() - 1) {
                        query.append("),(");
                    }
                }
                query.append(");");
                connection.createStatement().executeUpdate(query.toString());
            }
        } catch (SQLException ex) {
            logger.logError("Не удалось получить разрез по запросу: " + query);
            handleSQLException(ex);
        }
    }

    public List<String> getLabelList(String tableName, String category, int maxCount) {
        if(connection == null) {
            return new ArrayList<>();
        }
        String query = "";
        try {
            query = "SELECT label FROM " + tableName + "_labels WHERE category = '" + category + "' LIMIT " + maxCount + ";";
            ResultSet res = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY).executeQuery(query);
            res.last();
            List<String> labels = new ArrayList<>();
            res.beforeFirst();
            while (res.next()) {
                labels.add(res.getString("label"));
            }
            return labels;
        } catch (SQLException ex) {
            logger.logError("Не удалось получить список столбцов по запросу: " + query);
            handleSQLException(ex);
        }
        return new ArrayList<>();
    }

    /**
     * Получает список названий столбцов с категориями в определенной таблице.
     *
     * @param tableName - название таблицы
     * @return список столбцов в строковом виде
     */
    public List<String> getCategoryNames(String tableName) {
        if(connection == null) {
            return new ArrayList<>();
        }
        String query = "";
        try {
            query = "SELECT column_name FROM information_schema.columns WHERE table_name = '" + tableName + "' ORDER BY column_name;";
            ResultSet res = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY).executeQuery(query);
            res.last();
            List<String> categoryNames = new ArrayList<>();
            res.beforeFirst();
            while (res.next()) {
                String colName = res.getString("column_name");
                if (colName.startsWith("category_")) {
                    categoryNames.add(colName);
                }
            }
            return categoryNames;
        } catch (SQLException ex) {
            logger.logError("Не удалось получить список столбцов по запросу: " + query);
            handleSQLException(ex);
        }
        return new ArrayList<>();
    }

    /**
     * Получает список названий столбцов с рядами данных.
     *
     * @param tableName - название таблицы
     * @return список столбцов в строковом виде
     */
    public List<String> getValueNames(String tableName) {
        if(connection == null) {
            return new ArrayList<>();
        }
        String query = "";
        try {
            query = "SELECT column_name FROM information_schema.columns WHERE table_name = '" + tableName + "' ORDER BY column_name;";
            ResultSet res = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY).executeQuery(query);
            res.last();
            List<String> categoryNames = new ArrayList<>();
            res.beforeFirst();
            while (res.next()) {
                String colName = res.getString("column_name");
                if (colName.startsWith("value_")) {
                    categoryNames.add(colName);
                }
            }
            return categoryNames;
        } catch (SQLException ex) {
            logger.logError("Не удалось получить список столбцов по запросу: " + query);
            handleSQLException(ex);
        }
        return new ArrayList<>();
    }

    /**
     * Получает список названий таблиц с исходными данными.
     *
     * @return список столбцов в строковом виде
     */
    public List<String> getTableNames() {
        if(connection == null) {
            return new ArrayList<>();
        }
        String query = "";
        try {
            query = "SELECT table_name FROM information_schema.tables WHERE table_type = 'BASE TABLE' " +
                    "AND table_schema NOT IN ('pg_catalog', 'information_schema') ORDER BY table_name;";
            ResultSet res = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY).executeQuery(query);
            res.last();
            List<String> tableNames = new ArrayList<>();
            res.beforeFirst();
            while (res.next()) {
                String tableName = res.getString("table_name");
                if (!tableName.endsWith("_decreases") && !tableName.endsWith("_constants") && !tableName.endsWith("_labels")) {
                    tableNames.add(tableName);
                }
            }
            return tableNames;
        } catch (SQLException ex) {
            logger.logError("Не удалось получить список столбцов по запросу: " + query);
            handleSQLException(ex);
        }
        return new ArrayList<>();
    }

    /**
     * Вставляет в таблицу интервалов с уменьшениями новые строки с указанными значениями данных.
     *
     * @param tableName - название таблицы
     * @param colNames  - названия столбцов таблицы
     * @param intervals - вставляемые интервалы
     */
    public void insertDecrease(String tableName, String[] colNames, List<SuspiciousInterval> intervals, Date minDate, Date maxDate) {
        if(connection == null) {
            return;
        }
        StringBuilder query = new StringBuilder();
        try {
            query.append("INSERT INTO ").append(tableName).append("(");
            for (int i = 0; i < colNames.length; i++) {
                query.append(colNames[i]);
                if (i < colNames.length - 1) {
                    query.append(", ");
                }
            }
            query.append(", pos1, pos2, min_date, max_date, decrease_score, relative_width, relative_diff, value_name");
            query.append(") VALUES (");
            for (int k = 0; k < intervals.size(); k++) {
                SuspiciousInterval interval = intervals.get(k);
                Slice slice = interval.slice;
                String[] labels = new String[colNames.length];
                for (int i = 0; i < colNames.length; i++) {
                    boolean columnIsPresent = false;
                    for (int j = 0; j < slice.colNames.length; j++) {
                        if (colNames[i].equals(slice.colNames[j])) {
                            columnIsPresent = true;
                            labels[i] = slice.labels[j];
                        }
                    }
                    if (!columnIsPresent) {
                        labels[i] = labelNotPresent;
                    }
                }
                for (int i = 0; i < labels.length; i++) {
                    if (labels[i].startsWith("'")) {
                        query.append(labels[i]);
                    } else {
                        query.append("'").append(labels[i]).append("'");
                    }
                    if (i < labels.length - 1) {
                        query.append(", ");
                    }
                }
                query.append(", ").append(interval.pos1);
                query.append(", ").append(interval.pos2);
                query.append(", '").append(minDate).append("'");
                query.append(", '").append(maxDate).append("'");
                query.append(", ").append(interval.getDecreaseScore());
                query.append(", ").append(interval.getRelativeWidth());
                query.append(", ").append(interval.getRelativeDiff() / interval.slice.getRelativeSigma());
                query.append(", ").append("'").append(interval.slice.valueName).append("'");
                if (k < intervals.size() - 1) {
                    query.append("),(");
                }
            }
            query.append(");");
            connection.createStatement().executeUpdate(query.toString());
        } catch (SQLException ex) {
            logger.logError("Не удалось вставить интервалы с уменьшением по запросу: " + query);
            handleSQLException(ex);
        }
    }

    /**
     * Получает из таблицы интервалов с уменьшениями список интервалов, сгруппированных по определенным столбцам и
     * отвечающих определенным требованиям.
     *
     * @param tableName         - название таблицы
     * @param valueName         - название ряда данных
     * @param categoryCombos    - сочетания названий столбцов таблицы
     * @param approximationType - тип приближения срезов
     * @param minIntervalMult   - минимальная длина интервалов, которые будут рассматриваться (измеряется как доля длины
     *                          временного промежутка всего разреза, от 0 до 1)
     * @param thresholdMult     - минимальная разность между первой и последней величиной для интервалов, которые будут
     *                          рассматриваться (измеряется как доля разности между максимальным и минимальным значением
     *                          на всем разрезе, от 0 до 1)
     * @return список интервалов
     */
    public List<SuspiciousInterval> getDecreases(String tableName, String valueName, List<String[]> categoryCombos,
                                                 ApproximationType approximationType, double minIntervalMult,
                                                 double thresholdMult, int maxIntervals) {
        if(connection == null) {
            return new ArrayList<>();
        }
        StringBuilder query = new StringBuilder();
        try {
            String tableDecName = tableName + "_decreases";
            query.append("SELECT * FROM ").append(tableDecName).append(" WHERE (");
            List<String> categoryNames = getCategoryNames(tableDecName);
            appendCategories(categoryCombos, query, categoryNames);
            query.append(") AND relative_width > ").append(minIntervalMult);
            query.append(" AND -relative_diff > ").append(thresholdMult);
            query.append(" AND value_name = '").append(valueName).append("'");
            query.append(" LIMIT 1024;");
            ResultSet res = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY).executeQuery(query.toString());
            res.last();
            List<SuspiciousInterval> intervals = new ArrayList<>();
            res.beforeFirst();
            List<Slice> slices = new ArrayList<>();
            while (res.next() && intervals.size() <= maxIntervals * categoryNames.size()) {
                int pos1 = res.getInt("pos1");
                int pos2 = res.getInt("pos2");
                Date minDate = res.getTimestamp("min_date");
                Date maxDate = res.getTimestamp("max_date");
                if (!intervalIntersects(intervals, categoryNames, res, pos1, pos2)) {
                    Slice slice = matchSlice(tableName, valueName, approximationType, categoryNames, res, slices, minDate, maxDate);
                    intervals.add(new SuspiciousInterval(slice, pos1, pos2, 0.2));
                }
            }
            return intervals;
        } catch (SQLException ex) {
            logger.logError("Не удалось получить интервалы с уменьшением по запросу: " + query);
            handleSQLException(ex);
        }
        return new ArrayList<>();
    }

    /**
     * Получает из таблицы интервалов с уменьшениями список интервалов, сгруппированных по определенным столбцам и
     * отвечающих определенным требованиям. Работает при выборе интервалов из одного среза.
     *
     * @param tableName         - название таблицы
     * @param valueName         - название ряда данных
     * @param colNames          - названия столбцов таблицы
     * @param labels            - значения столбцов таблицы
     * @param approximationType - тип приближения срезов
     * @param minIntervalMult   - минимальная длина интервалов, которые будут рассматриваться (измеряется как доля длины
     *                          временного промежутка всего разреза, от 0 до 1)
     * @param thresholdMult     - минимальная разность между первой и последней величиной для интервалов, которые будут
     *                          рассматриваться (измеряется как доля разности между максимальным и минимальным значением
     *                          на всем разрезе, от 0 до 1)
     * @return список интервалов
     */
    public List<SuspiciousInterval> getDecreasesSimple(String tableName, String valueName, String[] colNames, String[] labels,
                                                       ApproximationType approximationType, double minIntervalMult,
                                                       double thresholdMult, int maxIntervals) {
        if(connection == null) {
            return new ArrayList<>();
        }
        StringBuilder query = new StringBuilder();
        try {
            String tableDecName = tableName + "_decreases";
            query.append("SELECT * FROM ").append(tableDecName).append(" WHERE (");
            List<String> categoryNames = getCategoryNames(tableDecName);
            appendCategoriesSimple(colNames, labels, query, categoryNames);
            query.append(") AND relative_width > ").append(minIntervalMult);
            query.append(" AND -relative_diff > ").append(thresholdMult);
            query.append(" AND value_name = '").append(valueName).append("'");
            query.append(" LIMIT 1024;");
            ResultSet res = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY).executeQuery(query.toString());
            res.last();
            List<SuspiciousInterval> intervals = new ArrayList<>();
            res.beforeFirst();
            List<Slice> slices = new ArrayList<>();
            while (res.next() && intervals.size() <= maxIntervals * categoryNames.size()) {
                int pos1 = res.getInt("pos1");
                int pos2 = res.getInt("pos2");
                Date minDate = res.getTimestamp("min_date");
                Date maxDate = res.getTimestamp("max_date");
                if (!intervalIntersects(intervals, categoryNames, res, pos1, pos2)) {
                    Slice slice = matchSlice(tableName, valueName, approximationType, categoryNames, res, slices, minDate, maxDate);
                    intervals.add(new SuspiciousInterval(slice, pos1, pos2, 0.2));
                }
            }
            return intervals;
        } catch (SQLException ex) {
            logger.logError("Не удалось получить интервалы с уменьшением по запросу: " + query);
            handleSQLException(ex);
        }
        return new ArrayList<>();
    }


    /**
     * Вставляет в таблицу интервалов с отсутствием изменений новые строки с указанными значениями данных.
     *
     * @param tableName - название таблицы
     * @param colNames  - названия столбцов таблицы
     * @param intervals - вставляемые интервалы
     */
    public void insertConstant(String tableName, String[] colNames, List<SuspiciousInterval> intervals, Date minDate, Date maxDate) {
        if(connection == null) {
            return;
        }
        StringBuilder query = new StringBuilder();
        try {
            query.append("INSERT INTO ").append(tableName).append("(");
            for (int i = 0; i < colNames.length; i++) {
                query.append(colNames[i]);
                if (i < colNames.length - 1) {
                    query.append(", ");
                }
            }
            query.append(", pos1, pos2, min_date, max_date, flatness_score, relative_width, relative_value_range, value_name");
            query.append(") VALUES (");
            for (int k = 0; k < intervals.size(); k++) {
                SuspiciousInterval interval = intervals.get(k);
                Slice slice = interval.slice;
                String[] labels = new String[colNames.length];
                for (int i = 0; i < colNames.length; i++) {
                    boolean columnIsPresent = false;
                    for (int j = 0; j < slice.colNames.length; j++) {
                        if (colNames[i].equals(slice.colNames[j])) {
                            columnIsPresent = true;
                            labels[i] = slice.labels[j];
                        }
                    }
                    if (!columnIsPresent) {
                        labels[i] = labelNotPresent;
                    }
                }
                for (int i = 0; i < labels.length; i++) {
                    if (labels[i].startsWith("'")) {
                        query.append(labels[i]);
                    } else {
                        query.append("'").append(labels[i]).append("'");
                    }
                    if (i < labels.length - 1) {
                        query.append(", ");
                    }
                }
                query.append(", ").append(interval.pos1);
                query.append(", ").append(interval.pos2);
                query.append(", '").append(minDate).append("'");
                query.append(", '").append(maxDate).append("'");
                query.append(", ").append(interval.getFlatnessScore());
                query.append(", ").append(interval.getRelativeWidth());
                query.append(", ").append(interval.getRelativeValueRange() / interval.slice.getRelativeSigma());
                query.append(", ").append("'").append(interval.slice.valueName).append("'");
                if (k < intervals.size() - 1) {
                    query.append("),(");
                }
            }
            query.append(");");
            connection.createStatement().executeUpdate(query.toString());
        } catch (SQLException ex) {
            logger.logError("Не удалось вставить интервалы с отсутствием роста по запросу: " + query);
            handleSQLException(ex);
        }
    }

    /**
     * Получает из таблицы интервалов с отсутствием изменений список интервалов, сгруппированных по определенным столбцам и
     * отвечающих определенным требованиям.
     *
     * @param tableName         - название таблицы
     * @param valueName         - название ряда данных
     * @param categoryCombos    - сочетания названий столбцов таблицы
     * @param approximationType - тип приближения срезов
     * @param minIntervalMult   - минимальная длина интервалов, которые будут рассматриваться (измеряется как доля длины
     *                          временного промежутка всего разреза, от 0 до 1)
     * @param thresholdMult     - максимальная разность между максимальной и минимальной величиной для интервалов, которые будут
     *                          рассматриваться (измеряется как доля разности между максимальным и минимальным значением
     *                          на всем разрезе, от 0 до 1)
     * @return список интервалов
     */
    public List<SuspiciousInterval> getConstants(String tableName, String valueName, List<String[]> categoryCombos,
                                                 ApproximationType approximationType, double minIntervalMult,
                                                 double thresholdMult, int maxIntervals) {
        if(connection == null) {
            return new ArrayList<>();
        }
        StringBuilder query = new StringBuilder();
        try {
            String tableDecName = tableName + "_constants";
            query.append("SELECT * FROM ").append(tableDecName).append(" WHERE (");
            List<String> categoryNames = getCategoryNames(tableDecName);
            appendCategories(categoryCombos, query, categoryNames);
            query.append(") AND relative_width > ").append(minIntervalMult);
            query.append(" AND relative_value_range < ").append(thresholdMult);
            query.append(" AND value_name = '").append(valueName).append("'");
            query.append(" LIMIT 1024;");
            ResultSet res = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY).executeQuery(query.toString());
            res.last();
            List<SuspiciousInterval> intervals = new ArrayList<>();
            res.beforeFirst();
            List<Slice> slices = new ArrayList<>();
            while (res.next() && intervals.size() <= maxIntervals * categoryNames.size()) {
                int pos1 = res.getInt("pos1");
                int pos2 = res.getInt("pos2");
                Date minDate = res.getTimestamp("min_date");
                Date maxDate = res.getTimestamp("max_date");
                if (!intervalIntersects(intervals, categoryNames, res, pos1, pos2)) {
                    Slice slice = matchSlice(tableName, valueName, approximationType, categoryNames, res, slices, minDate, maxDate);
                    intervals.add(new SuspiciousInterval(slice, pos1, pos2, 0.2));
                }
            }
            return intervals;
        } catch (SQLException ex) {
            logger.logError("Не удалось получить интервалы с отсутствием роста по запросу: " + query);
            handleSQLException(ex);
        }
        return new ArrayList<>();
    }

    /**
     * Получает из таблицы интервалов с отсутствием изменений список интервалов, сгруппированных по определенным столбцам и
     * отвечающих определенным требованиям. Работает при выборе интервалов из одного среза.
     *
     * @param tableName         - название таблицы
     * @param valueName         - название ряда данных
     * @param colNames          - названия столбцов таблицы
     * @param labels            - значения столбцов таблицы
     * @param approximationType - тип приближения срезов
     * @param minIntervalMult   - минимальная длина интервалов, которые будут рассматриваться (измеряется как доля длины
     *                          временного промежутка всего разреза, от 0 до 1)
     * @param thresholdMult     - минимальная разность между первой и последней величиной для интервалов, которые будут
     *                          рассматриваться (измеряется как доля разности между максимальным и минимальным значением
     *                          на всем разрезе, от 0 до 1)
     * @return список интервалов
     */
    public List<SuspiciousInterval> getConstantsSimple(String tableName, String valueName, String[] colNames, String[] labels,
                                                       ApproximationType approximationType, double minIntervalMult,
                                                       double thresholdMult, int maxIntervals) {
        if(connection == null) {
            return new ArrayList<>();
        }
        StringBuilder query = new StringBuilder();
        try {
            String tableDecName = tableName + "_constants";
            query.append("SELECT * FROM ").append(tableDecName).append(" WHERE (");
            List<String> categoryNames = getCategoryNames(tableDecName);
            appendCategoriesSimple(colNames, labels, query, categoryNames);
            query.append(") AND relative_width > ").append(minIntervalMult);
            query.append(" AND relative_value_range < ").append(thresholdMult);
            query.append(" AND value_name = '").append(valueName).append("'");
            query.append(" LIMIT 1024;");
            ResultSet res = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY).executeQuery(query.toString());
            res.last();
            List<SuspiciousInterval> intervals = new ArrayList<>();
            res.beforeFirst();
            List<Slice> slices = new ArrayList<>();
            while (res.next() && intervals.size() <= maxIntervals * categoryNames.size()) {
                int pos1 = res.getInt("pos1");
                int pos2 = res.getInt("pos2");
                Date minDate = res.getTimestamp("min_date");
                Date maxDate = res.getTimestamp("max_date");
                if (!intervalIntersects(intervals, categoryNames, res, pos1, pos2)) {
                    Slice slice = matchSlice(tableName, valueName, approximationType, categoryNames, res, slices, minDate, maxDate);
                    intervals.add(new SuspiciousInterval(slice, pos1, pos2, 0.2));
                }
            }
            return intervals;
        } catch (SQLException ex) {
            logger.logError("Не удалось получить интервалы с отсутствием роста по запросу: " + query);
            handleSQLException(ex);
        }
        return new ArrayList<>();
    }

    /**
     * Закрывает соединение с базой данных.
     */
    public void closeConnection() {
        if(connection == null) {
            return;
        }
        try {
            connection.close();
        } catch (SQLException ex) {
            handleSQLException(ex);
        }
    }

    private void handleSQLException(SQLException ex) {
        ex.printStackTrace();
    }

    /**
     * Добавляет к запросу получения интервалов список комбинаций категорий в виде логического выражения.
     *
     * @param categoryCombos - сочетания названий столбцов таблицы
     * @param query          - запрос
     * @param categoryNames  - названия столбцов
     */
    private void appendCategories(List<String[]> categoryCombos, StringBuilder query, List<String> categoryNames) {
        for (int k = 0; k < categoryCombos.size(); k++) {
            String[] colNames = categoryCombos.get(k);
            for (int i = 0; i < categoryNames.size(); i++) {
                boolean categoryIsPresent = false;
                for (String secondCategory : colNames) {
                    if (categoryNames.get(i).equals(secondCategory)) {
                        categoryIsPresent = true;
                    }
                }
                if (categoryIsPresent) {
                    query.append(categoryNames.get(i)).append(" != ").append("'").append(labelNotPresent).append("'");
                } else {
                    query.append(categoryNames.get(i)).append(" = ").append("'").append(labelNotPresent).append("'");
                }
                if (i < categoryNames.size() - 1) {
                    query.append(" AND ");
                }
            }
            if (k < categoryCombos.size() - 1) {
                query.append(" OR ");
            }
        }
    }

    /**
     * Добавляет к запросу получения интервалов список значений категорий в виде логического выражения. Работает при
     * выборе интервалов из одного среза.
     *
     * @param colNames      - названия столбцов таблицы
     * @param labels        - значения столбцов таблицы
     * @param query         - запрос
     * @param categoryNames - названия столбцов
     */
    private void appendCategoriesSimple(String[] colNames, String[] labels, StringBuilder query, List<String> categoryNames) {
        for (int k = 0; k < categoryNames.size(); k++) {
            boolean categoryPresent = false;
            for (int i = 0; i < colNames.length; i++) {
                if (colNames[i].equals(categoryNames.get(k))) {
                    query.append(colNames[i]).append(" = ").append(labels[i]);
                    categoryPresent = true;
                }
            }
            if (!categoryPresent) {
                query.append(categoryNames.get(k)).append(" = '").append(labelNotPresent).append("'");
            }
            if (k < categoryNames.size() - 1) {
                query.append(" AND ");
            }
        }
    }

    /**
     * Находит в списке или получает из базы данных срез, соответствующий строке из таблицы интервалов (совпадающий с ней
     * по значениям определенных категорий).
     *
     * @param tableName         - название таблицы с данными
     * @param approximationType - тип приближения срезов
     * @param categoryNames     - названия столбцов
     * @param res               - ответ на запрос к базе данных, содержащий список интервалов
     * @param slices            - раннее найденные столбцы
     * @return новый или найденный срез
     */
    private Slice matchSlice(String tableName, String valueName, ApproximationType approximationType,
                             List<String> categoryNames, ResultSet res, List<Slice> slices, Date minDate, Date maxDate) throws SQLException {
        List<String> labelsList = new ArrayList<>();
        List<String> colNamesList = new ArrayList<>();
        for (String categoryName : categoryNames) {
            if (!res.getString(categoryName).equals(labelNotPresent)) {
                labelsList.add("'" + res.getString(categoryName) + "'");
                colNamesList.add(categoryName);
            }
        }
        String[] labels = labelsList.toArray(new String[0]);
        String[] colNames = colNamesList.toArray(new String[0]);
        Slice slice = null;
        for (Slice oldSlice : slices) {
            boolean sliceIsTheSame = true;
            if (oldSlice.labels.length != labels.length) {
                sliceIsTheSame = false;
            } else {
                for (int i = 0; i < labels.length; i++) {
                    if (!oldSlice.labels[i].equals(labels[i])) {
                        sliceIsTheSame = false;
                    }
                }
            }
            if (sliceIsTheSame) {
                slice = oldSlice;
            }
        }
        if (slice == null) {
            slice = getSlice(tableName, valueName, colNames, labels, approximationType, minDate, maxDate).getAccumulation();
            slices.add(slice);
        }
        return slice;
    }

    private boolean intervalIntersects(List<SuspiciousInterval> intervals, List<String> categoryNames, ResultSet res,
                                       int pos1, int pos2) throws SQLException {
        List<String> labelsList = new ArrayList<>();
        for (String categoryName : categoryNames) {
            if (!res.getString(categoryName).equals(labelNotPresent)) {
                labelsList.add("'" + res.getString(categoryName) + "'");
            }
        }
        for (SuspiciousInterval interval : intervals) {
            if (interval.slice.labels.length != labelsList.size()) {
                continue;
            } else {
                boolean intervalIntersects = true;
                for (int i = 0; i < labelsList.size(); i++) {
                    if (!interval.slice.labels[i].equals(labelsList.get(i))) {
                        intervalIntersects = false;
                    }
                }
                if (!intervalIntersects) {
                    continue;
                }
            }
            if ((pos1 > interval.pos1 && pos1 < interval.pos2) || (pos2 > interval.pos1 && pos2 < interval.pos2) ||
                    (interval.pos1 > pos1 && interval.pos1 < pos2) || (interval.pos2 > pos1 && interval.pos2 < pos2) ||
                    (interval.pos1 == pos1 && interval.pos2 == pos2)) {
                return true;
            }
        }
        return false;
    }

}

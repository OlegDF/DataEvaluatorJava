package com.Model;
import com.DataObjects.Slice;
import com.DataObjects.SlicePoint;
import com.DataObjects.SuspiciousInterval;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Сервис, который управляет запросами к базе данных Postgres.
 */
public class DatabaseService {

    public final String labelNotPresent = "NOT_APPLICABLE";

    private Connection connection = null;

    /**
     * Конструктор, устанавливающий соединение с базой данных с указанным названием, именем пользователя и паролем.
     *
     * @param db - название базы данных
     * @param user - имя пользователя
     * @param password - пароль
     */
    public DatabaseService(String db, String user, String password) {
        openConnection(db, user, password);
    }

    private void openConnection(String db, String user, String password) {
        final String url = "jdbc:postgresql://localhost/" + db + "?user=" + user + "&password=" + password;
        try {
            connection = DriverManager.getConnection(url);
        } catch (SQLException ex) {
            handleSQLException(ex);
        }
    }

    /**
     * Создает таблицу с указанным названием, в которой будут храниться результаты работы. Также удаляет существующую
     * таблицу с таким же названием, если она существует.
     *
     * @param tableName - название таблицы
     * @param colNames - названия столбцов таблицы
     * @param colTypes - типы данных в соответствующих столбцах
     */
    public void createTable(String tableName, String[] colNames, String[] colTypes) {
        try {
            connection.createStatement().executeUpdate("DROP TABLE IF EXISTS " + tableName + ";");
            StringBuilder query = new StringBuilder("CREATE TABLE " + tableName + " (");
            for(int i = 0; i < colNames.length; i++) {
                query.append(colNames[i]).append(" ").append(colTypes[i]);
                if(i < colNames.length - 1) {
                    query.append(", ");
                }
            }
            query.append(");");
            connection.createStatement().executeUpdate(query.toString());
        } catch (SQLException ex) {
            handleSQLException(ex);
        }
    }

    /**
     * Вставляет в таблицу новую строку с указанными значениями данных.
     *
     * @param tableName - название таблицы
     * @param colNames - названия столбцов таблицы
     * @param colTypes - типы данных в соответствующих столбцах
     * @param row - значения в новой строке в строковом виде
     */
    public void insertData(String tableName, String[] colNames, String[] colTypes, String[] row) {
        try {
            StringBuilder query = new StringBuilder("INSERT INTO " + tableName + "(");
            for(int i = 0; i < colNames.length; i++) {
                query.append(colNames[i]);
                if(i < colNames.length - 1) {
                    query.append(", ");
                }
            }
            query.append(") VALUES (");
            for(int i = 0; i < row.length; i++) {
                if(colTypes[i].equals("varchar(255)") || colTypes[i].equals("timestamptz")) {
                    query.append("'").append(row[i]).append("'");
                } else {
                    query.append(row[i]);
                }
                if(i < row.length - 1) {
                    query.append(", ");
                }
            }
            query.append(");");
            connection.createStatement().executeUpdate(query.toString());
        } catch (SQLException ex) {
            handleSQLException(ex);
        }
    }

    /**
     * Получает разрез - набор данных, в которых один или более столбцов равны заданным значениям.
     *
     * @param tableName - название таблицы
     * @param colNames - названия столбцов, по которым отбираются данные
     * @param labels - значения в соответствующих столбцах в строковом виде
     * @return объект-разрез
     */
    public Slice getSlice(String tableName, String[] colNames, String[] labels) {
        try {
            StringBuilder query = new StringBuilder("SELECT * FROM " + tableName + " WHERE ");
            for(int i = 0; i < colNames.length; i++) {
                query.append(colNames[i]).append("=");
                query.append(labels[i]);
                if(i < colNames.length - 1) {
                    query.append(" AND ");
                }
            }
            query.append(" ORDER BY first_date;");
            ResultSet res = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY).executeQuery(query.toString());
            res.last();
            int length = res.getRow();
            SlicePoint[] points = new SlicePoint[length];
            res.beforeFirst();
            int i = 0;
            while(res.next()) {
                points[i] = new SlicePoint(res.getLong("value_1"), res.getLong("amount"), res.getTimestamp("first_date"));
                i++;
            }
            return new Slice(tableName, colNames, labels, points);
        } catch (SQLException ex) {
            handleSQLException(ex);
        }
        return new Slice(tableName, colNames, labels);
    }

    /**
     * Получает список уникальных значений, которые принимают данные в указанном столбце.
     *
     * @param tableName - название таблицы
     * @param colName - название столбца
     * @return список значений в строковом виде
     */
    public String[] getUniqueLabels(String tableName, String colName) {
        try {
            StringBuilder query = new StringBuilder("SELECT DISTINCT " + colName + " FROM " + tableName + ";");
            ResultSet res = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY).executeQuery(query.toString());
            res.last();
            int length = res.getRow();
            String[] labels = new String[length];
            res.beforeFirst();
            int i = 0;
            while(res.next()) {
                labels[i] = res.getString(colName);
                i++;
            }
            return labels;
        } catch (SQLException ex) {
            handleSQLException(ex);
        }
        return new String[0];
    }

    /**
     * Получает список названий столбцов с категориями в определенной таблице.
     *
     * @param tableName - название таблицы
     * @return список столбцов в строковом виде
     */
    public List<String> getCategoryNames(String tableName) {
        try {
            String query = "SELECT column_name FROM information_schema.columns WHERE table_name = '" + tableName + "';";
            ResultSet res = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY).executeQuery(query);
            res.last();
            List<String> categoryNames = new ArrayList<>();
            res.beforeFirst();
            while(res.next()) {
                String colName = res.getString("column_name");
                if(colName.startsWith("category_")) {
                    categoryNames.add(colName);
                }
            }
            return categoryNames;
        } catch (SQLException ex) {
            handleSQLException(ex);
        }
        return new ArrayList<>();
    }

    /**
     * Вставляет в таблицу интервалов с уменьшениями новые строки с указанными значениями данных.
     *
     * @param tableName - название таблицы
     * @param colNames - названия столбцов таблицы
     * @param intervals - вставляемые интервалы
     */
    public void insertDecrease(String tableName, String[] colNames, List<SuspiciousInterval> intervals) {
        try {
            StringBuilder query = new StringBuilder("INSERT INTO " + tableName + "(");
            for(int i = 0; i < colNames.length; i++) {
                query.append(colNames[i]);
                if(i < colNames.length - 1) {
                    query.append(", ");
                }
            }
            query.append(", pos1, pos2, decrease_score, relative_width, relative_diff");
            query.append(") VALUES (");
            for(int k = 0; k < intervals.size(); k++) {
                SuspiciousInterval interval = intervals.get(k);
                Slice slice = interval.slice;
                String[] labels = new String[colNames.length];
                for(int i = 0; i < colNames.length; i++) {
                    boolean columnIsPresent = false;
                    for(int j = 0; j < slice.colNames.length; j++) {
                        if(colNames[i].equals(slice.colNames[j])) {
                            columnIsPresent = true;
                            labels[i] = slice.labels[j];
                        }
                    }
                    if(!columnIsPresent) {
                        labels[i] = labelNotPresent;
                    }
                }
                for(int i = 0; i < labels.length; i++) {
                    if(labels[i].startsWith("'")) {
                        query.append(labels[i]);
                    } else {
                        query.append("'").append(labels[i]).append("'");
                    }
                    if(i < labels.length - 1) {
                        query.append(", ");
                    }
                }
                query.append(", ").append(interval.pos1);
                query.append(", ").append(interval.pos2);
                query.append(", ").append(interval.getDecreaseScore());
                query.append(", ").append(interval.getRelativeWidth());
                query.append(", ").append(interval.getRelativeDiff());
                if(k < intervals.size() - 1) {
                    query.append("),(");
                }
            }
            query.append(");");
            connection.createStatement().executeUpdate(query.toString());
        } catch (SQLException ex) {
            handleSQLException(ex);
        }
    }

    /**
     * Получает из таблицы интервалов с уменьшениями список интервалов, сгруппированных по определенным столбцам и
     * отвечающих определенным требованиям.
     *
     * @param tableName - название таблицы
     * @param colNames - названия столбцов таблицы
     * @param minIntervalMult - минимальная длина интервалов, которые будут рассматриваться (измеряется как доля длины
     *                        временного промежутка всего разреза, от 0 до 1)
     * @param thresholdMult - минимальная разность между первой и последней величиной для интервалов, которые будут
     *                        рассматриваться (измеряется как доля разности между максимальным и минимальным значением
     *                        на всем разрезе, от 0 до 1)
     * @return список интервалов
     */
    public List<SuspiciousInterval> getDecreases(String tableName, String[] colNames, double minIntervalMult, double thresholdMult) {
        try {
            String tableDecName = tableName + "_decreases";
            StringBuilder query = new StringBuilder("SELECT * FROM ").append(tableDecName).append(" WHERE ");
            List<String> categoryNames = getCategoryNames(tableDecName);
            for(int i = 0; i < categoryNames.size(); i++) {
                boolean categoryIsPresent = false;
                for(String secondCategory: colNames) {
                    if(categoryNames.get(i).equals(secondCategory)) {
                        categoryIsPresent = true;
                    }
                }
                if(categoryIsPresent) {
                    query.append(categoryNames.get(i)).append(" != ").append("'").append(labelNotPresent).append("'");
                } else {
                    query.append(categoryNames.get(i)).append(" = ").append("'").append(labelNotPresent).append("'");
                }
                if(i < categoryNames.size() - 1) {
                    query.append(" AND ");
                }
            }
            query.append(" AND relative_width > ").append(minIntervalMult);
            query.append(" AND -relative_diff > ").append(thresholdMult);
            query.append(";");
            ResultSet res = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY).executeQuery(query.toString());
            res.last();
            List<SuspiciousInterval> intervals = new ArrayList<>();
            res.beforeFirst();
            List<Slice> slices = new ArrayList<>();
            while(res.next()) {
                String[] labels = new String[colNames.length];
                for(int i = 0; i < colNames.length; i++) {
                    labels[i] = "'" + res.getString(colNames[i]) + "'";
                }
                Slice slice = null;
                for(Slice oldSlice: slices) {
                    boolean sliceIsTheSame = true;
                    for(int i = 0; i < labels.length; i++) {
                        if(!oldSlice.labels[i].equals(labels[i])) {
                            sliceIsTheSame = false;
                        }
                    }
                    if(sliceIsTheSame) {
                        slice = oldSlice;
                    }
                }
                if(slice == null) {
                    slice = getSlice(tableName, colNames, labels).getAccumulation();
                    slices.add(slice);
                }
                intervals.add(new SuspiciousInterval(slice, res.getInt("pos1"), res.getInt("pos2")));
            }
            return intervals;
        } catch (SQLException ex) {
            handleSQLException(ex);
        }
        return new ArrayList<>();
    }



    /**
     * Вставляет в таблицу интервалов с отсутствием изменений новые строки с указанными значениями данных.
     *
     * @param tableName - название таблицы
     * @param colNames - названия столбцов таблицы
     * @param intervals - вставляемые интервалы
     */
    public void insertConstant(String tableName, String[] colNames, List<SuspiciousInterval> intervals) {
        try {
            StringBuilder query = new StringBuilder("INSERT INTO " + tableName + "(");
            for(int i = 0; i < colNames.length; i++) {
                query.append(colNames[i]);
                if(i < colNames.length - 1) {
                    query.append(", ");
                }
            }
            query.append(", pos1, pos2, relative_width, relative_value_range");
            query.append(") VALUES (");
            for(int k = 0; k < intervals.size(); k++) {
                SuspiciousInterval interval = intervals.get(k);
                Slice slice = interval.slice;
                String[] labels = new String[colNames.length];
                for(int i = 0; i < colNames.length; i++) {
                    boolean columnIsPresent = false;
                    for(int j = 0; j < slice.colNames.length; j++) {
                        if(colNames[i].equals(slice.colNames[j])) {
                            columnIsPresent = true;
                            labels[i] = slice.labels[j];
                        }
                    }
                    if(!columnIsPresent) {
                        labels[i] = labelNotPresent;
                    }
                }
                for(int i = 0; i < labels.length; i++) {
                    if(labels[i].startsWith("'")) {
                        query.append(labels[i]);
                    } else {
                        query.append("'").append(labels[i]).append("'");
                    }
                    if(i < labels.length - 1) {
                        query.append(", ");
                    }
                }
                query.append(", ").append(interval.pos1);
                query.append(", ").append(interval.pos2);
                query.append(", ").append(interval.getRelativeWidth());
                query.append(", ").append(interval.getRelativeValueRange());
                if(k < intervals.size() - 1) {
                    query.append("),(");
                }
            }
            query.append(");");
            connection.createStatement().executeUpdate(query.toString());
        } catch (SQLException ex) {
            handleSQLException(ex);
        }
    }

    /**
     * Получает из таблицы интервалов с отсутствием изменений список интервалов, сгруппированных по определенным столбцам и
     * отвечающих определенным требованиям.
     *
     * @param tableName - название таблицы
     * @param colNames - названия столбцов таблицы
     * @param minIntervalMult - минимальная длина интервалов, которые будут рассматриваться (измеряется как доля длины
     *                        временного промежутка всего разреза, от 0 до 1)
     * @param thresholdMult - максимальная разность между максимальной и минимальной величиной для интервалов, которые будут
     *                        рассматриваться (измеряется как доля разности между максимальным и минимальным значением
     *                        на всем разрезе, от 0 до 1)
     * @return список интервалов
     */
    public List<SuspiciousInterval> getConstants(String tableName, String[] colNames, double minIntervalMult, double thresholdMult) {
        try {
            String tableDecName = tableName + "_constants";
            StringBuilder query = new StringBuilder("SELECT * FROM ").append(tableDecName).append(" WHERE ");
            List<String> categoryNames = getCategoryNames(tableDecName);
            for(int i = 0; i < categoryNames.size(); i++) {
                boolean categoryIsPresent = false;
                for(String secondCategory: colNames) {
                    if(categoryNames.get(i).equals(secondCategory)) {
                        categoryIsPresent = true;
                    }
                }
                if(categoryIsPresent) {
                    query.append(categoryNames.get(i)).append(" != ").append("'").append(labelNotPresent).append("'");
                } else {
                    query.append(categoryNames.get(i)).append(" = ").append("'").append(labelNotPresent).append("'");
                }
                if(i < categoryNames.size() - 1) {
                    query.append(" AND ");
                }
            }
            query.append(" AND relative_width > ").append(minIntervalMult);
            query.append(" AND relative_value_range < ").append(thresholdMult);
            query.append(";");
            ResultSet res = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY).executeQuery(query.toString());
            res.last();
            List<SuspiciousInterval> intervals = new ArrayList<>();
            res.beforeFirst();
            List<Slice> slices = new ArrayList<>();
            while(res.next()) {
                String[] labels = new String[colNames.length];
                for(int i = 0; i < colNames.length; i++) {
                    labels[i] = "'" + res.getString(colNames[i]) + "'";
                }
                Slice slice = null;
                for(Slice oldSlice: slices) {
                    boolean sliceIsTheSame = true;
                    for(int i = 0; i < labels.length; i++) {
                        if(!oldSlice.labels[i].equals(labels[i])) {
                            sliceIsTheSame = false;
                        }
                    }
                    if(sliceIsTheSame) {
                        slice = oldSlice;
                    }
                }
                if(slice == null) {
                    slice = getSlice(tableName, colNames, labels).getAccumulation();
                    slices.add(slice);
                }
                intervals.add(new SuspiciousInterval(slice, res.getInt("pos1"), res.getInt("pos2")));
            }
            return intervals;
        } catch (SQLException ex) {
            handleSQLException(ex);
        }
        return new ArrayList<>();
    }

    /**
     * Закрывает соединение с базой данных.
     */
    public void closeConnection() {
        try {
            connection.close();
        } catch (SQLException ex) {
            handleSQLException(ex);
        }
    }

    private void handleSQLException(SQLException ex) {
        ex.printStackTrace();
    }

}

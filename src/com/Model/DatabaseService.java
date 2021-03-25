package com.Model;
import com.DataObjects.Slice;
import com.DataObjects.SlicePoint;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DatabaseService {

    private Connection connection = null;
    private final String db;

    public DatabaseService(String db, String user, String password) {
        openConnection(db, user, password);
        this.db = db;
    }

    private void openConnection(String db, String user, String password) {
        final String url = "jdbc:postgresql://localhost/" + db + "?user=" + user + "&password=" + password;
        try {
            connection = DriverManager.getConnection(url);
        } catch (SQLException ex) {
            handleSQLException(ex);
        }
    }

    public void createTable(String tableName, String[] colNames, String[] colTypes) {
        try {
            connection.createStatement().executeQuery("DROP TABLE IF EXISTS " + tableName + ";");
            StringBuilder query = new StringBuilder("CREATE TABLE " + tableName + " (");
            for(int i = 0; i < colNames.length; i++) {
                query.append(colNames[i]).append(" ").append(colTypes[i]);
                if(i < colNames.length - 1) {
                    query.append(", ");
                }
            }
            query.append(");");
            connection.createStatement().executeQuery(query.toString());
        } catch (SQLException ex) {
            handleSQLException(ex);
        }
    }

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
                if(colTypes[i].equals("string") || colTypes[i].equals("timestamptz")) {
                    query.append("'").append(row[i]).append("'");
                } else {
                    query.append(row[i]);
                }
                if(i < row.length - 1) {
                    query.append(", ");
                }
            }
            query.append(");");
            connection.createStatement().executeQuery(query.toString());
        } catch (SQLException ex) {
            handleSQLException(ex);
        }
    }

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
            return new Slice(colNames, labels, points);
        } catch (SQLException ex) {
            handleSQLException(ex);
        }
        return new Slice(colNames, labels);
    }

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

    public void closeConnection() {
        try {
            connection.close();
        } catch (SQLException ex) {
            handleSQLException(ex);
        }
    }

    private void handleSQLException(SQLException ex) {
        ex.printStackTrace();
        try {
            connection.rollback();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}

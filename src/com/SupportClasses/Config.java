package com.SupportClasses;

import com.DataObjects.Approximations.ApproximationType;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Класс, который получает конфигурацию из текстового файла (параметры, такие как название файла, базы данных, данные для
 * соединения с БД)
 */
public class Config {

    private Map<String, String> config;

    public Config() {
        config = getConfig();
    }

    /**
     * Считывает содержимое файла конфигурации.
     *
     * @return карта (ключи - названия параметров, значения - собственно параметры)
     */
    private Map<String, String> getConfig() {
        Map<String, String> res = new HashMap<>();
        try {
            BufferedReader lineReader = new BufferedReader(new FileReader("db_config.txt"));

            String line = lineReader.readLine();
            while(line != null) {
                if(!line.startsWith("//")) {
                    String[] lineSplit = line.split("=");
                    if(lineSplit.length == 2) {
                        res.put(lineSplit[0], lineSplit[1]);
                    }
                }
                line = lineReader.readLine();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        res.putIfAbsent("db_name", "evaluatordb");
        res.putIfAbsent("user_name", "evaluator");
        res.putIfAbsent("password", "comparison419");
        res.putIfAbsent("table_name", "data_v06");
        res.putIfAbsent("max_slices_per_combo", "16");
        res.putIfAbsent("approximation_type", "linear");
        return res;
    }

    public String getDbName() {
        return config.get("db_name");
    }

    public String getUserName() {
        return config.get("user_name");
    }

    public String getPassword() {
        return config.get("password");
    }

    public String getTableName() {
        return config.get("table_name");
    }

    public int getMaxSlicesPerCombo() {
        try {
            return Integer.parseInt(config.get("max_slices_per_combo"));
        } catch (NumberFormatException e) {
            return 16;
        }
    }
    public ApproximationType getApproximationType() {
        String approximationTypeStr = config.get("approximation_type");
        switch(approximationTypeStr) {
            case "empty":
                return ApproximationType.EMPTY;
            case "linear":
                return ApproximationType.LINEAR;
            case "averages":
                return ApproximationType.AVERAGES;
            default:
                return ApproximationType.EMPTY;
        }
    }

}

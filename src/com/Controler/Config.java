package com.Controler;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

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
                String[] lineSplit = line.split("=");
                if(lineSplit.length == 2) {
                    res.put(lineSplit[0], lineSplit[1]);
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

}

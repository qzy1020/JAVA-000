package com.qzy.hikari;

import com.sun.org.slf4j.internal.Logger;
import com.sun.org.slf4j.internal.LoggerFactory;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

public class HikariUtil {

    private static Logger logger = LoggerFactory.getLogger(HikariUtil.class);
    private static HikariUtil hikariUtil;
    public static HikariUtil getHikariUtilInstance(){
        if (hikariUtil == null) {
            hikariUtil = new HikariUtil();
        }
        return hikariUtil;
    }
    private static HikariDataSource hikariDataSource;
    public static HikariDataSource getHikariDataSourceInstance(){
        if (hikariDataSource == null) {
            hikariDataSource = new HikariDataSource();
        }
        return hikariDataSource;
    }
    public HikariDataSource getDataSource() throws IOException, SQLException, IOException {
        Properties properties = new Properties();
        InputStream in = HikariUtil.class.getClass().getResourceAsStream("db.properties");
        properties.load(in);

        int max_conn =  Integer.valueOf(properties.getProperty("max_conn"));
        String url = String.valueOf(properties.getProperty("url"));
        int port = Integer.valueOf(properties.getProperty("port"));
        String name = String.valueOf(properties.getProperty("name"));
        String username = String.valueOf(properties.getProperty("username"));
        String password = String.valueOf(properties.getProperty("password"));

        if (url == null || url.length() == 0) {
            return null;
        }

        HikariConfig config = new HikariConfig();
        config.setMaximumPoolSize(max_conn);
        config.setDataSourceClassName("com.mysql.jdbc.jdbc2.optional.MysqlDataSource");
        config.addDataSourceProperty("serverName", url);
        config.addDataSourceProperty("port", port);
        config.addDataSourceProperty("databaseName", name);
        config.addDataSourceProperty("user", username);
        config.addDataSourceProperty("password", password);
        return new HikariDataSource(config);
    }


    public Connection getConnection() {
        try {
            return getDataSource().getConnection();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean stop() throws Exception {
        getDataSource().close();
        return true;
    }
}

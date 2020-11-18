package com.qzy.jdbc;

import com.sun.org.apache.bcel.internal.generic.RETURN;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class JdbcUtil {

    private static Connection conn;
    public static Connection createConnection(){
        //加载驱动
        try {
            Class.forName("com.mysql.jdbc.Driver");
            String jdbc = "jdbc:mysql://127.0.0.1:3306/test1?characterEncoding=UTF-8";
            //创建连接
            conn = DriverManager.getConnection(jdbc, "root", "123456");
            return conn;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    public static Connection getConnection(){
        if (conn == null){
            createConnection();
        }
        return conn;
    }

    public void insertData() throws Exception {
        Statement state = getConnection().createStatement();   //容器
        String sql = "insert into Student values('100','xxx', 15)";
        state.executeUpdate(sql);
        getConnection().close();
    }

    public void updateData() throws Exception {
        Statement state = getConnection().createStatement();
        String sql = "update student set name='xxx' where id='100'";
        state.executeUpdate(sql);
        getConnection().close();
    }

    public void queryData() throws Exception {
        Statement state = getConnection().createStatement();
        String sql = "select id from student";
        ResultSet rs = state.executeQuery(sql);
        List<String> list = new ArrayList<String>();
        while (rs.next()){
            list.add(rs.getString("id"));
        }
        getConnection().close();
    }

    public void deleteData() throws Exception {
        Statement state = getConnection().createStatement();
        String sql="delete from student where id='100'";
        state.executeUpdate(sql);
        getConnection().close();
    }
}

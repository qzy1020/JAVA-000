package com.qzy.statement;

import com.qzy.jdbc.JdbcUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

public class JdbcStatementUtil {

    public void transaction() throws SQLException {
        Connection con = null;
        PreparedStatement st = null;
        String sql1 ="insert into student (name, age) values('tom', 23)";
        String sql2 ="delete from student where id = 100";
        try {
            con = JdbcUtil.getConnection();
            con.setAutoCommit(false);
            st = con.prepareStatement(sql1);
            st.executeUpdate();
            System.out.println("第一个语句成功了");
            st = con.prepareStatement(sql2);
            st.executeUpdate();
            System.out.println("第二个语句成功了");
            con.commit();
        } catch (Exception e) {
            try {
                con.rollback();//出现异常进行回滚；
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
            e.printStackTrace();
        }finally{
            try {
                con.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
            con.close();
            st.close();
        }
    }

    public void handleBatch1() throws SQLException {
        Connection con = null;
        Statement stat = null;
        con = JdbcUtil.getConnection();
        try {
            stat = con.createStatement();
            stat.addBatch("drop database if exists test");
            stat.addBatch("create database test");
            stat.addBatch("use test");
            stat.addBatch("create table student(id int primary key auto_increment, name varchar(20), age int(11))");
            stat.addBatch("insert into student values(null,'a', 21)");
            stat.addBatch("insert into student values(null,'b', 23)");
            stat.addBatch("insert into student values(null,'c', 24)");
            stat.executeBatch();
        } catch (Exception e) {
            e.printStackTrace();
        }finally{
            con.close();
            stat.close();
        }
    }

    public void handleBatch2() throws SQLException {
        Connection con = null;
        Statement stat = null;
        PreparedStatement ps = null;

        try {
            Class.forName("com.mysql.jdbc.Driver");
            con = JdbcUtil.getConnection();
            //开始事务
            con.setAutoCommit(false);
            String sql = "insert into student values ('test',?)";
            ps = con.prepareStatement(sql);
            for(int i = 100;i < 300;i++){
                ps.setString(1, "name"+i);
                ps.addBatch();
            }
            ps.executeBatch();
            //提交事务
            con.commit();
            System.out.println("执行完成");
        } catch (Exception e) {
            e.printStackTrace();
        }finally{
            con.close();
            stat.close();
            ps.close();
        }
    }
}

package generic.kodro;

import java.lang.reflect.*;
import java.sql.*;
import java.util.*;

import generic.base.*;

public class A {
    public static Statement statement;
    public static String query;
    public static boolean mine = true;

    public static void insert(Connection c, Object o) throws Exception {
        if (c == null || c.isClosed()) {
            c = new Connexion().enterToBdd();
            mine = false;
        }
        List<Field> fields = U.getFields(o.getClass());
        U.construirePK(c, o, fields);
        query = "INSERT INTO " + U.getTableName(o)+ " " + U.colonnes(fields, o) + " VALUES(" + U.stringValues(fields, o) + ")";
        System.out.println(query);
        statement = c.createStatement();
        int x = statement.executeUpdate(query);
        System.out.println(x + " row(s)");
        closeData(c);
    }

    public static void update(Connection c, Object o1, Object o) throws Exception {
        if (c == null || c.isClosed()) {
            c = new Connexion().enterToBdd();
            mine = false;
        }
        List<Field> fields = U.getFields(o1.getClass());
        statement = c.createStatement();
        query = "UPDATE " + U.getTableName(o1) + U.getSetter(o) + U.condition(fields, o1);
        System.out.println(query);
        int x = statement.executeUpdate(query);
        closeData(c);
    }

    public static void delete(Connection c, Object o) throws Exception {
        if (c == null || c.isClosed()) {
            c = new Connexion().enterToBdd();
            mine = false;
        }
        statement = c.createStatement();
        List<Field> fields = U.getFields(o.getClass());
        query = "DELETE FROM " + U.getTableName(o) + " " + U.condition(fields, o);
        System.out.println(query);
        int x = statement.executeUpdate(query);
        closeData(c);
    }

    public static List select(Connection c, Object o) throws Exception {
        if (c == null || c.isClosed()) {
            c = new Connexion().enterToBdd();
            mine = false;
        }
        List<Field> fields = U.getFields(o.getClass());
        statement = c.createStatement();
        query = "SELECT * FROM " + U.getTableName(o) + " " + U.condition(fields, o);
        System.out.println(query);
        ResultSet resultSet = statement.executeQuery(query);
        List object = U.getComposant(resultSet, fields, o, c);
        System.out.println("line find");
        return object;
    }

    public static List executeQuery(Connection c, Object o, String sql) throws Exception {
        if (c == null || c.isClosed()) {
            c = new Connexion().enterToBdd();
            mine = false;
        }
        List<Field> fields = U.getFields(o.getClass());
        System.out.println(sql);
        ResultSet resultSet = statement.executeQuery(sql);
        List object = U.getComposant(resultSet, fields, o, c);
        System.out.println("line find");
        return object;
    }

    public static void executeUpdate(Connection c, String sql) throws Exception {
        if (c == null || c.isClosed()) {
            c = new Connexion().enterToBdd();
            mine = false;
        }
        System.out.println(sql);
        statement = c.createStatement();
        int x = statement.executeUpdate(sql);
        closeData(c);
    }

    public static void closeData(Connection c) throws SQLException {
        if(!mine){
            try {
                c.commit();
            } catch (Exception ee) {
                c.rollback();
            } finally {
                c.close();
            }
            mine = true;
        }
    }
}
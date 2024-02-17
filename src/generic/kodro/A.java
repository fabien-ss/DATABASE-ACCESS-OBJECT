package generic.kodro;

import java.lang.reflect.*;
import java.sql.*;
import java.util.*;

import generic.base.*;
import jdk.jfr.Label;

public class A {

    @Label("Insert list object")
    public static void insertList(Connection c, List<Object> o) throws Exception {
        boolean mine = true;
        if (c == null || c.isClosed()) {
            c = new Connexion().enterToBdd();
            mine = false;
        }
        List<Field> fields = U.getFields(o.get(0).getClass());
        U.construirePK(c, o.get(0), fields);
        System.out.println("first init ok");
        String query = "INSERT INTO " + U.getTableName(o.get(0))+ " " + U.colonnes(fields, o.get(0)) + " VALUES " + U.multipleInsert(c,fields, o) + "";
        System.out.println(query);
        Statement statement = c.createStatement();
        int x = statement.executeUpdate(query);
        System.out.println(x + " row(s)");
        U.closeData(c, mine);
    }

    @Label("Insert object")
    public static void insert(Connection c, Object o) throws Exception {
        System.out.println("tsotra");
        boolean mine = true;
        if (c == null || c.isClosed()) {
            c = new Connexion().enterToBdd();
            mine = false;
        }
        List<Field> fields = U.getFields(o.getClass());
        U.construirePK(c, o, fields);
        String query = "INSERT INTO " + U.getTableName(o)+ " " + U.colonnes(fields, o) + " VALUES " + U.stringValues(fields, o) + "";
        System.out.println(query);
        Statement statement = c.createStatement();
        int x = statement.executeUpdate(query);
        System.out.println(x + " row(s)");
        U.closeData(c, mine);
    }

    @Label("Update object, parameter 1: Connection, parameter 2: object to update, parameter 3: object to update new value")
    public static void update(Connection c, Object o1, Object o) throws Exception {
        boolean mine = true;
        if (c == null || c.isClosed()) {
            c = new Connexion().enterToBdd();
            mine = false;
        }
        List<Field> fields = U.getFields(o1.getClass());
        Statement statement = c.createStatement();
        String query = "UPDATE " + U.getTableName(o1) + U.getSetter(o) + U.condition(fields, o1);
        System.out.println(query);
        int x = statement.executeUpdate(query);
        U.closeData(c, mine);
    }

    @Label("Delete object")
    public static void delete(Connection c, Object o) throws Exception {
        boolean mine = true;
        if (c == null || c.isClosed()) {
            c = new Connexion().enterToBdd();
            mine = false;
        }
        Statement statement = c.createStatement();
        List<Field> fields = U.getFields(o.getClass());
        String query = "DELETE FROM " + U.getTableName(o) + " " + U.condition(fields, o);
        System.out.println(query);
        int x = statement.executeUpdate(query);
        U.closeData(c, mine);
    }

    @Label("Get list of the target object o, depend on object o's attributes values")
    public static List select(Connection c, Object o) throws Exception {
        boolean mine = true;
        if (c == null || c.isClosed()) {
            c = new Connexion().enterToBdd();
            mine = false;
        }
        List<Field> fields = U.getFields(o.getClass());
        Statement statement = c.createStatement();
        String query = "SELECT * FROM " + U.getTableName(o) + " " + U.condition(fields, o);
        System.out.println(query);
        ResultSet resultSet = statement.executeQuery(query);
        List object = U.getComposant(resultSet, fields, o, c);
        System.out.println("line find");
        U.closeData(c, mine);
        return object;
    }

    @Label("Get sql return $")
    public static List executeQuery(Connection c, Object o, String sql) throws Exception {
        boolean mine = true;
        if (c == null || c.isClosed())
            c = new Connexion().enterToBdd();
            mine = false;
        List<Field> fields = U.getFields(o.getClass());
        System.out.println(sql);
        Statement statement = c.createStatement();
        ResultSet resultSet = statement.executeQuery(sql);
        List object = U.getComposant(resultSet, fields, o, c);
        System.out.println("line find");
        U.closeData(c, mine);
        return object;
    }

    @Label("Execute a query wich update the database like insert or update or delete")
    public static void executeUpdate(Connection c, String sql) throws Exception {
        boolean mine = true;
        if (c == null || c.isClosed()) {
            c = new Connexion().enterToBdd();
            mine = false;
        }
        System.out.println(sql);
        Statement statement = c.createStatement();
        int x = statement.executeUpdate(sql);
        U.closeData(c, mine);
    }
}
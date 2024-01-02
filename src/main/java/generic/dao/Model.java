package generic.dao;

import java.lang.reflect.*;
import java.sql.*;
import java.util.*;

import generic.base.*;
import generic.annotation.*;

public class Model {

    String prefixe = "";
    int longPK = 7;
    String sequence = "";
    Boolean primaryKey = false;

    private String getTableName() throws Exception{
        try{
            return this.getClass().getAnnotation(C.class).t();
        }
        catch(Exception e){
            throw new Exception("Pas de correspondance de table "+e.getMessage());
        } 
    }

    private List getComposant(ResultSet resultSet, List<Field> fields) throws Exception {
        List<Object> vObjects = new Vector<>();
        while (resultSet.next()) {
            Constructor<?> constructor = this.getClass().getDeclaredConstructor();
            Object ob = constructor.newInstance();
      		int i = 1;
                for (Field field : fields) {
                    if(field.isAnnotationPresent(C.class)){
                        field.setAccessible(true);
                        String name = TableUtility.getColumnName(field);
                        String method = TableUtility.getSetter(field.getName());
                        Method m = this.getClass().getMethod(method, field.getType());
                        m.setAccessible(true);        
                        m.invoke(ob, resultSet.getObject(name));
                        m.setAccessible(false);
                    }
              	}
            
            vObjects.add(ob);
        }
        return vObjects;
    }

    private String conditionAttributNonNulle(List<Field> modelFields) throws Exception{
        String condition = " where ";
        for (int i = 0; i < modelFields.size(); i++) {
            modelFields.get(i).setAccessible(true);
            if(modelFields.get(i).getDeclaredAnnotation(C.class)==null)
                continue;
            C coresp=modelFields.get(i).getAnnotation(C.class);
            if(coresp.pk()){
                if(modelFields.get(i).get(this)!=null) {
                    return " where "+TableUtility.getColumnName(modelFields.get(i))+"='"+modelFields.get(i).get(this)+"'";
                } 
            }
            else{
                if(modelFields.get(i).get(this)!=null){
                    condition += condition + TableUtility.getColumnName(modelFields.get(i))+"='"+modelFields.get(i).get(this)+"'" +
                    " and ";
                }
            }
        }
        if(condition.length() > 7){
            return condition.substring(0, condition.length() - 5);
        }
        return "";
    }
    
    private String condition(List<Field> g) throws Exception{
        String pk = "";
        pk = pk + this.conditionAttributNonNulle(g);
        return pk;
    }

    private String stringValues(List<Field> g) throws Exception{
        String values = "";
        for (Field field : g) {
            field.setAccessible(true);
            if(field.getAnnotation(C.class) != null && field.get(this) !=null){
                values = values + "'" + field.get(this) + "',";
            }
        }
        values = values.substring(0, values.length() - 1);
        return values;
    }

    private String colonnes(List<Field> g) throws Exception{
        String values = "(";
        for (Field field : g) {
            field.setAccessible(true);
            if(field.getAnnotation(C.class) != null && field.get(this) !=null){
                C correspondance = field.getAnnotation(C.class);
                if(!correspondance.c().equals("")){
                    values = values + "" + correspondance.c() + ",";
                }
                else{
                    values = values + "" + field.getName() + ",";
                }
            }
            field.setAccessible(false);
        }
        values = values.substring(0, values.length() - 1);
        return values+")";
    }

    public String construirePK(Connection c) throws Exception {
        int sequence = this.getSequence(c);
        int nb = this.getPrefixe().length();
        int reste = this.getLongPK() - nb;
        String num = this.mameno(sequence, reste);
        return this.getPrefixe()+num;
    }

    private String mameno(int numero, int reste) {
        String num = numero + "";
        String zero = "";
        reste = reste - num.length();
        for (int i = 0; i < reste; i++) {
            zero = zero + "0";
        }
        String retour = zero + num;
        return retour;
    }

    private int getSequence(Connection c) throws Exception {
    
        String sql = "Select nextval('" + this.getSequence() + "')";
        Statement statement = c.createStatement();
        ResultSet resultSet = statement.executeQuery(sql);
        if (resultSet.next()) {
            return resultSet.getInt(1);
        }
        return 0;
    }

    private String getSetter(Object o) throws Exception{
        String condition = " set ";
        List<Field> g = TableUtility.getFields(o.getClass());
        for (int i = 0; i < g.size(); i++) {
            g.get(i).setAccessible(true);
            if (g.get(i).getAnnotation(C.class) != null && g.get(i).get(o) != null) {
                g.get(i).setAccessible(true);
                C coresp=g.get(i).getAnnotation(C.class);
                if(!coresp.c().equals("")){
                    condition = condition + " " + coresp.c() + "='" + g.get(i).get(o) + "',";
                }
                else{
                    condition = condition + " " + g.get(i).getName() + "='" + g.get(i).get(o) + "',";
                }
            }
        }
        condition = condition.substring(0, condition.length() - 1);
        return condition;
    }

    public void insert(Connection c) throws Exception {
        boolean mine = true;
        if (c == null || c.isClosed()) {
            c = new Connexion().enterToBdd();
            mine = false;
        }
        String nomDeTable = this.getTableName();
        Field[] g = this.getClass().getDeclaredFields();
        List<Field> fields = TableUtility.getFields(this.getClass());
        String colonnes = this.colonnes(fields);
        String values = this.stringValues(fields);
        String query = "INSERT INTO " + nomDeTable+ " " + colonnes + " VALUES(" + values + ")";
        System.out.println(query);
        Statement statement = c.createStatement();
        int x = statement.executeUpdate(query);
        try {
            c.commit();
            System.out.println(x + " row(s) modified successfully");
        } catch (Exception ee) {
            c.rollback();
        }
        if (!mine) c.close();
    }

    public void update(Connection c, Object o) throws Exception {
        boolean mine = true;
        if (c == null || c.isClosed()) {
            c = new Connexion().enterToBdd();
            mine = false;
        }
        List<Field> fields = TableUtility.getFields(this.getClass());
        Statement statement = c.createStatement();
        String setter = this.getSetter(o);
        String condition = this.condition(fields);
        String nomDeTable = this.getTableName();
        String query = "UPDATE " + nomDeTable + setter + condition;
        System.out.println(query);
        int x = statement.executeUpdate(query);
        try {
            c.commit();
            System.out.println(x+" rows modified");
        } catch (Exception ee) {
            c.rollback();
        }finally{
            if (!mine) {
                c.close();
            }
        }
    }

    public void delete(Connection c) throws Exception {
        boolean mine = true;
        if (c == null || c.isClosed()) 
            c = new Connexion().enterToBdd();
            mine = false;
        Statement statement = c.createStatement();
        List<Field> fields = TableUtility.getFields(this.getClass());
        String condition = this.condition(fields);
        String nomDeTable = this.getTableName();
        String query = "DELETE FROM " + nomDeTable + " " + condition;
        System.out.println(query);
        int x = statement.executeUpdate(query);
        try {
            c.commit();
            System.out.println(x+" rows modified");
        } catch (Exception ee) {
            c.rollback();
        }finally{
            if (!mine) {
                c.close();
            }
        }
    }

    public List select(Connection c) throws Exception {
        boolean mine = true;
        if (c == null || c.isClosed())
            c = new Connexion().enterToBdd();
            mine = false;
        String nomDeTable = this.getTableName();
        String query = "Select * from " + nomDeTable;
        List<Field> fields = TableUtility.getFields(this.getClass());
        String condition = this.condition(fields);
        Statement statement = c.createStatement();
        query = query + " " + condition;
        System.out.println(query);
        ResultSet resultSet = statement.executeQuery(query);
        List object = this.getComposant(resultSet, fields);
        System.out.println("line find");
        if (!mine) c.close();
        return object;
    }

    public void executeQuery(Connection c, String query) throws Exception {
        boolean mine = true;
        if (c == null || c.isClosed()) 
            c = new Connexion().enterToBdd();
            mine = false;
        System.out.println(query);
        Statement statement = c.createStatement();
        int x = statement.executeUpdate(query);
        try {
            c.commit();
            System.out.println(x+" rows modified");
        } catch (Exception ee) {
            c.rollback();
        }finally{
            if (!mine) {
                c.close();
            }
        }
    }

    public List selectAll(Connection c) throws Exception {
        System.out.println("selectAll");
        boolean mine = true;
        if (c == null || c.isClosed()) 
            c = new Connexion().enterToBdd();
            mine = false;
        String nomDeTable = this.getTableName();
        String query = "Select * from " + nomDeTable;
        System.out.println(query);
        Statement statement = c.createStatement();
        ResultSet resultSet = statement.executeQuery(query);
        List<Field> fields = TableUtility.getFields(this.getClass());
        List vObjects = this.getComposant(resultSet, fields);
        System.out.println(vObjects.size() + " "+ "lines find");
        if (!mine) c.close();
        return vObjects;
    }

    public void setPrefixe(String prefixe) {
        this.prefixe = prefixe;
    }

    public String getPrefixe() {
        return prefixe;
    }

    public void setLongPK(int longPK) throws Exception {
        if(longPK < 0) { throw new Exception("Longueur du pk doit-être strictement positif"); }
        this.longPK = longPK;
    }

    public int getLongPK() {
        return longPK;
    }

    public void setPrimaryKey(Boolean primaryKey) {
        this.primaryKey = primaryKey;
    }

    public Boolean getPrimaryKey() {
        return primaryKey;
    }

    public String getSequence() {
        return sequence;
    }

    public void setSequence(String sequence) {
        this.sequence = sequence;
    }

}
/*
    author: Fabien@KM -

    --select
    --selectAll
    --update
    --delete
*/

package utils;
import java.io.File;
import java.lang.reflect.*;
import java.sql.*;
import java.util.*;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;

public class Crud {

    String prefixe = "";
    int longPK = 7;
    String nomFonction = "";
    Boolean primaryKey = false;
    
    public Connection enterToBdd() {
        try{
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new File("config.xml"));
            Node databaseNode = doc.getElementsByTagName("database").item(0);
            String url = databaseNode.getChildNodes().item(1).getTextContent();
            String usern = databaseNode.getChildNodes().item(3).getTextContent();
            String pass = databaseNode.getChildNodes().item(5).getTextContent();
            Connection c = DriverManager.getConnection(url, usern, pass);
            c.setAutoCommit(false);
            return c;
        }
        catch(Exception e){
            System.out.println(e);
        }
        return null;
    }

    public String getTableName() throws Exception{
        String nom = "";
        try{
            return this.getClass().getAnnotation(Correspondance.class).nomTable();
        }
        catch(Exception e){
            throw new Exception("Pas de correspondance de table");
        } 
    }

    public Vector getComposant(ResultSet resultSet) throws Exception {
        Field[] fields = this.getClass().getDeclaredFields();
        Vector<Object> vObjects = new Vector<>();
        while (resultSet.next()) {
            Constructor<?> constructor = this.getClass().getDeclaredConstructor();
            Object ob = constructor.newInstance();
            try {
                int i = 1;
                for (Field field : fields) {
                    String typeF = field.getType() + "";
                    String name = field.getName();
                    String method = getSetter(name);
                    Method m = this.getClass().getDeclaredMethod(method, field.getType());
                    m.invoke(ob, resultSet.getObject(i));
                    i += 1;
                }
            } catch (Exception e) {
                
            }
            vObjects.add(ob);
        }
        return vObjects;
    }

    String nomColonnePrimaryKey(Field[] modelFields)throws Exception{
        for (int i = 0; i < modelFields.length; i++) {
            if(modelFields[i].getDeclaredAnnotation(Correspondance.class)==null)
                continue;
            Correspondance coresp=modelFields[i].getAnnotation(Correspondance.class);
            if(coresp.primarykey()){
                System.out.println(modelFields[i].getName()+"='"+modelFields[i].get(this)+"'");
                return modelFields[i].getName()+"='"+modelFields[i].get(this)+"'";
            }
        }
        throw new Exception("La classe ne contient pas de cle primaire");
    }
    
    public String conditionPK() throws Exception{
        String pk = "WHERE ";
        Field[] g = this.getClass().getDeclaredFields();
        pk = pk + this.nomColonnePrimaryKey(g);
        return pk;
    }

    public String getCondition() throws Exception{
        String condition = " WHERE ";
        Field[] g = this.getClass().getDeclaredFields();
        for (int i = 0; i < g.length; i++) {
            if (g[i].getAnnotation(Correspondance.class) != null && g[i].get(this) != null) {
                g[i].setAccessible(true);
                condition = condition +" "+ g[i].getName() + "='" + g[i].get(this) + "' and";
            }
        }
        condition = condition.substring(0, condition.length()-3);
        return condition;
    }

    public static String getSetter(String str) {
        str = firstLetterToUpper(str);
        return "set" + str;
    }

    public static String getGetter(String str) {
        str = firstLetterToUpper(str);
        return "get" + str;
    }

    public static String firstLetterToUpper(String str) {
        String retour = str.charAt(0) + "";
        retour = retour.toUpperCase();
        for (int i = 1; i < str.length(); i++) {
            retour += str.charAt(i) + "";
        }
        return retour;
    }

    public String stringValues() {
        Field[] g = this.getClass().getDeclaredFields();
        String values = "";
        try {
            for (Field field : g) {
                if(field.getAnnotation(Correspondance.class) != null && field.get(this) !=null){  
                        values = values + "'" + field.get(this) + "',";
                }
            }
        } catch (Exception ee) {
        }
        values = values.substring(0, values.length() - 1);
        return values;
    }

    public String colonnes() {
        Field[] g = this.getClass().getDeclaredFields();
        String values = "(";
        try {
            for (Field field : g) {
                if(field.getAnnotation(Correspondance.class) != null && field.get(this) !=null){
                    values = values + "" + field.getName() + ",";
                }
            }
        } catch (Exception ee) {
        }
        values = values.substring(0, values.length() - 1);
        return values+")";
    }

    public String construirePK(Connection c) throws Exception {
        Boolean mine = true;
        if(c==null || c.isClosed()){ 
            c = this.enterToBdd();
            mine = false;
        }
        int sequence = this.getSequence(c);
        String pk = this.getPrefixe();
        int nb = this.getPrefixe().length();
        int reste = this.getLongPK() - nb;
        String num = this.mameno(sequence, reste);
        if(!mine) c.close();
        return this.getPrefixe()+num;
    }

    public String mameno(int numero, int reste) {
        String num = numero + "";
        String zero = "";
        reste = reste - num.length();
        for (int i = 0; i < reste; i++) {
            zero = zero + "0";
        }
        String retour = zero + num;
        return retour;
    }

    public int getSequence(Connection c) throws Exception {
        if (c == null || c.isClosed()) {
            c = this.enterToBdd();
        }
        String sql = "Select " + this.getNomFonction() + " FROM DUAL";
        Statement statement = c.createStatement();
        ResultSet resultSet = statement.executeQuery(sql);
        while (resultSet.next()) {
            return resultSet.getInt(1);
        }
        return 0;
    }

    public String getSetter(Object o) {
        String condition = " set ";
        Field[] g = o.getClass().getDeclaredFields();
        try {
            for (int i = 0; i < g.length; i++) {
                Method m = o.getClass().getMethod(getGetter(g[i].getName()));
                Object temp = m.invoke(o);
                //field.get(t:his) !=null
                if (g[i].getAnnotation(Correspondance.class) != null && g[i].get(o) != null) {
                    g[i].setAccessible(true);
                    condition = condition + " " + g[i].getName() + "='" + g[i].get(o) + "',";
                }
            }
        } catch (Exception ee) {
        }
        condition = condition.substring(0, condition.length() - 1);
        return condition;
    }

    public void insert(Connection c) throws Exception {
        boolean mine = true;
        if (c == null || c.isClosed()) {
            c = this.enterToBdd();
            mine = false;
        }
        String values = this.stringValues();
        String nomDeTable = this.getTableName();
        String colonnes = this.colonnes();
        String query = "INSERT INTO " + nomDeTable+ " " + colonnes + " VALUES(" + values + ")";
        System.out.println(query);
        Statement statement = c.createStatement();
        int x = statement.executeUpdate(query);
        try {
            c.commit();
        } catch (Exception ee) {
            c.rollback();
        }
        if (!mine) {
            c.close();
        }
    }

    public void update(Connection c, Object o) throws Exception {
        boolean mine = true;
        if (c == null || c.isClosed()) {
            c = this.enterToBdd();
            mine = false;
        }
        Statement statement = c.createStatement();
        String setter = this.getSetter(o);
        String condition = this.conditionPK();
        String nomDeTable = this.getTableName();
        String query = "UPDATE " + nomDeTable + setter + condition;
        System.out.println(query);
        int x = statement.executeUpdate(query);
        try {
            c.commit();
            System.out.println(x+" rows modified");
        } catch (Exception ee) {
            c.rollback();
        }
        if (!mine) {
            c.close();
        }
    }

    public void delete(Connection c) throws Exception {
        boolean mine = true;
        if (c == null || c.isClosed()) {
            c = this.enterToBdd();
            mine = false;
        }
        Statement statement = c.createStatement();
        String condition = this.conditionPK();
        String nomDeTable = this.getTableName();
        String query = "DELETE FROM " + nomDeTable + " " + condition;
        System.out.println(query);
        int x = statement.executeUpdate(query);
        try {
            c.commit();
            System.out.println(x+" rows modified");
        } catch (Exception ee) {
            c.rollback();
        }
        if (!mine) {
            c.close();
        }
    }

    public Vector select(Connection c) throws Exception {
        boolean mine = true;
        if (c == null || c.isClosed()) {
            c = this.enterToBdd();
            mine = false;
        }
        String nomDeTable = this.getTableName();
        String query = "Select * from " + nomDeTable;
        String condition = this.conditionPK();
        Statement statement = c.createStatement();
        query = query + " " + condition;
        System.out.println(query);
        ResultSet resultSet = statement.executeQuery(query);
        Vector vObjects = this.getComposant(resultSet);
        System.out.println(vObjects.size() + " "+ "line find");
        if (!mine) {
            c.close();
        }
        return vObjects;
    }

    public Vector selectAll(Connection c) throws Exception {
        boolean mine = true;
        if (c == null || c.isClosed()) {
            c = this.enterToBdd();
            mine = false;
        }
        String nomDeTable = this.getTableName();
        String query = "Select * from " + nomDeTable;
        Statement statement = c.createStatement();
        query = query;
        ResultSet resultSet = statement.executeQuery(query);
        Vector vObjects = this.getComposant(resultSet);
        c.close();
        System.out.println(vObjects.size() + " "+ "lines find");
        if (!mine) {
            c.close();
        }
        return vObjects;
    }

    public void setPrefixe(String prefixe) {
        this.prefixe = prefixe;
    }

    public String getPrefixe() {
        return prefixe;
    }

    public void setLongPK(int longPK) {
        this.longPK = longPK;
    }

    public int getLongPK() {
        return longPK;
    }

    public void setNomFonction(String nomFonction) {
        this.nomFonction = nomFonction;
    }

    public String getNomFonction() {
        return nomFonction;
    }

    public void setPrimaryKey(Boolean primaryKey) {
        this.primaryKey = primaryKey;
    }

    public Boolean getPrimaryKey() {
        return primaryKey;
    }
}
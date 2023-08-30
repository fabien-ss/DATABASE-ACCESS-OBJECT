package generic;

import java.io.File;
import java.lang.reflect.*;
import java.sql.*;
import java.util.*;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class Model {

    String prefixe = "";
    int longPK = 7;
    String sequence = "";
    Boolean primaryKey = false;
    String url = "";
    String password = "";
    String username = "";

    public void init() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(new File("/files/config.xml"));
        Node databaseNode = doc.getElementsByTagName("database").item(0);
        url = databaseNode.getChildNodes().item(1).getTextContent();
        username = databaseNode.getChildNodes().item(3).getTextContent();
        password = databaseNode.getChildNodes().item(5).getTextContent();   
    }

    public void checkDatabaseConnection() throws Exception {
        if(url.equals("")) throw new Exception("Url vide.");
        if(username.equals("")) throw new Exception("Nom d'utilisateur vide.");
        if(password.equals("")) throw new Exception("Mot de passe vide.");
    }
    
    public Connection enterToBdd() throws Exception{
        this.init();
        this.checkDatabaseConnection();
        Class.forName("org.postgresql.Driver");
        Connection c = DriverManager.getConnection(this.url, this.username, this.password);
        c.setAutoCommit(false);
        return c;
   }

    public String getTableName() throws Exception{
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
                int i = 1;
                for (Field field : fields) {
                    field.setAccessible(true);
                    String name = field.getName();
                    String method = getSetter(name);
                    Method m = this.getClass().getDeclaredMethod(method, field.getType());
                    m.setAccessible(true);
                    
                    try {
                        m.invoke(ob, resultSet.getObject(i));
                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                    }
                    i += 1;
                }
            
            vObjects.add(ob);
        }
        return vObjects;
    }

    public Object getUniqueComposant(ResultSet resultSet) throws Exception {
        Field[] fields = this.getClass().getDeclaredFields();
        if (resultSet.next()) {
            Constructor<?> constructor = this.getClass().getDeclaredConstructor();
            Object ob = constructor.newInstance();
                int i = 1;
                for (Field field : fields) {
                    field.setAccessible(true);
                    String name = field.getName();
                    String method = getSetter(name);
                    Method m = this.getClass().getDeclaredMethod(method, field.getType());
                    m.setAccessible(true);
                    
                    try {
                        m.invoke(ob, resultSet.getObject(i));
                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                    }
                    i += 1;
                }
            
            return ob;
        }
        return null;
    }

    String nomColonnePrimaryKey(Field[] modelFields)throws Exception{
        for (int i = 0; i < modelFields.length; i++) {
            modelFields[i].setAccessible(true);
            if(modelFields[i].getDeclaredAnnotation(Correspondance.class)==null)
                continue;
            Correspondance coresp=modelFields[i].getAnnotation(Correspondance.class);
            if(coresp.primarykey()){
                return modelFields[i].getName()+"='"+modelFields[i].get(this)+"'";
            }
        }
        throw new Exception("La classe ne contient pas de cle primaire");
    }

    String conditionAttributNonNulle(Field[] modelFields) throws Exception{
        String condition = "";
        for (int i = 0; i < modelFields.length; i++) {
            modelFields[i].setAccessible(true);
            if(modelFields[i].getDeclaredAnnotation(Correspondance.class)==null)
                continue;
            Correspondance coresp=modelFields[i].getAnnotation(Correspondance.class);
            if(coresp.primarykey()){
                if(modelFields[i].get(this)!=null) {
                    return modelFields[i].getName()+"='"+modelFields[i].get(this)+"'";
                } 
            }
            else{
                if(modelFields[i].get(this)!=null){
                    if(!coresp.nomColonne().equals("")){
                        condition = condition + coresp.nomColonne()+"='"+modelFields[i].get(this)+"'";
                    }
                    if(coresp.nomColonne().equals("")){
                        condition = condition + modelFields[i].getName()+"='"+modelFields[i].get(this)+"'";
                    }
                    if(i +1 !=modelFields.length-1){
                        condition = condition + " and ";
                    }
                }
            }
        }
        return condition;
    }
    
    public String condition() throws Exception{
        String pk = " WHERE ";
        Field[] g = this.getClass().getDeclaredFields();
        if(!this.hasCondition(g)) return "";
        pk = pk + this.conditionAttributNonNulle(g);
        return pk;
    }

    public boolean hasCondition(Field[] modelFields) throws Exception{
        for (int i = 0; i < modelFields.length; i++) {
            modelFields[i].setAccessible(true);
            if(modelFields[i].get(this) != null) {
                return true;
            }
        }
        return false;
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

    public String stringValues() throws Exception{
        Field[] g = this.getClass().getDeclaredFields();
        String values = "";
        for (Field field : g) {
            field.setAccessible(true);
            if(field.getAnnotation(Correspondance.class) != null && field.get(this) !=null){  
                values = values + "'" + field.get(this) + "',";
            }
        }
        values = values.substring(0, values.length() - 1);
        return values;
    }

    public String colonnes() throws Exception{
        Field[] g = this.getClass().getDeclaredFields();
        String values = "(";
        for (Field field : g) {
            field.setAccessible(true);
            if(field.getAnnotation(Correspondance.class) != null && field.get(this) !=null){
                Correspondance correspondance = field.getAnnotation(Correspondance.class);
                if(!correspondance.nomColonne().equals("")){
                    values = values + "" + correspondance.nomColonne() + ",";
                }
                else{
                    values = values + "" + field.getName() + ",";
                }
            }
        }
        values = values.substring(0, values.length() - 1);
        return values+")";
    }

    public String construirePK(Connection c) throws Exception {
        Boolean mine = true;
        if(c==null || c.isClosed())
            c = this.enterToBdd();
            mine = false;
        int sequence = this.getSequence(c);
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
        String sql = "Select nextval('" + this.getSequence() + "')";
        Statement statement = c.createStatement();
        ResultSet resultSet = statement.executeQuery(sql);
        if (resultSet.next()) {
            return resultSet.getInt(1);
        }
        return 0;
    }

    public String getSetter(Object o) throws Exception{
        String condition = " set ";
        Field[] g = o.getClass().getDeclaredFields();
        for (int i = 0; i < g.length; i++) {
            g[i].setAccessible(true);
            if (g[i].getAnnotation(Correspondance.class) != null && g[i].get(o) != null) {
                g[i].setAccessible(true);
                Correspondance coresp=g[i].getAnnotation(Correspondance.class);
                if(!coresp.nomColonne().equals("")){
                    condition = condition + " " + coresp.nomColonne() + "='" + g[i].get(o) + "',";
                }
                else{
                    condition = condition + " " + g[i].getName() + "='" + g[i].get(o) + "',";
                }
            }
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
            System.out.println(x+" rows modified");
        } catch (Exception ee) {
            c.rollback();
        }
        if (!mine) c.close();
    }

    public void update(Connection c, Object o) throws Exception {
        boolean mine = true;
        if (c == null || c.isClosed()) {
            c = this.enterToBdd();
            mine = false;
        }
        Statement statement = c.createStatement();
        String setter = this.getSetter(o);
        String condition = this.condition();
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
        if (c == null || c.isClosed()) 
            c = this.enterToBdd();
            mine = false;
        Statement statement = c.createStatement();
        String condition = this.condition();
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
        if (!mine) c.close();
    }

    public Object select(Connection c) throws Exception {
        boolean mine = true;
        if (c == null || c.isClosed())
            c = this.enterToBdd();
            mine = false;
        String nomDeTable = this.getTableName();
        String query = "Select * from " + nomDeTable;
        String condition = this.condition();
        Statement statement = c.createStatement();
        query = query + " " + condition;
        System.out.println(query);
        ResultSet resultSet = statement.executeQuery(query);
        Object object = this.getUniqueComposant(resultSet);
        System.out.println("line find");
        if (!mine) c.close();
        return object;
    }

    public void executeQuery(Connection c, String query) throws Exception {
        boolean mine = true;
        if (c == null || c.isClosed()) 
            c = this.enterToBdd();
            mine = false;
        System.out.println(query);
        Statement statement = c.createStatement();
        statement.execute(query);
        if (!mine) c.close();
    }

    public Vector selectAll(Connection c) throws Exception {
        System.out.println("selectAll");
        boolean mine = true;
        if (c == null || c.isClosed()) 
            c = this.enterToBdd();
            mine = false;
        String nomDeTable = this.getTableName();
        String condition = this.condition();
        String query = "Select * from " + nomDeTable + " " + condition;
        System.out.println(query);
        Statement statement = c.createStatement();
        ResultSet resultSet = statement.executeQuery(query);
        Vector vObjects = this.getComposant(resultSet);
        c.close();
        System.out.println(vObjects.size() + " "+ "lines find");
        if (!mine) c.close();
        return vObjects;
    }

    public static String sqlType(Field o) throws Exception {
        Correspondance annotation = o.getDeclaredAnnotation(Correspondance.class);
        String retour = annotation.nomColonne() + " " + annotation.typeColonne();
        if(annotation.primarykey()) retour += " PRIMARY KEY ";
        if(annotation.foreignkey()) retour += " FOREIGN KEY " + annotation.foreignkeyColonne();
        return retour;
    }

    public String sqlColonne() throws Exception{
        Field[] fields = this.getClass().getDeclaredFields();
        String colonnes = "( \n";
        for(int i=0; i<fields.length; i++){
            if(fields[i].isAnnotationPresent(Correspondance.class)){
                String temp = sqlType(fields[i]);
                colonnes += "\t" + temp;
                if(i != fields.length - 1 & fields[i + 1].isAnnotationPresent(Correspondance.class)){
                    colonnes += ",";
                }
                colonnes += "\n";
            }
        }
        return colonnes + ");";
    }

    public String migrate() throws Exception{
        String create = "CREATE TABLE IF NOT EXISTS ";
        String tableName = this.getTableName();
        String colonnes = this.sqlColonne();
        return create + tableName + colonnes;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
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
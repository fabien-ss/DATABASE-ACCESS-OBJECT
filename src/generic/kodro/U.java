package generic.kodro;

import generic.annotation.C;
import generic.annotation.P;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class U {

     public static void closeData(Connection c, boolean mine) throws SQLException {
        if(!mine){
            try {
                c.commit();
            } catch (Exception ee) {
                c.rollback();
            } finally {
                c.close();
            }
        }
    }

    public static String multipleInsert(Connection c ,List<Field> fields,List<Object> objects) throws Exception{
        String sqlValues = "";
        for (int i = 0; i < objects.size(); i++) {
            if(i > 0) U.construirePK(c, objects.get(i), fields);
            sqlValues += " " + stringValues(fields, objects.get(i)) +",";
        }
        return sqlValues.substring(0, sqlValues.length() - 1);
    }

    public static String stringValues(List<Field> g, Object o) throws Exception{
        String values = "(";
        for (Field field : g) {
            field.setAccessible(true);
            if(field.getAnnotation(C.class) != null && field.get(o) !=null){
                values = values + "'" + field.get(o) + "',";
            }
        }
        values = values.substring(0, values.length() - 1);
        return values + ")";
    }

    public static String getTableName(Object o) throws Exception{
        try{
            return o.getClass().getAnnotation(C.class).t();
        }
        catch(Exception e){
            throw new Exception("DAO - Pas de correspondance de table "+e.getMessage());
        }
    }

    public static List getComposant(ResultSet resultSet, List<Field> fields, Object o, Connection co) throws Exception {
        List<Object> vObjects = new Vector<>();
        C c;
        String name, method = "";
        while (resultSet.next()) {
            Constructor<?> constructor = o.getClass().getDeclaredConstructor();
            Object ob = constructor.newInstance();
            int i = 1;
            for (Field field : fields) {
                if(field.isAnnotationPresent(C.class)){
                    field.setAccessible(true);
                    c = field.getDeclaredAnnotation(C.class);
                    name = U.getColumnName(field);
                    method = U.getSetter(field.getName());
                    Method m = o.getClass().getMethod(method, field.getType());
                    m.setAccessible(true);
                    if(c.fk()){
                        Class cl = field.getType();
                        Object object = cl.newInstance();
                        m.invoke(ob, findForeignObject(object, resultSet.getObject(name), co));
                    }
                    else {
                        System.out.println(name);
                        try {
                            m.invoke(ob, resultSet.getObject(name));
                        }catch (Exception e){
                            try{
                                int tour = resultSet.findColumn(name);
                                m.invoke(ob, resultSet.getObject(tour));
                            }catch (Exception es){}
                        }
                    }
                    System.out.println(resultSet.getObject(i));
                    i += 1;
                }
            }
            vObjects.add(ob);
        }
        return vObjects;
    }

    public static Object findForeignObject(Object o, Object primaryKeyValue, Connection c) throws Exception {
        List<Field> fields = getFields(o.getClass());
        if(!o.getClass().isAnnotationPresent(P.class)) throw new Exception("La table n'a pas de primary key.");
        for (Field f: fields) {
            if(f.isAnnotationPresent(C.class) & f.getDeclaredAnnotation(C.class).pk()){
                f.setAccessible(true);
                f.set(o, primaryKeyValue);
                o = A.select(c, o).get(0);
                return o;
            }
        }

        return o;
    }
    private static String conditionAttributNonNulle(List<Field> modelFields, Object o) throws Exception{
        String condition = " where ";
        for (int i = 0; i < modelFields.size(); i++) {
            modelFields.get(i).setAccessible(true);
            if(modelFields.get(i).getDeclaredAnnotation(C.class)==null)
                continue;
            C coresp=modelFields.get(i).getAnnotation(C.class);
            if(coresp.pk()){
                if(modelFields.get(i).get(o)!=null) {
                    return " where "+ U.getColumnName(modelFields.get(i))+"='"+modelFields.get(i).get(o)+"'";
                }
            }
            else{
                if(modelFields.get(i).get(o)!=null){
                    condition += U.getColumnName(modelFields.get(i))+"='"+modelFields.get(i).get(o)+"'" +
                            " and ";
                }
            }
        }
        if(condition.length() > 7){
            return condition.substring(0, condition.length() - 5);
        }
        return "";
    }

    public static String condition(List<Field> g, Object o) throws Exception{
        String pk = "";
        pk = pk + conditionAttributNonNulle(g, o);
        return pk;
    }

    public static String colonnes(List<Field> g, Object o) throws Exception{
        String values = "(";
        for (Field field : g) {
            field.setAccessible(true);
            if(field.getAnnotation(C.class) != null && field.get(o) !=null){
                C correspondance = field.getAnnotation(C.class);
                if(!correspondance.c().equals("")){
                    values = values + correspondance.c() + ",";
                }
                else{
                    values = values + field.getName() + ",";
                }
            }
            field.setAccessible(false);
        }
        values = values.substring(0, values.length() - 1);
        return values+")";
    }

    public static void construirePK(Connection c, Object o, List<Field> fields) throws Exception {
        Object[] sequenceValue = getSequenceValue(o);
        int sequence = getSequence(c, (String) sequenceValue[0]);
        int nb = ((String)sequenceValue[2]).length();
        int reste = ((int) sequenceValue[1]) - nb;
        String num = mameno(sequence, reste);
        String sequenceV = (String)sequenceValue[2]+num;
        setPrimaryKeyField(fields, sequenceV, o);
    }

    public static void setPrimaryKeyField(List<Field> fields, String sequence, Object o) throws Exception {
        boolean isAccessible = false;
        for (Field f: fields) {
            System.out.println(f.getName());
            if(f.isAnnotationPresent(C.class)){
                System.out.println("nisy");
                C c = f.getDeclaredAnnotation(C.class);
                f.setAccessible(true);
                if(c.pk() & f.get(o) == null){
                    System.out.println("null daholo");
                    f.set(o, sequence);
                    return;
                }
                f.setAccessible(false);
            }
        }
       // throw new Exception("DAO - You didn't specify primary key");
    }

    private static String mameno(int numero, int reste) {
        String num = numero + "";
        String zero = "";
        reste = reste - num.length();
        for (int i = 0; i < reste; i++) {
            zero = zero + "0";
        }
        String retour = zero + num;
        return retour;
    }

    private static int getSequence(Connection c, String o) throws Exception {
        String sql = "Select nextval('" + o + "')";
        Statement statement = c.createStatement();
        ResultSet resultSet = statement.executeQuery(sql);
        if (resultSet.next()) {
            return resultSet.getInt(1);
        }
        return 0;
    }

    public static String getSetter(Object o) throws Exception{
        String condition = " set ";
        List<Field> g = U.getFields(o.getClass());
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


    static String getColumnName(Field field){
        if(field.isAnnotationPresent(C.class)){
            C c = field.getAnnotation(C.class);
            if(c.c() != ""){
                return c.c();
            }
        }
        return field.getName();
    }

    static List<Field> getFields(Class c){
        List<Field> fields = new ArrayList<Field>();
        while (c != Object.class) {
            fields.addAll(getFieldFromClass(c));
            c = c.getSuperclass();
        }
        return fields;
    }

    private static List<Field> getFieldFromClass(Class<?> clazz) {
        Field[] fields = clazz.getDeclaredFields();
        List<Field> fieldslst = new ArrayList<Field>();
        for (Field field : fields) {
            fieldslst.add(field);
        }
        return fieldslst;
    }

    public static Object[] getSequenceValue(Object o) throws Exception {
        if(!o.getClass().isAnnotationPresent(P.class)) throw new Exception("DAO - Cannot generate sequence, sequence annotation needed");
        Object[] retour = new Object[3];
        P p = o.getClass().getDeclaredAnnotation(P.class);
        retour[0] = p.s();
        retour[1] = p.l();
        retour[2] = p.p();
        return retour;
    }
}

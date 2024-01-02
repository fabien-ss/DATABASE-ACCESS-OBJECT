package generic.dao;

import java.io.BufferedReader;
import java.io.FileReader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import generic.annotation.*;

public class TableUtility {

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
            C c = (C)field.getAnnotation(C.class);
            if(c.c() != ""){
                return c.c();
            }
        }
        return field.getName();
    }

    static List<Field> getFields(Class c){
        List<Field> fields = new ArrayList<Field>();
        while (c != Model.class) {
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
    
    public static HashMap<String, String> fetchData(String data){
        String[] line = data.split("\n");
        HashMap<String, String> mappings = new HashMap<String, String>();
        for (String string : line) {
            String[] column = string.split("=");
            mappings.put(column[0], column[1]);
        }   
        return mappings;
    }
    
    public static String chargerModele(String cheminFichier) throws Exception {
        StringBuilder contenu = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(cheminFichier))) {
            String ligne;
            while ((ligne = reader.readLine()) != null) {
                contenu.append(ligne).append("\n");
            }
        } 
        return contenu.toString();
    }
}

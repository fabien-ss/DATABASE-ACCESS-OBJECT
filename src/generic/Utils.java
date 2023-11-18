package generic;

import annotation.Column;
import annotation.Table;

public class Utils {
    
    public static String getTableName(Object objet){
        String retour = objet.getClass().getName();
        if(objet.getClass().isAnnotationPresent(Table.class)){
            Table table = objet.getClass().getAnnotation(Table.class);
            if(table.name() != null || table.name() != ""){
                retour = objet.getClass().getAnnotation(Column.class).nomTable();
            }
        }
        return retour;
    }
}

package annotation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
public @interface Column {
    
    public String nomTable() default"";

    public String nomColonne() default"";
    
}

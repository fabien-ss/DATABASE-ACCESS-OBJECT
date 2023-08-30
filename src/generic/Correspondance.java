package generic;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
public @interface Correspondance {
    
    public String nomTable() default"";

    public String nomColonne() default"";

    public boolean foreignkey() default false;

    public String foreignkeyColonne() default "";

    public boolean primarykey() default false;
    
    public String typeColonne() default "";
}

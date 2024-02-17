package generic.annotation;

import java.lang.annotation.*;


@Retention(RetentionPolicy.RUNTIME)
public @interface C {
    public String t() default"";

    public String c() default"";

    public boolean fk() default false;

    public boolean pk() default false;
}

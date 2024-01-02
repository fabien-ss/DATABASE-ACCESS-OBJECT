package test;

import generic.annotation.C;
import generic.annotation.P;


@P(s = "seq_test", l = 5, p = "V")
@C(t = "test")
public class Test2 extends Test{

    @C(c = "test")
    String test;

    public void setTest(String test) {
        this.test = test;
    }

    public String getTest() {
        return test;
    }
}

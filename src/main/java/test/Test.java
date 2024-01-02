package test;

import generic.annotation.C;
import generic.annotation.P;

@P(s = "seq_test", l = 5, p = "V")
@C(t = "test")
public class Test {

    @C(c = "id", pk = true)
    private String idtest;

    public String getIdtest() {
        return idtest;
    }


    public void setIdtest(String idtest) {
        this.idtest = idtest;
    }

}

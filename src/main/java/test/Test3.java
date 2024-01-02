package test;

import generic.annotation.C;
import generic.annotation.P;

import java.util.List;

@P(p = "T", l = 7, s = "seq_test2")
@C(t = "test2")
public class Test3 {
    @C(pk = true, c = "id")
    String id;
    @C(c = "id_test" , fk = true)
    Test2 test;
    @C(c = "nom")
    String nom;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Test2 getTest() {
        return test;
    }

    public void setTest(Test2 test) {
        this.test = test;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }
}

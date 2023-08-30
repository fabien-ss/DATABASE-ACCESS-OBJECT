package generic;

@Correspondance(nomTable = "voiture")
public class Voiture extends Model {

    int a;
    @Correspondance(nomColonne = "m", typeColonne = "Varchar(200)")
    String marque;
    @Correspondance(nomColonne = "plaque", typeColonne = "Varchar(200)")
    String plaque;
    String n;

    Voiture() {
    
    }
    
    public int getA() {
        return a;
    }

    public void setA(int a) {
        this.a = a;
    }

    public String getMarque() {
        return marque;
    }

    public void setMarque(String marque) {
        this.marque = marque;
    }

    public String getPlaque() {
        return plaque;
    }

    public void setPlaque(String plaque) {
        this.plaque = plaque;
    }

    public String getN() {
        return n;
    }

    public void setN(String n) {
        this.n = n;
    }

    public static void main(String[] args) throws Exception {
       
    }
}
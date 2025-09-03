//Proyecto 5 y 6
//Alan José Alejandre Domínguez
//7CV2
package networking;
import java.io.*;
import java.util.List;
public class Libro implements java.io.Serializable
{
    public String a[]; 
    public List <Double> b;
   
    public Libro(String a[], List <Double> b)
    {
        this.a = a;
        this.b = b;
    }
    public String[] getA(){
    	return a;
    }
    public List<Double> getB(){
    	return b;
    }
}

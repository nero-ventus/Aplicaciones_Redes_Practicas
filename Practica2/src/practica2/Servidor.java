package practica2;

import javafx.util.Pair;

public class Servidor {
    
    public static void main(String[] args) {
        ServidorHelper aux = new ServidorHelper();
        Pair<char[][], String[]> container = aux.makeLetterSoup();
        
        for(int i = 0; i < 16; i++){
            for(int j = 0; j < 16; j++){
                System.out.print(container.getKey()[i][j] + " ");
            }
            System.out.println("");
        }
    }
    
}
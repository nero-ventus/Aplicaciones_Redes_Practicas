package practica2;

import java.util.Random;
import javafx.util.Pair;

public class ServidorHelper {
    private String[] words = {"hola", "adios", "dia", "noche", "amor", "odio",
        "anime", "manga", "computadora", "celular", "familia", "amigos", "musica", "silencio", "redes"};
    
    boolean checkWordFit(String word, char[][] letterSoup, int[][] visited, int x, int y, int direction){
        int vertical_move = 0;
        int horizontal_move = 0;
        
        if(direction == 0){
            vertical_move = 1;
            horizontal_move = 0;
        }
        else if(direction == 1){
            vertical_move = 1;
            horizontal_move = 1;
        }
        else if(direction == 2){
            vertical_move = 0;
            horizontal_move = 1;
        }
        else if(direction == 3){
            vertical_move = -1;
            horizontal_move = 1;
        }
        else if(direction == 4){
            vertical_move = -1;
            horizontal_move = 0;
        }
        else if(direction == 5){
            vertical_move = -1;
            horizontal_move = -1;
        }
        else if(direction == 6){
            vertical_move = 0;
            horizontal_move = -1;
        }
        else if(direction == 7){
            vertical_move = 1;
            horizontal_move = -1;
        }
        
        int k = 0;
        for(int i = x, j = y; i >= 0 && i < 16 && j >= 0 && j < 16 && k < word.length();
                i += horizontal_move, j += vertical_move, k++){
            if(visited[i][j] == 1 && letterSoup[i][j] != word.charAt(k))
                return false;
        }
        
        return k == word.length();
    }
    
    
    Pair<char[][], String[]> makeLetterSoup(){
        char[][] letterSoup = new char[16][16];
        String[] solutions = new String[words.length];
        int[][] visited = new int[16][16];
        
        for(int i = 0; i < this.words.length; i++){
            int flag = 1;
            
            for(int j = 0; j < 16; j++){
                for(int k = 0; k < 16; k++){
                    Random chancer = new Random();
                    int number = chancer.nextInt(256);
                    
                    if(number == 0){
                        int direction = chancer.nextInt(8);
                        if(checkWordFit(this.words[i], letterSoup, visited, j, k, direction)){
                            int vertical_move = 0;
                            int horizontal_move = 0;

                            if(direction == 0){
                                vertical_move = 1;
                                horizontal_move = 0;
                            }
                            else if(direction == 1){
                                vertical_move = 1;
                                horizontal_move = 1;
                            }
                            else if(direction == 2){
                                vertical_move = 0;
                                horizontal_move = 1;
                            }
                            else if(direction == 3){
                                vertical_move = -1;
                                horizontal_move = 1;
                            }
                            else if(direction == 4){
                                vertical_move = -1;
                                horizontal_move = 0;
                            }
                            else if(direction == 5){
                                vertical_move = -1;
                                horizontal_move = -1;
                            }
                            else if(direction == 6){
                                vertical_move = 0;
                                horizontal_move = -1;
                            }
                            else if(direction == 7){
                                vertical_move = 1;
                                horizontal_move = -1;
                            }

                            int z = 0;
                            for(int x = j, y = k; x >= 0 && x < 16 && y >= 0 && y < 16 && z < this.words[i].length();
                                    x += horizontal_move, y += vertical_move, z++){
                                visited[x][y] = 1;
                                letterSoup[x][y] = this.words[i].charAt(z);
                            }
                            
                            flag = 0;
                            
                            solutions[i] = "" + j + "" + "" + k + "" + "" + direction + "" + "" + this.words[i].length() + "";
                            
                            break;
                        }
                    }
                }
                
                if(flag == 0)
                    break;
            }
            
            if(flag == 1){
                i--;
            }
        }
        
        for(int i = 0; i < 16; i++){
            for(int j = 0; j < 16; j++){
                if(visited[i][j] == 0){
                    Random chancer = new Random();
                    
                    int letter_num = chancer.nextInt(26);
                    
                    char container = (char) (letter_num + 97);
                    
                    letterSoup[i][j] = container;
                }
            }
        }
        
        return new Pair<>(letterSoup, solutions);
    }
}
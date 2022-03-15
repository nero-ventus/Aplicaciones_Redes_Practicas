package practica2;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Random;
import javafx.util.Pair;

public class ServidorHelper {
    private String[] words /*= {"usb", "mouse", "ventilador", "ram", "monitor", "teclado", "procesador", "memoria",
        "laptop", "diskette", "bocina", "audifonos", "motherboard", "fuente", "gabinete"}*/;

    public ServidorHelper() {
        try{
            File aux_path = new File("");
            List<String> container = Files.readAllLines(Paths.get(aux_path.getAbsolutePath() + "\\words.txt"),
                    StandardCharsets.UTF_8);
            
            words = new String[container.size()];
            
            for(int i = 0; i < container.size(); i++)
                words[i] = container.get(i);
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }
    
    int[] answerToIntArray(String answer){
        int[] converted = new int[4];
        
        int current_part = 0;
        for(int i = 0; i < answer.length(); i++){
            String container = "";
            int j = i;
            
            while(j < answer.length() && answer.charAt(j) != ' '){
                container += answer.charAt(j);
                j++;
            }
            
            i = j;
            
            converted[current_part] = Integer.parseInt(container);
            current_part++;
        }
        
        return converted;
    }
    
    int[][] marker(int[][] marked, String ans){
        int[] coverted = answerToIntArray(ans);
        
        int direction = coverted[2];
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
        
        
        for(int i = coverted[0], j = coverted[1], k = 0; i < 16 && j < 16 && k < coverted[3];
                i += vertical_move, j += horizontal_move, k++){
            marked[i][j] = 1;
        }
        
        return marked;
    }
    
    boolean checkAnswer(String[] solutions, String answer){
        for(int i = 0; i < solutions.length; i++)
            if(answer.equals(solutions[i]))
                return true;
        return false;
    }
    
    String letterSoupToString(char[][] letterSoup, int[][] marked, int score, int dificulty, int errors){
        String ans = "";
        
        if(dificulty == 0){
            ans += "Dificultad: Facil\nEncontrar:\n";
            for(int i = 0; i < this.words.length;){
                for(int j = 0; j < 3 && i < this.words.length; j++, i++)
                    ans += this.words[i] + "\t";
                ans += "\n";
            }
        }
        else if(dificulty == 1){
            ans += "Dificultad: Media\nEncontrar:\n";
            for(int i = 0; i < this.words.length;){
                for(int j = 0; j < 3 && i < this.words.length; j++, i++){
                    for(int k = 0; k < errors && k < this.words[i].length(); k++)
                        ans += this.words[i].charAt(k);
                    ans += "\t";
                }
                ans += "\n";
            }
        }
        else if(dificulty == 2){
            ans += "Dificultad: Dificil\nEncontrar:\n";
            for(int i = 0; i < this.words.length;){
                for(int j = 0; j < 3 && i < this.words.length; j++, i++)
                    ans += this.words[i].length() + "\t";
                ans += "\n";
            }
        }
        
        ans += "Sopa de Letras\n\t0\t1\t2\t3\t4\t5\t6\t7\t8\t9\t10\t11\t12\t13\t14\t15\n";
        for(int i = 0; i < 16; i++){
            
            ans += i + "\t";
            
            for(int j = 0; j < 16; j++){
                
                if(marked[i][j] == 1)
                    ans += Character.toUpperCase(letterSoup[i][j]) + "\t";
                else
                    ans += letterSoup[i][j] + "\t";
            }
            ans += "\n";
        }
        
        ans += "Palabras encontradas " + score + "\nIntroduzca las coordenadas de la casilla de inicio de la palabra (fila y columna), el numero que represeta su direccion\n"
                + "0 ↓\t1 ↘\t2 →\t3 ↗\t4 ↑\t5 ↖\t6 ←\t7 ↙"
                + "\ny la longitud de la palabra encontrada.\nEj:\n0 5 1 4\n";
        
        return ans;
    }
    
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
                i += vertical_move, j += horizontal_move, k++){
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
                                    x += vertical_move, y += horizontal_move, z++){
                                visited[x][y] = 1;
                                letterSoup[x][y] = this.words[i].charAt(z);
                            }
                            
                            flag = 0;
                            
                            solutions[i] = "" + j + " " + k + " " + direction + " " + this.words[i].length() + "";
                            
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
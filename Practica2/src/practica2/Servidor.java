package practica2;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.Random;
import javafx.util.Pair;

public class Servidor {
    
    public static void main(String[] args) {
        
        long[] best_times = new long[3];
        best_times[0] = (Long.MAX_VALUE/1000);
        best_times[1] = (Long.MAX_VALUE/1000);
        best_times[2] = (Long.MAX_VALUE/1000);
        
        while(true){
            try{
                
                DatagramSocket ds = new DatagramSocket(1234);
                ds.setReuseAddress(true);
                
                DatagramPacket p = new DatagramPacket(new byte[65535], 65535);
                ds.receive(p);
                String confirmation = new String(p.getData(), 0, p.getLength());
                
                InetAddress dst = p.getAddress();
                int pto = p.getPort();

                if(confirmation.equals("1")){
                
                    System.out.println("El cliente se ha conectado al servidor desde "+ dst
                            + "en el puerto " + pto +"\nIniciando el juego");
                    
                    String option = "1";
                    
                    while(option.equals("1")){
                        System.out.println("Crando Sopa de letras");
                        ServidorHelper aux = new ServidorHelper();
                        Pair<char[][], String[]> container = aux.makeLetterSoup();
                        int[][] marked = new int[16][16];
                        char[][] letterSoup = container.getKey();
                        String[] solutions = container.getValue();
                        int score = 0;
                        Random chancer = new Random();
                        int dificulty = chancer.nextInt(3);
                        
                        System.out.println("Sopa de letras creada");
                        System.out.println("Las soluciones son: ");
                        
                        for(int i = 0; i < solutions.length; i++)
                            System.out.println(solutions[i]);
                        
                        System.out.println("Empieza a medir el tiempo");
                        
                        int mistakes = 0;
                        long start_time = System.currentTimeMillis();
                        
                        while(score < 15){
                            
                            String msj = aux.letterSoupToString(letterSoup, marked, score, dificulty, mistakes);
                            byte[] b1 = msj.getBytes();
                            DatagramPacket p1 = new DatagramPacket(b1, b1.length, dst, pto);
                            ds.send(p1);
                            System.out.println("Sopa de letras enviada con tama??o " + b1.length);

                            DatagramPacket p2 = new DatagramPacket(new byte[65535], 65535);
                            ds.receive(p2);
                            String answer = new String(p2.getData(), 0, p2.getLength());
                            System.out.println("La respuesta del usuario es: " + answer);

                            if(aux.checkAnswer(solutions, answer)){
                                marked = aux.marker(marked, answer);
                                score++;
                            }
                            else{
                                mistakes++;
                            }
                            
                            System.out.println("Puntuacion actual del usuario es: " + score);
                            
                            String msj2 = "1";
                            if(score == 15)
                                msj2 = "0";
                            byte[] b2 = msj2.getBytes();
                            DatagramPacket p3 =new DatagramPacket(b2, b2.length, dst, pto);
                            ds.send(p3);
                            
                        }

                        long end_time = System.currentTimeMillis();
                        
                        long total_time = end_time - start_time;
                        
                        if(best_times[2] > total_time){
                            best_times[2] = (total_time/1000);
                            Arrays.sort(best_times);
                        }
                        
                        String msj = "Tiempo Total: " + (total_time/1000) + " segundos\nMejores tiempos\n1 "
                                + best_times[0] + "\n2 "+ best_times[1] + "\n3 " + best_times[2]
                                + "\n??Volver a jugar?\n1 Si\n* No";
                        byte[] b1 = msj.getBytes();
                        DatagramPacket p1 =new DatagramPacket(b1, b1.length, dst, pto);
                        ds.send(p1);
                        
                        DatagramPacket p2 = new DatagramPacket(new byte[65535], 65535);
                        ds.receive(p2);
                        option = new String(p2.getData(), 0, p2.getLength());
                    }
                }
                
                ds.close();
            }
            catch(Exception e){
                e.printStackTrace();
            }
        }
    }
    
}
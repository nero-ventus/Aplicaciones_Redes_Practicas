package practica2;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Scanner;

public class Cliente {

    public static void main(String[] args) {
        try {
            Scanner sc = new Scanner(System.in);
            System.out.println("Escriba la direccion IP a conectarse o 0 para la predeterminada");
            String ip = sc.nextLine();
            
            System.out.println("Escriba el puerto conectarse");
            int pto = Integer.parseInt(sc.nextLine());
            
            
            InetAddress dst;
            
            if(ip.equals("0"))
                dst = InetAddress.getLocalHost();
            else
                dst = InetAddress.getByName(ip);
            
            DatagramSocket ds = new DatagramSocket();
            
            String confirmation = "1";
            byte[] b = confirmation.getBytes();
            DatagramPacket p = new DatagramPacket(b, b.length, dst, pto);
            ds.send(p);
            
            System.out.println("Conexion con el servidor establecida\nIniciando el juego");
            String option = "1";
            
            while(option.equals("1")){
                String is_not_done = "1";
                
                while(is_not_done.equals("1")){
                    
                    DatagramPacket p1 = new DatagramPacket(new byte[65535], 65535);
                    ds.receive(p1);
                    String letterSoup = new String(p1.getData(), 0, p1.getLength());
                    System.out.println(letterSoup);
                    
                    String answer = sc.nextLine();
                    byte[] b1 = answer.getBytes();
                    DatagramPacket p2 =new DatagramPacket(b1, b1.length, dst, pto);
                    ds.send(p2);
                    
                    DatagramPacket p3 = new DatagramPacket(new byte[65535], 65535);
                    ds.receive(p3);
                    is_not_done = new String(p3.getData(), 0, p3.getLength());
                }
                
                System.out.println("Felicidades, terminaste la sopa de letras!!!");
                
                DatagramPacket p1 = new DatagramPacket(new byte[65535], 65535);
                ds.receive(p1);
                String msj = new String(p1.getData(), 0, p1.getLength());
                System.out.println(msj);

                option = sc.nextLine();
                byte[] b1 = option.getBytes();
                DatagramPacket p2 =new DatagramPacket(b1, b1.length, dst, pto);
                ds.send(p2);
                
            }
            
            ds.close();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
}
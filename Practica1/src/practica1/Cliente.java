package practica1;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.net.Socket;
import javax.swing.JFileChooser;

public class Cliente {
    
    public static void main(String[] args) {
        try{
            Socket c1 = new Socket("localhost", 1234);
            DataInputStream dis = new DataInputStream(c1.getInputStream());
            DataOutputStream dos = new DataOutputStream(c1.getOutputStream());
            
            ClienteHelper aux = new ClienteHelper();
            System.out.println("Conexion establecida con el servidor\nBienvenido");
            
            int opcion = 1;
            
            while(opcion != 0){
                
                opcion = aux.menu(c1, dis);
                dos.writeInt(opcion);
                dos.flush();
                
                if(opcion == 1){
                    aux.mandarArchivosCliente(c1, dos);
                }
                else if(opcion != 0){
                    aux.recibirOpciones(dis, dos);
                }
                else if(opcion == 0){
                    opcion = dis.readInt();
                }
            }
            
            dos.close();
            dis.close();
            c1.close();
            System.out.println("Hasta Luego");
            
        }
        catch(Exception e){
            System.out.println(e.getMessage());
            System.out.println("Reintentado conectar con el servidor");
            main(args);
        }
    }
}
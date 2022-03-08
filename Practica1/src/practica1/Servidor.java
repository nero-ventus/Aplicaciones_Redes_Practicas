package practica1;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class Servidor {
    
    public static void main(String[] args) {
        try{
            ServerSocket ss = new ServerSocket(1234);
            ss.setReuseAddress(true);
            System.out.println("Servidor iniciado en el puerto: " + ss.getLocalPort());
            
            while(true){
                Socket c1 = ss.accept();
                c1.setTcpNoDelay(true);
                DataInputStream dis = new DataInputStream(c1.getInputStream());
                DataOutputStream dos = new DataOutputStream(c1.getOutputStream());
                System.out.println("Cliente conectado desde " + c1.getInetAddress() + ":" + c1.getPort());
                
                ServidorHelper aux = new ServidorHelper();
                int opc = 1;
                String relative_path = "";
                
                while(opc != 0){
                    opc = aux.recibirRespuesta(c1, dis, relative_path, dos);
                    
                    if(opc == 1){
                        aux.recibirArchivosServidor(c1, dis);
                    }
                    else if(opc != 0){
                        relative_path = aux.darOpciones(opc, relative_path, dis, dos);
                    }
                    
                }
                
                c1.close();
            }
        }
        catch(Exception e){
            System.out.println(e.getMessage());
        }
    }
}
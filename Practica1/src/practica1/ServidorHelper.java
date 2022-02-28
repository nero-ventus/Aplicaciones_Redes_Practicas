package practica1;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.Socket;

public class ServidorHelper {
    String pathActualServidor(){
        File f = new File("");
        return f.getAbsolutePath() + "\\Archivos_Servidor\\";
    }
    
    void recibirArchivosServidor(Socket c1, DataInputStream dis){
        
        try{
            int left = 0;
            String path = pathActualServidor();
            File carpeta = new File(path);
            carpeta.mkdirs();
            carpeta.setWritable(true);
            
            while(left != 1){
                
                left = dis.readInt();
                String relative_path = dis.readUTF();
                long tam = dis.readLong();
                
                System.out.println("Quedan " + left + " archivos por recibir");

                File destination_aux = new File(path + relative_path);
                System.out.println("Sadsaddasdasdsadasdasd      " + destination_aux.getParent());
                File destination = new File(destination_aux.getParent());
                destination.mkdirs();
                destination.setWritable(true);
                
                DataOutputStream dos = new DataOutputStream(new FileOutputStream(path + relative_path));

                long received = 0;

                while(received < tam){
                    byte[] b = new byte[1500];

                    int read = dis.read(b);
                    System.out.println("Leidos: " + read);

                    dos.write(b, 0, read);
                    dos.flush();

                    received += read;

                    int percent = (int)((received * 100) / tam);

                    System.out.println("Recivido el " + percent + "% del archivo");
                }

                dos.close();
                
            }
            
            System.out.println("Todos los archivos recividos");
        }
        catch(Exception e){
            e.printStackTrace();
        }
        
    }
}
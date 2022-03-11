package practica1;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

public class ServidorHelper {
    
    private Socket last_config;
    
    Socket modificarConexion(Socket c1, DataInputStream dis, DataOutputStream dos){
        this.last_config = c1;
        
        try{
            dos.writeUTF("Desea habilitar el algoritmo de Nagle?\n1 Si\n2 No\n");
            dos.flush();
            
            int ans = dis.readInt();
            
            if(ans == 1)
                c1.setTcpNoDelay(true);
            else
                c1.setTcpNoDelay(false);
            
            dos.writeUTF("Escriba el tamaño del buffer de escritura o -1 si no desea cambiarlo\n");
            dos.flush();
            
            ans = dis.readInt();
            
            if(ans > 0)
                c1.setSendBufferSize(ans);
            
            dos.writeUTF("Escriba el tamaño del buffer de lectura o -1 si no desea cambiarlo\n");
            dos.flush();
            
            ans = dis.readInt();
            
            if(ans > 0)
                c1.setReceiveBufferSize(ans);
            
            dos.writeUTF("Escriba el tiempo del temporalizador o -1 si no desea cambiarlo\n");
            dos.flush();
            
            ans = dis.readInt();
            
            if(ans > 0)
                c1.setSoTimeout(ans);
            
        }
        catch(Exception e){
            //e.printStackTrace();
        }
        
        this.last_config = c1;
        
        return this.last_config;
    }
    
    String pathActualServidor(){
        File f = new File("");
        return f.getAbsolutePath() + "\\Archivos_Servidor\\";
    }
    
    int recibirRespuesta(DataInputStream dis, String relative_path, DataOutputStream dos){
        
        int opc = 0;
        try{
            
            File auxiliar = new File(pathActualServidor() + relative_path);
            File[] listaArchivos = auxiliar.listFiles();

            String menu = "Seleccione una opcion\nOpcion 0: Salir\nOpcion 1: Modificar conexion\nOpcion 2: Subir archivos\n";

            for(int i = 3; i<listaArchivos.length + 3; i++){
                if(listaArchivos[i - 3].isFile())
                    menu += "Opcion "+ i +": "+ listaArchivos[i - 3].getName() + " -----> Archivo\n";
                else
                    menu += "Opcion "+ i +": "+ listaArchivos[i - 3].getName() + " -----> Carpeta\n"; 
            }
            
            dos.writeUTF(menu);
            dos.flush();
       
            opc = dis.readInt();
        }
        catch(Exception e){
            //e.printStackTrace();
        }
        
        return opc;
    }

    void recibirArchivosServidor(String relative_path_server){
        
        try{
            int left = 0;
            String path = pathActualServidor() + relative_path_server;
            File carpeta = new File(path);
            carpeta.mkdirs();
            carpeta.setWritable(true);
            
            ServerSocket ss = new ServerSocket(1235);
            ss.setReuseAddress(true);
            Socket c = ss.accept();
            DataInputStream dis = new DataInputStream(c.getInputStream());
            
            try{
                while(left != 1){

                    left = dis.readInt();
                    String relative_path = dis.readUTF();
                    long tam = dis.readLong();

                    System.out.println("Quedan " + left + " archivos por recibir");

                    File destination_aux = new File(path + relative_path);
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
            }
            catch(Exception e1){
                //e1.printStackTrace();
            }
            
            System.out.println("Todos los archivos recividos");
            
            dis.close();
            c.close();
            ss.close();
        }
        catch(Exception e){
            //e.printStackTrace();
        }
    }
    
    
    void eliminar(File current_file){
        try{
            System.out.println("Se eliminara " + current_file.getName());

            if(!current_file.isFile()){
                File[] listaArchivos = current_file.listFiles();

                for(int i = 0; i < listaArchivos.length; i++){
                    if(listaArchivos[i].isFile()){
                        listaArchivos[i].delete();
                    }
                    else{
                        eliminar(listaArchivos[i]);
                    }
                }
            }

            System.out.println("Eliminado " +current_file.delete());
        
        }
        catch(Exception e){
            //e.printStackTrace();
        }
        
        return;
    }   
    
    File[] getAllFiles(File[] original){
        ArrayList<File> container = new ArrayList<File>();
        Queue<File> q_aux = new LinkedList<File>();
        
        for(int i = 0; i < original.length; i++)
            q_aux.add(original[i]);
        
        while(q_aux.isEmpty() == false){
            File current_f = q_aux.poll();
            
            if(current_f.isFile()){
                container.add(current_f);
            }
            else{
                File[] folder_content = current_f.listFiles();
                
                for(int j = 0; j < folder_content.length; j++)
                    q_aux.add(folder_content[j]);
            }
        }
        
        File[] answer = new File[container.size()];
        
        for(int i = 0; i < container.size(); i++)
            answer[i] = container.get(i);
        
        return answer;
    }
    
    void enviarArchivoServidor(File[] container){
        File[] fs = getAllFiles(container);

        String parent_path = container[0].getParent();

        if(fs == null)
            return;

        try{
            
            ServerSocket ss = new ServerSocket(1235);
            ss.setReuseAddress(true);
            Socket c = ss.accept();
            DataOutputStream dos = new DataOutputStream(c.getOutputStream());
            
            try{
                for(int i = 0; i <fs.length; i++){
                    File current_f = fs[i];

                    String name = current_f.getName();
                    long tam = current_f.length();

                    System.out.println("Se enviara el archivo " + name + " de " + tam + "bytes");

                    DataInputStream dis = new DataInputStream(new FileInputStream(current_f));

                    String path = current_f.getAbsolutePath();
                    String base = parent_path;
                    String relative = new File(base).toURI().relativize(new File(path).toURI()).getPath();

                    dos.writeInt(fs.length - i);
                    dos.flush();
                    dos.writeUTF(relative);
                    dos.flush();
                    dos.writeLong(tam);
                    dos.flush();

                    long sent = 0;

                    while(sent < tam){
                        byte[] b = new byte[1500];

                        int read = dis.read(b);

                        System.out.println("Enviados: " + read);

                        dos.write(b, 0, read);
                        dos.flush();

                        sent += read;

                        int percent = (int) ((sent * 100) / tam);

                        System.out.println("Enviado el " + percent + "% del archivo");
                    }

                    System.out.println("Archivo enviado");

                    dis.close();
                }
            }
            catch(Exception e1){
                //e1.printStackTrace();
            }

            System.out.println("Todos los archivos enviados");

            dos.close();
            c.close();
            ss.close();
        }
        catch(Exception e){
            //e.printStackTrace();
        }
    }
    
    String darOpciones(int opc, String relative_path, DataInputStream dis, DataOutputStream dos){
        File auxiliar = new File(pathActualServidor() + relative_path);
        File[] listaArchivos = auxiliar.listFiles();
        File current_file = listaArchivos[opc - 3];
        
        String msj = "Que quieres hacer?\n1 Descargar\n2 Eliminar\n";
        
        if(!current_file.isFile()){
            msj += "3 Entrar\n";
        }
        
        try{
            dos.writeUTF(msj);
            dos.flush();
            
            int ans = dis.readInt();
            
            if(ans == 1){
                File[] container = new File[1];
                container[0] = current_file;
                enviarArchivoServidor(container);
            }
            if(ans == 2){
                System.out.println("Por eliminar");
                eliminar(current_file);
                System.out.println("Eliminado");
            }
            if(ans == 3){
                relative_path += current_file.getName() + "\\";
            }
        }
        catch(Exception e){
            //e.getStackTrace();
        }
        
        return relative_path;
    }
}
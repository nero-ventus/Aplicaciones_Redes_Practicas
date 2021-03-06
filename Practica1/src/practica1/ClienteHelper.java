package practica1;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;
import javax.swing.JFileChooser;

public class ClienteHelper {
    
    void modificarConexion(DataInputStream dis, DataOutputStream dos){
        
        try{
            Scanner sc = new Scanner(System.in);
            String msj = "";
            int ans = 0;
            
            for(int i = 0; i < 4 ; i++){
                msj = dis.readUTF();
                System.out.println(msj);

                ans = Integer.parseInt(sc.nextLine());
                dos.writeInt(ans);
                dos.flush();
            }
        }
        catch(Exception e){
            //e.printStackTrace();
        }
        
        return;
    }
    
    void menuLocal(String relative_path){
        File auxiliar = new File(pathActualCliente()+"\\"+ relative_path);
        File[] listaArchivos = auxiliar.listFiles();

        String menu = "Seleccione una opcion\nOpcion 0: Salir\n";

        for(int i = 1; i < listaArchivos.length + 1; i++){
            if(listaArchivos[i - 1].isFile())
                menu += "Opcion "+ i +": "+ listaArchivos[i - 1].getName() + " -----> Archivo\n";
            else
                menu += "Opcion "+ i +": "+ listaArchivos[i - 1].getName() + " -----> Carpeta\n"; 
        }
        
        System.out.println(menu);
        
        
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
    
    String opcionesLocales(String relative_path, int opc){
        File auxiliar = new File(pathActualCliente()+"\\"+ relative_path);
        File[] listaArchivos = auxiliar.listFiles();
        File current_file = listaArchivos[opc - 1];

        String msj = "Que quieres hacer?\n0 Nada\n1 Eliminar\n";

        if(!current_file.isFile()){
            msj += "2 Entrar\n";
        }
        
        System.out.println(msj);
        
        Scanner sc = new Scanner(System.in);
        int ans = Integer.parseInt(sc.nextLine());
        
        if(ans == 1){
            System.out.println("Por eliminar");
            eliminar(current_file);
            System.out.println("Eliminado");
        }
        if(ans == 2){
            relative_path += current_file.getName() + "\\";
        }
        
        return relative_path;
    }
    
    String pathActualCliente(){
        File f = new File("");
        return f.getAbsolutePath() + "\\Archivos_Cliente";
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
    
    int menu(DataInputStream dis){
        int opc = -1;

        try {
            String menu = dis.readUTF();
            System.out.println(menu);

            //BufferedReader br = new BufferedReader(new InputStreamReader(System.in,"ISO-8859-1"));//"Windows-1250"
            //opc = br.read();
            
            Scanner sc = new Scanner(System.in, "ISO-8859-1");
            opc = Integer.parseInt(sc.nextLine());
                        
        }
        catch(Exception e){
            //return menu(c1, dis);
            //e.printStackTrace();
        }

        return opc;
    }
    
    void mandarArchivosCliente(){

        try{
            Socket c = new Socket("localhost", 1235);
            DataOutputStream dos = new DataOutputStream(c.getOutputStream());

            try{
                JFileChooser jfc = new JFileChooser();

                jfc.setCurrentDirectory(new File(pathActualCliente()));
                jfc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
                jfc.setMultiSelectionEnabled(true);
                jfc.getActionMap().get("viewTypeDetails").actionPerformed(null);
                int r = jfc.showOpenDialog(null);
                jfc.setRequestFocusEnabled(true);

                if(r == JFileChooser.APPROVE_OPTION){
                    File[] container = jfc.getSelectedFiles();

                    File[] fs = getAllFiles(jfc.getSelectedFiles());

                    String parent_path = jfc.getSelectedFiles()[0].getParent();

                    if(fs == null)
                        return;

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

                    System.out.println("Todos los archivos enviados");
                }
            }
            catch(Exception e1){
                //e1.printStackTrace();
            }
            
            dos.close();
            c.close();
        }
        catch(Exception e){
            //e.printStackTrace();
        }
    } 
    
    void recibirArchivosCliente(){
        try{
            
            Socket c = new Socket("localhost", 1235);
            DataInputStream dis = new DataInputStream(c.getInputStream());
            
            try{
                int left = 0;
                String path = pathActualCliente() + "\\";
                File carpeta = new File(path);
                carpeta.mkdirs();
                carpeta.setWritable(true);

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
        }
        catch(Exception e){
            //e.printStackTrace();
        }
    }
    
    void recibirOpciones(DataInputStream dis, DataOutputStream dos){
        
        try{
            String msj = dis.readUTF();
            System.out.println(msj);
            
            Scanner sc = new Scanner(System.in, "ISO-8859-1");
            int ans = Integer.parseInt(sc.nextLine());
            
            dos.writeInt(ans);
            dos.flush();
            
            if(ans == 1){
                recibirArchivosCliente();
            }
        }
        catch(Exception e){
            //e.printStackTrace();
        }                
    }
}
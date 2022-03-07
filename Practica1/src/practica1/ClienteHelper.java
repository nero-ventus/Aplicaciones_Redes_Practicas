package practica1;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;
import javax.swing.JFileChooser;

public class ClienteHelper {
    
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
    
    int menu(Socket c1, DataInputStream dis){
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
            e.printStackTrace();
        }

        return opc;
    }
    
    void mandarArchivosCliente(Socket c1, DataOutputStream dos){
        
        JFileChooser jfc = new JFileChooser();
                    
        jfc.setCurrentDirectory(new File(pathActualCliente()));
        jfc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        jfc.setMultiSelectionEnabled(true);
        jfc.getActionMap().get("viewTypeDetails").actionPerformed(null);
        System.out.println("A4644");
        int r = jfc.showOpenDialog(null);
        System.out.println("A4644");
        jfc.setRequestFocusEnabled(true);

        if(r == JFileChooser.APPROVE_OPTION){
            File[] container = jfc.getSelectedFiles();
            
            File[] fs = getAllFiles(jfc.getSelectedFiles());

            String parent_path = jfc.getSelectedFiles()[0].getParent();

            if(fs == null)
                return;

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

                System.out.println("Todos los archivos enviados");

            }
            catch(Exception e){
                e.printStackTrace();
            }
        }
        
        
    }
}
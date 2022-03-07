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
                dos.write(opcion);
                
                if(opcion == 1){
                    JFileChooser jfc = new JFileChooser();
            
                    jfc.setCurrentDirectory(new File(aux.pathActualCliente()));
                    jfc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
                    jfc.setMultiSelectionEnabled(true);
                    jfc.getActionMap().get("viewTypeDetails").actionPerformed(null);
                    int r = jfc.showOpenDialog(null);
                    jfc.setRequestFocusEnabled(true);
                    
                    if(r == JFileChooser.APPROVE_OPTION){
                        File[] container = jfc.getSelectedFiles();
                        aux.mandarArchivosCliente(c1, dos, aux.getAllFiles(container), container[0].getParent());
                    }
                    
                }
                else if(opcion != 0){
                    
                }
                
                opcion = 0;
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
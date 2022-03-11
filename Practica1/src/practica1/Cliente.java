package practica1;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.Scanner;

public class Cliente {
    
    public static void main(String[] args) {
        int mode = 1;
        while(mode != 0){
            System.out.println("Seleecione una opcion\n0 Salir\n1 Archivos locales\n2 Archivos en linea");
            Scanner sc = new Scanner(System.in);
            mode = Integer.parseInt(sc.nextLine());
            
            if(mode == 1){
                String relative_path = "";
                int opc = 1;
                ClienteHelper aux = new ClienteHelper();
                
                while(opc != 0){
                    aux.menuLocal(relative_path);
                    
                    opc = Integer.parseInt(sc.nextLine());
                    
                    if(opc != 0){
                        relative_path = aux.opcionesLocales(relative_path, opc);
                    }
                    else{
                        
                        if(!relative_path.equals("")){
                            int conta = 1;
                            
                            for(int i = relative_path.length() - 2; i >= 0; i--){
                                if(relative_path.charAt(i) == 92){
                                    break;
                                }
                                conta++;
                            }
                            
                            //System.out.println(relative_path.length() + " " + conta);
                            
                            relative_path = relative_path.substring(0, relative_path.length() - conta);
                            
                            opc = 1;
                        }
                        
                    }
                    
                }
            }
            else if(mode == 2){
                
                try{
                    Socket c1 = new Socket("localhost", 1234);
                    DataInputStream dis = new DataInputStream(c1.getInputStream());
                    DataOutputStream dos = new DataOutputStream(c1.getOutputStream());

                    ClienteHelper aux = new ClienteHelper();
                    System.out.println("Conexion establecida con el servidor\nBienvenido");

                    int opcion = 1;

                    while(opcion != 0){

                        opcion = aux.menu(dis);
                        dos.writeInt(opcion);
                        dos.flush();

                        if(opcion == 1){
                            aux.modificarConexion(dis, dos);
                        }
                        else if(opcion == 2){
                            aux.mandarArchivosCliente();
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

                }
                catch(Exception e){
                    //System.out.println(e.getMessage());
                    System.out.println("Error imprevisto, volvierdo al menu");
                    main(args);
                }
            }
        }
        System.out.println("Hasta Luego");
    }
}
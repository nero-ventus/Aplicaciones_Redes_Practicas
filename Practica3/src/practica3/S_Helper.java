package practica3;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;

public class S_Helper {
    
    int pto = 9930,pto_dst = 9931;
    MulticastSocket s;
    InetAddress gpo;
    String lista_usuarios = "";
    
    void despliegaInfoNIC(NetworkInterface netint) throws SocketException {
        System.out.printf("Nombre de despliegue: %s\n", netint.getDisplayName());
        System.out.printf("Nombre: %s\n", netint.getName());
        String multicast = (netint.supportsMulticast())?"Soporta multicast":"No soporta multicast";
        System.out.printf("Multicast: %s\n", multicast);
        Enumeration<InetAddress> inetAddresses = netint.getInetAddresses();
        for (InetAddress inetAddress : Collections.list(inetAddresses)) {
            System.out.printf("Direccion: %s\n", inetAddress);
        }
        System.out.printf("\n");
    }
    
    void iniciaServer(){
        try{
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            
            Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
            
            int z=0;
            
            for(NetworkInterface netint : Collections.list(nets)){
                System.out.print("[Interfaz "+ ++z +"]:");
                despliegaInfoNIC(netint);
            }//for
            
            System.out.print("\nElige la interfaz multicast:");
            int interfaz = Integer.parseInt(br.readLine());
            //NetworkInterface ni = NetworkInterface.getByName("eth2");
            NetworkInterface ni = NetworkInterface.getByIndex(interfaz);
            br.close();
            System.out.println("\nElegiste "+ni.getDisplayName());
          
            this.s = new MulticastSocket(pto);
            this.s.setReuseAddress(true);
            this.s.setTimeToLive(255);
            
            this.gpo = InetAddress.getByName("230.1.1.1");
            //InetAddress gpo = InetAddress.getByName("ff3e:40:2001::1");
            SocketAddress dir;
            try{
                 dir = new InetSocketAddress(gpo, pto);
            }
            catch(Exception e){
              e.printStackTrace();
               return;
            }//catch
            s.joinGroup(dir, ni);
            //s.joinGroup(gpo);
            
        }
        catch(Exception e){
            e.printStackTrace();
        }
        
        Thread escucha = new Thread(new Runnable() {
                @Override
                public void run() {
                    try{
                        System.out.println("Servicio iniciado y unido al grupo.. comienza escucha de mensajes");
                        for(;;){
                            DatagramPacket p = new DatagramPacket(new byte[65535],65535);
                            s.receive(p);
                            
                            String username = new String(p.getData(), 0, p.getLength());
                            s.receive(p);
                            
                            String to = new String(p.getData(), 0, p.getLength());
                            s.receive(p);
                            
                            String msj_texto = new String(p.getData(), 0, p.getLength());
                            if(msj_texto.equals("000000000")){
                                
                                lista_usuarios = lista_usuarios.concat(username + "¿");
                                
                                enviaMensaje(username.getBytes());
                                enviaMensaje(("").getBytes());
                                enviaMensaje(("000000000").getBytes());
                                
                                enviaMensaje(lista_usuarios.getBytes());
                                
                                
                            }
                            else if(msj_texto.equals("000000001")){
                                s.receive(p);
                                
                                String msj_2 = new String(p.getData(), 0, p.getLength());
                                
                                enviaMensaje(username.getBytes());
                                enviaMensaje(to.getBytes());
                                enviaMensaje(("000000001").getBytes());
                                enviaMensaje(msj_2.getBytes());
                            }
                            else if(msj_texto.equals("000000002")){
                                
                                s.receive(p);
                                String tam_texto = new String(p.getData(), 0, p.getLength());
                                int tam = Integer.parseInt(tam_texto);
                
                                byte[] contenedor = new byte[tam];
                                int k = 0;

                                for(int i = 0; i < tam/1500 + 1; i++){
                                    
                                    s.receive(p);
                                            
                                    byte[] recibido = p.getData();

                                    for(int j = 0; j < 1500 && k < tam; j++){
                                        contenedor[k] = recibido[j];
                                        k++;
                                    }

                                }
                                
                                try(FileOutputStream fos = new FileOutputStream("Audio.wav")) {
                                    fos.write(contenedor);
                                    //fos.close(); There is no more need for this line since you had created the instance of "fos" inside the try. And this will automatically close the OutputStream
                                 }
                                
                                enviaMensaje(username.getBytes());
                                enviaMensaje(to.getBytes());
                                enviaMensaje(("000000002").getBytes());
                                enviaMensaje(tam_texto.getBytes());
                                
                                File archivo = new File("Audio.wav");
                
                                //enviaMensaje(String.valueOf(archivo.length()).getBytes());

                                contenedor = Files.readAllBytes(archivo.toPath());
                                k = 0;

                                for(int i = 0; i < archivo.length()/1500 + 1; i++){
                                    byte[] enviado = new byte[1500];

                                    for(int j = 0; j < 1500 && k < archivo.length(); j++){
                                        enviado[j] = contenedor[k];
                                        k++;
                                    }

                                    enviaMensaje(enviado);
                                }
                                
                            }
                            else if(msj_texto.equals("000000003")){
                                s.receive(p);
                                String tam_texto = new String(p.getData(), 0, p.getLength());
                                
                                s.receive(p);
                                String nombre_archivo = new String(p.getData(), 0, p.getLength());
                                
                                int tam = Integer.parseInt(tam_texto);
                
                                byte[] contenedor = new byte[tam];
                                int k = 0;

                                for(int i = 0; i < tam/1500 + 1; i++){
                                    
                                    s.receive(p);
                                            
                                    byte[] recibido = p.getData();

                                    for(int j = 0; j < 1500 && k < tam; j++){
                                        contenedor[k] = recibido[j];
                                        k++;
                                    }

                                }
                                
                                try(FileOutputStream fos = new FileOutputStream(nombre_archivo)) {
                                    fos.write(contenedor);
                                    //fos.close(); There is no more need for this line since you had created the instance of "fos" inside the try. And this will automatically close the OutputStream
                                 }
                                
                                enviaMensaje(username.getBytes());
                                enviaMensaje(to.getBytes());
                                enviaMensaje(("000000003").getBytes());
                                enviaMensaje(tam_texto.getBytes());
                                enviaMensaje(nombre_archivo.getBytes());
                                
                                File archivo = new File(nombre_archivo);
                
                                //enviaMensaje(String.valueOf(archivo.length()).getBytes());

                                contenedor = Files.readAllBytes(archivo.toPath());
                                k = 0;

                                for(int i = 0; i < archivo.length()/1500 + 1; i++){
                                    byte[] enviado = new byte[1500];

                                    for(int j = 0; j < 1500 && k < archivo.length(); j++){
                                        enviado[j] = contenedor[k];
                                        k++;
                                    }

                                    enviaMensaje(enviado);
                                }
                            }
                        }//for
                    }
                    catch(Exception e){
                        
                    }
                }
            });
            
            escucha.start();
        
    }
    
    void enviaMensaje(byte[] b){
        try{
            System.out.println("Se enviara el mensaje");
            DatagramPacket p = new DatagramPacket(b, b.length, this.gpo, this.pto_dst);
            this.s.send(p);
            System.out.println("Mensaje enviado con un ttl = " + this.s.getTimeToLive());
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }
}

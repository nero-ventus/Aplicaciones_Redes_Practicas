package Server;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.StringTokenizer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Manejador extends Thread {
    
    private ArrayList<alumno> lista_alumnos;
    private int PUERTO = 8000;
    private ServerSocket ss;

    public Manejador() throws Exception {
        System.out.println("Iniciando Servidor.......");
        this.ss = new ServerSocket(PUERTO);
        System.out.println("Servidor iniciado:---OK");
        System.out.println("Esperando por Cliente....");
        lista_alumnos = new ArrayList<alumno>();
        ExecutorService pool = Executors.newFixedThreadPool(100);
        for(;;){
            Socket accept = ss.accept();
            Thread hilo = new Thread(new Runnable() {
                Socket socket = accept;
                protected PrintWriter pw;
                protected BufferedOutputStream bos;
                protected BufferedReader br;
                DataOutputStream dos;
                DataInputStream dis;
                protected String FileName;
                
                @Override
                public void run() {
                    try{
                        //br=new BufferedReader(new InputStreamReader(socket.getInputStream()));
                        //bos=new BufferedOutputStream(socket.getOutputStream());
                        //pw=new PrintWriter(new OutputStreamWriter(bos));
                        dos = new DataOutputStream(socket.getOutputStream());
                        dis = new DataInputStream(socket.getInputStream());
                        //String line=br.readLine();
                        byte[] b = new byte[1024];
                        int t = dis.read(b);
                        String peticion = new String(b,0,t);
                        System.out.println("t: "+ t);
                        if(peticion == null) {
                                StringBuffer sb = new StringBuffer();
                                sb.append("<html><head><title>Servidor WEB\n");
                                sb.append("</title><body bgcolor=\"#AACCFF\"<br>Linea Vacia</br>\n");
                                sb.append("</body></html>\n");
                                dos.write(sb.toString().getBytes());
                                dos.flush();
                                socket.close();
                                return;
                        }
                        System.out.println("\nCliente Conectado desde: " + socket.getInetAddress());
                        System.out.println("Por el puerto: " + socket.getPort());
                        System.out.println("Datos: " + peticion + "\r\n\r\n");

                        StringTokenizer st1= new StringTokenizer(peticion,"\n");
                        String line = st1.nextToken();
                        System.out.println("Linea " + line);
                        if(!line.contains("?")){

                            getArch(line);
                            if(FileName == null || FileName.compareTo("") == 0) {
                                    SendA("WEB/index.html",dos);
                            }
                            else{
                                    SendA(FileName,dos);
                            }
                            //System.out.println(FileName);

                        }
                        else if(line.toUpperCase().startsWith("GET")) {
                            
                            StringTokenizer tokens=new StringTokenizer(line,"?");
                            String req_a=tokens.nextToken();
                            String req=tokens.nextToken();
                            System.out.println("Token1: "+req_a);
                            System.out.println("Token2: "+req);
                            String parametros = req.substring(0, req.indexOf(" "))+"\n";
                            System.out.println("parametros: "+parametros);
                            
                            int index = 0;
                            String boleta1 = "";
                            while(index < parametros.length() && parametros.charAt(index) != '=')
                                index++;
                            index++;
                            while(index < parametros.length() && parametros.charAt(index) != '\n'){
                                boleta1 = boleta1.concat(parametros.charAt(index) + "");
                                index++;
                            }
                            index++;
                            
                            alumno encontrado = buscarAlumno(Integer.parseInt(boleta1));
                            
                            StringBuffer respuesta = new StringBuffer();
                            respuesta.append("HTTP/1.1 200 Okay \n");
                            respuesta.append("Server: Rangel_Vera Server/1.1 \n");
                            String fecha = "Date: " + new Date()+" \n";
                            respuesta.append(fecha);
                            String tipo_mime = "Content-Type: text/html \n\n";
                            respuesta.append(tipo_mime);
                            respuesta.append("<html><head><title>SERVIDOR WEB</title></head>\n");
                            if(encontrado == null){
                                respuesta.append("<body bgcolor=\"#AACCFF\"><center><h1><br>\nEl alumno no esta registrado</h1><h3><b>\n");
                                respuesta.append("</b></h3>\n");
                                respuesta.append("</center></body></html>\n\n");
                            }
                            else{
                                respuesta.append("<body bgcolor=\"#AACCFF\"><center><h1><br>\nAlumno encontrado</h1><h3><b>\n");
                                respuesta.append("Nombre: " + encontrado.getNombre() + "\nBoleta: " + encontrado.getBoleta() + "\nGrupo: " + encontrado.getGrupo());
                                respuesta.append("</b></h3>\n");
                                respuesta.append("</center></body></html>\n\n");
                            }
                            System.out.println("Respuesta: "+respuesta);
                            dos.write(respuesta.toString().getBytes());
                            dos.flush();
                            dos.close();
                            //socket.close();

                        }
                        else if(line.toUpperCase().startsWith("POST")){
                            StringTokenizer tokens=new StringTokenizer(line,"?");
                            String req_a=tokens.nextToken();
                            String req=tokens.nextToken();
                            System.out.println("Token1: "+req_a);
                            System.out.println("Token2: "+req);
                            String parametros = req.substring(0, req.indexOf(" "))+"\n";
                            System.out.println("parametros: "+parametros);
                            
                            lista_alumnos.add(separarParametros(parametros, 0));
                            
                            StringBuffer respuesta = new StringBuffer();
                            respuesta.append("HTTP/1.1 200 Okay \n");
                            respuesta.append("Server: Rangel_Vera Server/1.1 \n");
                            String fecha = "Date: " + new Date()+" \n";
                            respuesta.append(fecha);
                            String tipo_mime = "Content-Type: text/html \n\n";
                            respuesta.append(tipo_mime);
                            respuesta.append("<html><head><title>SERVIDOR WEB</title></head>\n");
                            respuesta.append("<body bgcolor=\"#AACCFF\"><center><h1><br>\nAlumno Registrado</h1><h3><b>\n");
                            respuesta.append("</b></h3>\n");
                            respuesta.append("</center></body></html>\n\n");
                            System.out.println("Respuesta: "+respuesta);
                            dos.write(respuesta.toString().getBytes());
                            dos.flush();
                            dos.close();
                        }
                        else if(line.toUpperCase().startsWith("HEAD")){
                            StringTokenizer tokens=new StringTokenizer(line,"?");
                            String req_a=tokens.nextToken();
                            String req=tokens.nextToken();
                            System.out.println("Token1: "+req_a);
                            System.out.println("Token2: "+req);
                            String parametros = req.substring(0, req.indexOf(" "))+"\n";
                            System.out.println("parametros: "+parametros);
                            
                            /*int index = 0;
                            String boleta1 = "";
                            while(index < parametros.length() && parametros.charAt(index) != '=')
                                index++;
                            index++;
                            while(index < parametros.length() && parametros.charAt(index) != '\n'){
                                boleta1 = boleta1.concat(parametros.charAt(index) + "");
                                index++;
                            }
                            index++;
                            
                            alumno encontrado = buscarAlumno(Integer.parseInt(boleta1));*/
                            
                            StringBuffer respuesta = new StringBuffer();
                            respuesta.append("HTTP/1.1 200 Okay \n");
                            respuesta.append("Server: Rangel_Vera Server/1.1 \n");
                            String fecha = "Date: " + new Date()+" \n";
                            respuesta.append(fecha);
                            String tipo_mime = "Content-Type: text/html \n\n";
                            respuesta.append(tipo_mime);
                            /*respuesta.append("<html><head><title>SERVIDOR WEB</title></head>\n");
                            if(encontrado == null){
                                respuesta.append("<body bgcolor=\"#AACCFF\"><center><h1><br>\nEl alumno no esta registrado</h1><h3><b>\n");
                                respuesta.append("</b></h3>\n");
                                respuesta.append("</center></body></html>\n\n");
                            }
                            else{
                                respuesta.append("<body bgcolor=\"#AACCFF\"><center><h1><br>\nAlumno encontrado</h1><h3><b>\n");
                                respuesta.append("Nombre: " + encontrado.getNombre() + "\nBoleta: " + encontrado.getBoleta() + "\nGrupo: " + encontrado.getGrupo());
                                respuesta.append("</b></h3>\n");
                                respuesta.append("</center></body></html>\n\n");
                            }*/
                            System.out.println("Respuesta: "+respuesta);
                            dos.write(respuesta.toString().getBytes());
                            dos.flush();
                            dos.close();
                            //socket.close();
                        }
                        else if(line.toUpperCase().startsWith("PUT")){
                            StringTokenizer tokens=new StringTokenizer(line,"?");
                            String req_a=tokens.nextToken();
                            String req=tokens.nextToken();
                            System.out.println("Token1: "+req_a);
                            System.out.println("Token2: "+req);
                            String parametros = req.substring(0, req.indexOf(" "))+"\n";
                            System.out.println("parametros: "+parametros);
                            
                            int index = 0;
                            String boleta1 = "";
                            while(index < parametros.length() && parametros.charAt(index) != '=')
                                index++;
                            index++;
                            while(index < parametros.length() && parametros.charAt(index) != '&'){
                                boleta1 = boleta1.concat(parametros.charAt(index) + "");
                                index++;
                            }
                            index++;
                            
                            actualizarAlumno(Integer.parseInt(boleta1), separarParametros(parametros, index));
                            
                            StringBuffer respuesta = new StringBuffer();
                            respuesta.append("HTTP/1.1 200 Okay \n");
                            respuesta.append("Server: Rangel_Vera Server/1.1 \n");
                            String fecha = "Date: " + new Date()+" \n";
                            respuesta.append(fecha);
                            String tipo_mime = "Content-Type: text/html \n\n";
                            respuesta.append(tipo_mime);
                            respuesta.append("<html><head><title>SERVIDOR WEB</title></head>\n");
                            respuesta.append("<body bgcolor=\"#AACCFF\"><center><h1><br>\nAlumno Registrado</h1><h3><b>\n");
                            respuesta.append("</b></h3>\n");
                            respuesta.append("</center></body></html>\n\n");
                            System.out.println("Respuesta: "+respuesta);
                            dos.write(respuesta.toString().getBytes());
                            dos.flush();
                            dos.close();
                        }
                        else if(line.toUpperCase().startsWith("DELETE")){
                            
                            StringTokenizer tokens=new StringTokenizer(line,"?");
                            String req_a=tokens.nextToken();
                            String req=tokens.nextToken();
                            System.out.println("Token1: "+req_a);
                            System.out.println("Token2: "+req);
                            String parametros = req.substring(0, req.indexOf(" "))+"\n";
                            System.out.println("parametros: "+parametros);
                            
                            int index = 0;
                            String boleta1 = "";
                            while(index < parametros.length() && parametros.charAt(index) != '=')
                                index++;
                            index++;
                            while(index < parametros.length() && parametros.charAt(index) != '\n'){
                                boleta1 = boleta1.concat(parametros.charAt(index) + "");
                                index++;
                            }
                            index++;
                            
                            boolean eliminado = eliminarAlumno(Integer.parseInt(boleta1));
                            
                            StringBuffer respuesta = new StringBuffer();
                            respuesta.append("HTTP/1.1 200 Okay \n");
                            respuesta.append("Server: Rangel_Vera Server/1.1 \n");
                            String fecha = "Date: " + new Date()+" \n";
                            respuesta.append(fecha);
                            String tipo_mime = "Content-Type: text/html \n\n";
                            respuesta.append(tipo_mime);
                            respuesta.append("<html><head><title>SERVIDOR WEB</title></head>\n");
                            if(eliminado){
                                respuesta.append("<body bgcolor=\"#AACCFF\"><center><h1><br>\nAlumno eliminado con exito</h1><h3><b>\n");
                                respuesta.append("</b></h3>\n");
                                respuesta.append("</center></body></html>\n\n");
                            }
                            else{
                                respuesta.append("<body bgcolor=\"#AACCFF\"><center><h1><br>\nAlumno no registrado</h1><h3><b>\n");
                                respuesta.append("</b></h3>\n");
                                respuesta.append("</center></body></html>\n\n");
                            }
                            System.out.println("Respuesta: "+respuesta);
                            dos.write(respuesta.toString().getBytes());
                            dos.flush();
                            dos.close();
                            //socket.close();
                        }
                        else{
                                dos.write("HTTP/1.1 501 Not Implemented\r\n".getBytes());
                                dos.flush();
                                dos.close();
                                //socket.close();
                                //pw.println();
                        }
                                //dos.flush();
                                //bos.flush();
                        socket.close();
                    }
                    catch(Exception e){
                        
                        StringBuffer respuesta = new StringBuffer();
                        respuesta.append("HTTP/1.1 500 Error \n");
                        respuesta.append("Server: Rangel_Vera Server/1.1 \n");
                        String fecha = "Date: " + new Date()+" \n";
                        respuesta.append(fecha);
                        String tipo_mime = "Content-Type: text/html \n\n";
                        respuesta.append(tipo_mime);
                        
                        try{
                            dos = new DataOutputStream(socket.getOutputStream());
                            dos.write(respuesta.toString().getBytes());
                            dos.flush();
                            dos.close();
                        }
                        catch(Exception ex){
                            
                        }
                    }
                }
                public void getArch(String line){
                    int i;
                    int f;
                    if(line.toUpperCase().startsWith("GET")){
                        i=line.indexOf("/");
                        f=line.indexOf(" ",i);
                        FileName = line.substring(i+1,f);
                    }
                }

                public void SendA(String fileName,Socket sc,DataOutputStream dos) {
                    //System.out.println(fileName);
                    int fSize = 0;
                    byte[] buffer = new byte[4096];
                    try{
                        //DataOutputStream out =new DataOutputStream(sc.getOutputStream());

                        //sendHeader();
                        DataInputStream dis1 = new DataInputStream(new FileInputStream(fileName));
                        //FileInputStream f = new FileInputStream(fileName);
                        int x = 0;
                        File ff = new File("fileName");
                        long tam, cont=0;
                        tam = ff.length();
                        while(cont<tam){
                            x = dis1.read(buffer);
                            dos.write(buffer,0,x);
                            cont =cont+x;
                            dos.flush();
                        }
                        //out.flush();
                        dis.close();
                        dos.close();
                    }
                    catch(FileNotFoundException e){
                            //msg.printErr("Transaction::sendResponse():1", "El archivo no existe: " + fileName);
                    }
                    catch(IOException e){
            //			System.out.println(e.getMessage());
                            //msg.printErr("Transaction::sendResponse():2", "Error en la lectura del archivo: " + fileName);
                    }
                }

                public void SendA(String arg, DataOutputStream dos1) {
                    try{
                        int b_leidos=0;
                        DataInputStream dis2 = new DataInputStream(new FileInputStream(arg));
                        // BufferedInputStream bis2=new BufferedInputStream(new FileInputStream(arg));
                        byte[] buf=new byte[1024];
                        int x=0;
                        File ff = new File(arg);			
                        long tam_archivo=ff.length(),cont=0;
                        /***********************************************/
                        String sb = "";
                        sb = sb+"HTTP/1.1 200 ok\n";
                        sb = sb +"Server: Rangel_Vera Server/1.1 \n";
                        sb = sb +"Date: " + new Date()+" \n";
                        sb = sb +"Content-Type: text/html \n";
                        sb = sb +"Content-Length: "+tam_archivo+" \n";
                        sb = sb +"\n";
                        dos1.write(sb.getBytes());
                        dos1.flush();
                        /***********************************************/

                        while(cont<tam_archivo){
                            x = dis2.read(buf);
                            dos1.write(buf,0,x);
                            cont=cont+x;
                            dos1.flush();
                        }
                        //bos.flush();
                        dis2.close();
                        dos1.close();
                    }
                    catch(Exception e){
                        System.out.println(e.getMessage());
                    }

                }
                
                public alumno separarParametros(String parametros, int index){
                            
                    String nombre1 = "";
                    while(index < parametros.length() && parametros.charAt(index) != '=')
                        index++;
                    index++;
                    while(index < parametros.length() && parametros.charAt(index) != '&'){
                        nombre1 = nombre1.concat(parametros.charAt(index) + "");
                        index++;
                    }
                    index++;

                    String boleta1 = "";
                    while(index < parametros.length() && parametros.charAt(index) != '=')
                        index++;
                    index++;
                    while(index < parametros.length() && parametros.charAt(index) != '&'){
                        boleta1 = boleta1.concat(parametros.charAt(index) + "");
                        index++;
                    }
                    index++;

                    String grupo1 = "";
                    while(index < parametros.length() && parametros.charAt(index) != '=')
                        index++;
                    index++;
                    while(index < parametros.length() && parametros.charAt(index) != '&'){
                        grupo1 = grupo1.concat(parametros.charAt(index) + "");
                        index++;
                    }
                    index++;
                    
                    /*System.out.println(nombre1);
                    System.out.println(boleta1);
                    System.out.println(grupo1);*/
                    
                    return new alumno(nombre1, Integer.parseInt(boleta1), grupo1);
                }
                
                public alumno buscarAlumno(int boleta){
                    for(int i = 0; i < lista_alumnos.size(); i++)
                        if(lista_alumnos.get(i).getBoleta() == boleta)
                            return lista_alumnos.get(i);
                    
                    return null;
                }
                
                public boolean eliminarAlumno(int boleta){
                    for(int i = 0; i < lista_alumnos.size(); i++)
                        if(lista_alumnos.get(i).getBoleta() == boleta){
                            lista_alumnos.remove(i);
                            return true;
                        }
                    
                    return false;
                }
                
                public void actualizarAlumno(int boleta, alumno actualizado){
                    for(int i = 0; i < lista_alumnos.size(); i++)
                        if(lista_alumnos.get(i).getBoleta() == boleta){
                            lista_alumnos.set(i, actualizado);
                            return;
                        }
                    
                    lista_alumnos.add(actualizado);
                    return;
                }
                
            });
            pool.execute(hilo);
        }
    }
}
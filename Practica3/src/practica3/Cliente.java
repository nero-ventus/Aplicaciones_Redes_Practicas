package practica3;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
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
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class Cliente extends JFrame implements ActionListener, ItemListener{
    
    int pto = 9931,pto_dst = 9930;
    InetAddress gpo;
    MulticastSocket cl;
    private JPanel Lamina_Sup;
    private JPanel Lamina_inf;
    private JTextArea chat;
    private JTextField mensaje;
    private JButton enviar;
    private JButton audio_enviar;
    private String username;
    private JComboBox<String> usuarios_destino;
    private String enviar_a;
    private JButton enviar_archivo;
    private HashMap<String, String> emoticones;
    
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
    
    void iniciaCliente(){
        try{
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
            int z=0;
            
            for (NetworkInterface netint : Collections.list(nets)){
                System.out.print("[Interfaz "+ ++z +"]:");
                despliegaInfoNIC(netint);
            }//for
            
            System.out.print("\nElige la interfaz multicast:");
            int interfaz = Integer.parseInt(br.readLine());
            //NetworkInterface ni = NetworkInterface.getByName("eth2");
            NetworkInterface ni = NetworkInterface.getByIndex(interfaz);
            System.out.println("\nElegiste "+ni.getDisplayName());

            System.out.println("Escribe tu nombre de usuario:");
            this.username = br.readLine();
          
            br.close();    
                
            this.cl = new MulticastSocket(this.pto);    
            //System.out.println(cl.getSendBufferSize());
            this.cl.setReuseAddress(true);
            this.gpo = InetAddress.getByName("230.1.1.1");
            //InetAddress gpo = InetAddress.getByName("ff3e:40:2001::1");
            SocketAddress dir;
            try{
                dir = new InetSocketAddress(this.gpo,this.pto);
            }
            catch(Exception e){
                System.err.println("Sintaxis: java UDPEchoClient host [port]");
                return;
            }//catch
          
            this.cl.joinGroup(dir, ni);
            //cl.joinGroup(gpo);
            
            Thread escucha = new Thread(new Runnable() {
                @Override
                public void run() {
                    try{
                        System.out.println("Servicio iniciado y unido al grupo.. comienza escucha de mensajes");
                        for(;;){
                            DatagramPacket p = new DatagramPacket(new byte[65535],65535);
                            
                            cl.receive(p);
                            String usuario = new String(p.getData(), 0, p.getLength());
                            
                            cl.receive(p);
                            String to = new String(p.getData(), 0, p.getLength());
                            
                            cl.receive(p);
                            System.out.println("Datagrama multicast recibido desde "+p.getAddress()+":"+p.getPort()+"Con el mensaje:"+new String(p.getData(),0,p.getLength()));
                            
                            String msj_texto = new String(p.getData(), 0, p.getLength());
                            if(msj_texto.equals("000000000")){
                                cl.receive(p);
                                String lista_usuarios = new String(p.getData(), 0, p.getLength());
                                
                                usuarios_destino.removeAllItems();
                                usuarios_destino.addItem("Todos");
                                
                                int i = 0;
                                while(i < lista_usuarios.length()){
                                    int j = i;
                                    String usuario_nuevo = "";
                                    while(j < lista_usuarios.length() && lista_usuarios.charAt(j) != '¿'){
                                        usuario_nuevo = usuario_nuevo.concat(lista_usuarios.charAt(j) + "");
                                        j++;
                                    }
                                    
                                    System.out.println("Usuario nuevo: " + usuario_nuevo);
                                    
                                    usuarios_destino.addItem(usuario_nuevo);
                                    
                                    i = j;
                                    i++;
                                }
                                
                            }
                            else if(msj_texto.equals("000000001")){
                                cl.receive(p);
                                
                                String mensaje_emoticones = ponerEmojis(new String(p.getData(),0,p.getLength()));
                                
                                if(to.equals("Todos")){
                                    String current_chat = chat.getText();
                                    current_chat = current_chat.concat("\n" + mensaje_emoticones);
                                    chat.setText(current_chat);
                                }
                                else if(to.equals(username) || usuario.equals(username)){
                                    String current_chat = chat.getText();
                                    current_chat = current_chat.concat("\nPrivado>" + mensaje_emoticones);
                                    chat.setText(current_chat);
                                }
                            }
                            else if(msj_texto.equals("000000002")){
                                
                                cl.receive(p);
                                String tam_texto = new String(p.getData(), 0, p.getLength());
                                int tam = Integer.parseInt(tam_texto);
                
                                byte[] contenedor = new byte[tam];
                                int k = 0;

                                for(int i = 0; i < tam/1500 + 1; i++){
                                    
                                    cl.receive(p);
                                            
                                    byte[] recibido = p.getData();

                                    for(int j = 0; j < 1500 && k < tam; j++){
                                        contenedor[k] = recibido[j];
                                        k++;
                                    }

                                }
                                
                                if(to.equals("Todos") || to.equals(username) || usuario.equals(username)){
                                    
                                    try(FileOutputStream fos = new FileOutputStream("Audio.wav")) {
                                        fos.write(contenedor);
                                        //fos.close(); There is no more need for this line since you had created the instance of "fos" inside the try. And this will automatically close the OutputStream
                                     }
                                    
                                    JOptionPane.showMessageDialog(null, "Se recibio una nota de voz de " + usuario);
                                    /*Audio grabadora = new Audio();
                                    grabadora.playSound();*/

                                    Thread reproduceAudio = new Thread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Audio grabadora = new Audio();
                                            grabadora.playSound();
                                        }
                                    });
                                    reproduceAudio.start();
                                }
                            }
                            else if(msj_texto.equals("000000003")){
                                cl.receive(p);
                                String tam_texto = new String(p.getData(), 0, p.getLength());
                                
                                cl.receive(p);
                                String nombre_archivo = new String(p.getData(), 0, p.getLength());
                                
                                
                                int tam = Integer.parseInt(tam_texto);
                
                                byte[] contenedor = new byte[tam];
                                int k = 0;

                                for(int i = 0; i < tam/1500 + 1; i++){
                                    
                                    cl.receive(p);
                                            
                                    byte[] recibido = p.getData();

                                    for(int j = 0; j < 1500 && k < tam; j++){
                                        contenedor[k] = recibido[j];
                                        k++;
                                    }

                                }
                                
                                if(to.equals("Todos") || to.equals(username)){
                                    
                                    try(FileOutputStream fos = new FileOutputStream(nombre_archivo)) {
                                        fos.write(contenedor);
                                        //fos.close(); There is no more need for this line since you had created the instance of "fos" inside the try. And this will automatically close the OutputStream
                                    }
                                    
                                    
                                    JOptionPane.showMessageDialog(null, "Se recibio de "+ usuario +" el archivo: " + nombre_archivo);
                                    
                                    Desktop.getDesktop().open(new File(nombre_archivo));
                                    
                                }
                            }

                        }//for
                    }
                    catch(Exception e){
                        
                    }
                }
            });
            
            escucha.start();
          
            
            //Registarse en la red
            
            enviaMensaje(username.getBytes());
            enviaMensaje(("").getBytes());
            enviaMensaje(("000000000").getBytes());
            
            //Fin de registro
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }
    
    public Cliente(){
        Lamina_Sup = new JPanel();
        Lamina_inf = new JPanel();
        
        add(Lamina_Sup, BorderLayout.NORTH);
        add(Lamina_inf, BorderLayout.SOUTH);
        
        chat = new JTextArea("Chat\n");
        chat.setSize(900, 350);
        chat.setEnabled(false);
        Lamina_Sup.add(chat);
        
        mensaje = new JTextField(60);
        Lamina_inf.add(mensaje);
        
        enviar = new JButton("Enviar");
        enviar.addActionListener(this);
        Lamina_inf.add(enviar);
        
        audio_enviar = new JButton("Nota de voz");
        audio_enviar.addActionListener(this);
        Lamina_inf.add(audio_enviar);
        
        enviar_archivo = new JButton("Enviar archivo");
        enviar_archivo.addActionListener(this);
        Lamina_inf.add(enviar_archivo);
        
        usuarios_destino = new JComboBox<String>();
        usuarios_destino.addItem("Todos");
        usuarios_destino.addItemListener(this);
        Lamina_inf.add(usuarios_destino);
        
        
        enviar_a = "Todos";
        emoticones = new HashMap<String, String>();
        emoticones.put(":)", "😀");
        emoticones.put(";)", "😉");
        emoticones.put("XD", "😆");
        emoticones.put(":p", "😛");
        emoticones.put("._.", "😐");
        emoticones.put("-_-", "😑");
        emoticones.put("B)", "😎");
        emoticones.put(":/", "😕");
        emoticones.put(":'(", "😢");
        emoticones.put("<3", "❤");
        emoticones.put("</3", "💔");
        
        iniciaCliente();
        
        this.setTitle("Chat de " + username);
    }
    
    
    String ponerEmojis(String original){
        
        String answer = "";
        
        int i = 0;
        while(i < original.length()){
            int j = i;
            String current_word = "";
            
            while(j < original.length() && original.charAt(j) != ' '){
                current_word = current_word.concat(original.charAt(j) + "");
                j++;
            }
            
            i = j;
            i++;
            
            if(emoticones.containsKey(current_word)){
                answer = answer.concat(emoticones.get(current_word) + " ");
            }
            else{
                answer = answer.concat(current_word + " ");
            }
            
        }
        
        return answer;
    }
    
    void enviaMensaje(byte[] b){
        try{
            System.out.println("Se enviara el mensaje");
            DatagramPacket p = new DatagramPacket(b, b.length, this.gpo, this.pto_dst);
            this.cl.send(p);
            System.out.println("Mensaje enviado con un ttl = " + this.cl.getTimeToLive());
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }
    
    public static void main(String[] args) {
        JFrame f = new Cliente();
        f.setSize(1200, 500); 
        f.setVisible(true);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try{
            
            enviaMensaje(username.getBytes());
            enviaMensaje(enviar_a.getBytes());
            
            if(e.getSource().equals(enviar)){
                enviaMensaje(("000000001").getBytes());
                enviaMensaje((username + "> " + mensaje.getText()).getBytes());
                mensaje.setText("");
            }
            else if(e.getSource().equals(audio_enviar)){
                JOptionPane.showMessageDialog(null, "Tiene 5 segundos para hacer su mensaje");
                enviaMensaje(("000000002").getBytes());
                Audio grabadora = new Audio();
                grabadora.Graba();
                
                File archivo = new File("Audio.wav");
                
                enviaMensaje(String.valueOf(archivo.length()).getBytes());
                
                byte[] contenedor = Files.readAllBytes(archivo.toPath());
                int k = 0;
                
                for(int i = 0; i < archivo.length()/1500 + 1; i++){
                    byte[] enviado = new byte[1500];
                    
                    for(int j = 0; j < 1500 && k < archivo.length(); j++){
                        enviado[j] = contenedor[k];
                        k++;
                    }
                    
                    enviaMensaje(enviado);
                }
            }
            else if(e.getSource().equals(enviar_archivo)){
                JFileChooser jfc = new JFileChooser();

                jfc.setCurrentDirectory(new File(""));
                jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
                jfc.setMultiSelectionEnabled(false);
                jfc.getActionMap().get("viewTypeDetails").actionPerformed(null);
                int r = jfc.showOpenDialog(null);
                jfc.setRequestFocusEnabled(true);

                if(r == JFileChooser.APPROVE_OPTION){
                    JOptionPane.showMessageDialog(null, "El archivo seleccionado sera enviado");
                    enviaMensaje(("000000003").getBytes());

                    File archivo = jfc.getSelectedFile();

                    enviaMensaje(String.valueOf(archivo.length()).getBytes());
                    enviaMensaje(archivo.getName().getBytes());

                    byte[] contenedor = Files.readAllBytes(archivo.toPath());
                    int k = 0;

                    for(int i = 0; i < archivo.length()/1500 + 1; i++){
                        byte[] enviado = new byte[1500];

                        for(int j = 0; j < 1500 && k < archivo.length(); j++){
                            enviado[j] = contenedor[k];
                            k++;
                        }

                        enviaMensaje(enviado);
                    }
                }
            }
        }
        catch(Exception ex){
            
        }
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        enviar_a = (String) usuarios_destino.getSelectedItem();
    }
}
package practica8;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.SocketAddress;
import java.util.Scanner;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class GUI extends JFrame implements ActionListener{
    
    private JTextField nombre_busqueda;
    private JButton boton_buscar;
    private JTextArea servidores_activos;
    private JTextArea archivos_descargados;
    private JTextArea resultados_busqueda;
    private JTextField numero_descargar;
    private JButton boton_descargar;
    private JPanel Lamina_Sup;
    private JPanel Lamina_inf;
    private JPanel Lamina_int1;
    private JPanel Lamina_int2;
    private JPanel Lamina_int3;
    private int puertoMulti;
    private int puertoMutltiDst;
    InetAddress gpo;
    MulticastSocket cl;
    
    public GUI(){
        
        iniciaClienteMulticast();
        RMIHelper rmiaux = new RMIHelper();
        rmiaux.iniciaServerRMI();
        
        this.Lamina_Sup = new JPanel();
        this.Lamina_inf = new JPanel();

        this.Lamina_int1 = new JPanel();
        this.Lamina_int2 = new JPanel();
        this.Lamina_int3 = new JPanel();
        
        add(this.Lamina_Sup, BorderLayout.NORTH);
        add(this.Lamina_inf, BorderLayout.SOUTH);
        
        this.Lamina_Sup.add(this.Lamina_int1, BorderLayout.WEST);
        this.Lamina_Sup.add(this.Lamina_int2, BorderLayout.CENTER);
        this.Lamina_Sup.add(this.Lamina_int3, BorderLayout.EAST);
        
        this.servidores_activos = new JTextArea("Servidores activos\n");
        this.servidores_activos.setEnabled(false);
        this.Lamina_int1.add(this.servidores_activos);
        
        this.archivos_descargados = new JTextArea("Archivos descargados\n");
        this.archivos_descargados.setEnabled(false);
        this.Lamina_int2.add(this.archivos_descargados);
        
        this.resultados_busqueda = new JTextArea("Resultados de busqueda\n");
        this.resultados_busqueda.setEnabled(false);
        this.Lamina_int3.add(this.resultados_busqueda);
        
        this.nombre_busqueda = new JTextField(30);
        this.Lamina_inf.add(this.nombre_busqueda);
        
        this.boton_buscar = new JButton("Buscar");
        this.boton_buscar.addActionListener(this);
        this.Lamina_inf.add(this.boton_buscar);
        
        this.numero_descargar = new JTextField(10);
        this.Lamina_inf.add(this.numero_descargar);
        
        this.boton_descargar = new JButton("Descargar");
        this.boton_descargar.addActionListener(this);
        this.Lamina_inf.add(this.boton_descargar);
        
        this.setTitle("Aplicacion P2P");
    }
    
    void iniciaClienteMulticast(){
        try{
            Scanner sc = new Scanner(System.in);
            System.out.println("Elige tu puerto de conexcion multicast");
            this.puertoMulti = Integer.parseInt(sc.nextLine());
            this.puertoMutltiDst = 9930;
            
            NetworkInterface ni = NetworkInterface.getByIndex(1);
                
            this.cl = new MulticastSocket(this.puertoMulti);    
            this.cl.setReuseAddress(true);
            this.gpo = InetAddress.getByName("228.1.1.1");
            SocketAddress dir;
            try{
                dir = new InetSocketAddress(this.gpo,this.puertoMulti);
            }
            catch(Exception e){
                System.err.println("Sintaxis: java UDPEchoClient host [port]");
                return;
            }
          
            this.cl.joinGroup(dir, ni);
            
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

                        cl.receive(p);
                        String puertosTexto = new String(p.getData(), 0, p.getLength());
                        if(puertosTexto != null)
                            servidores_activos.setText(puertosTexto);
                    }
                }
                catch(Exception e){
                    e.printStackTrace();
                }
            }
        });
        escucha.start();
        
        Thread envia = new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    while(true){
                        String mensaje = "0";

                        enviaMensaje(mensaje.getBytes());

                        Thread.sleep(1000);
                    }
                }
                catch(Exception e){
                    e.printStackTrace();
                }
            }
            
            void enviaMensaje(byte[] b){
                    try{
                        System.out.println("Se enviara el mensaje");
                        DatagramPacket p = new DatagramPacket(b, b.length, gpo, puertoMutltiDst);
                        cl.send(p);
                        System.out.println("Mensaje enviado con un ttl = " + cl.getTimeToLive());
                    }
                    catch(Exception e){
                        e.printStackTrace();
                    }
                }
            
        });
        envia.start();
        
    }
    
    

    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource() == boton_buscar){
            RMIHelper rmiaux = new RMIHelper();
            resultados_busqueda.setText("Resultados de la busqueda:\n" + rmiaux.clienteRMI(nombre_busqueda.getText()));
            nombre_busqueda.setText("");
        }
    }

}
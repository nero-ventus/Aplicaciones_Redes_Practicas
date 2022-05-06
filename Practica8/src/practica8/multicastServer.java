package practica8;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.SocketAddress;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;

public class multicastServer {

    private int pto = 9930;
    private Hashtable<Integer, Long> puertos_conectados;
    private Hashtable<Integer, InetAddress> ip_conectadas;
    private MulticastSocket s;
    private InetAddress gpo;

    void iniciaServidor() {
        puertos_conectados = new Hashtable<>();
        ip_conectadas = new Hashtable<>();
        try {
            NetworkInterface ni = NetworkInterface.getByIndex(1);

            this.s = new MulticastSocket(pto);
            this.s.setReuseAddress(true);
            this.s.setTimeToLive(255);

            this.gpo = InetAddress.getByName("228.1.1.1");

            SocketAddress dir;
            try {
                dir = new InetSocketAddress(gpo, pto);
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
            s.joinGroup(dir, ni);

        } catch (Exception e) {
            e.printStackTrace();
        }

        Thread escucha = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    System.out.println("Servicio iniciado y unido al grupo.. comienza escucha de mensajes");
                    for (;;) {
                        DatagramPacket p = new DatagramPacket(new byte[65535], 65535);

                        s.receive(p);
                        String codigo = new String(p.getData(), 0, p.getLength());

                        if (codigo.equals("0")) {
                            System.out.println(p.getAddress());
                            puertos_conectados.put(p.getPort(), new Date().getTime());
                            ip_conectadas.put(p.getPort(), p.getAddress());
                        } else if (codigo.equals("1")) {
                            s.receive(p);
                            String nombre_buscar = new String(p.getData(), 0, p.getLength());
                            Enumeration<Integer> e = puertos_conectados.keys();

                            while (e.hasMoreElements()) {
                                int puerto = e.nextElement();
                                System.out.println("Envia A");
                                enviaMensaje("1".getBytes(), puerto);
                                enviaMensaje(nombre_buscar.getBytes(), puerto);
                            }
                        }
                    }//for
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            void enviaMensaje(byte[] b, int pto_dst) {
                try {
                    System.out.println("Se enviara el mensaje");
                    DatagramPacket p = new DatagramPacket(b, b.length, gpo, pto_dst);
                    s.send(p);
                    System.out.println("Mensaje enviado con un ttl = " + s.getTimeToLive());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        escucha.start();

        Thread envia = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (true) {
                        String mensaje = puertosAString();

                        Enumeration<Integer> e = puertos_conectados.keys();

                        System.out.println("Envia B " + mensaje);
                        while (e.hasMoreElements()) {
                            int puerto = e.nextElement();
                            enviaMensaje(mensaje.getBytes(), puerto);
                        }

                        Thread.sleep(5000);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            public String puertosAString() {

                String respuesta = "Servidores Activos\n";
                Enumeration<Integer> e = puertos_conectados.keys();

                while (e.hasMoreElements()) {
                    int puerto = e.nextElement();
                    long ultima_conex = puertos_conectados.get(puerto);
                    long diferencia = new Date().getTime() - ultima_conex;

                    if (diferencia <= 5000) {
                        respuesta = respuesta.concat(ip_conectadas.get(puerto) + ": " + puerto + " Temporalizador " + (int) (6 - diferencia / 1000) + "\n");
                    }

                }

                return respuesta;
            }

            void enviaMensaje(byte[] b, int pto_dst) {
                try {
                    System.out.println("Se enviara el mensaje");
                    DatagramPacket p = new DatagramPacket(b, b.length, gpo, pto_dst);
                    s.send(p);
                    System.out.println("Mensaje enviado con un ttl = " + s.getTimeToLive());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        envia.start();
    }
}

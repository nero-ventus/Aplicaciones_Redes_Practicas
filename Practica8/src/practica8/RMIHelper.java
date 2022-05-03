package practica8;

import java.io.File;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class RMIHelper implements Buscador{
    void iniciaServerRMI(){
        try{
            java.rmi.registry.LocateRegistry.createRegistry(1099); //puerto default del rmiregistry
            System.out.println("RMI registry ready.");
	}
        catch(Exception e) {
            System.out.println("Exception starting RMI registry:");
            e.printStackTrace();
	  }
	
	try{
            File aux = new File("");
            String full_path = aux.getAbsolutePath() + "build\\classes\\practica8";
            
            System.setProperty("java.rmi.server.codebase","file:" + full_path); ///file:///f:\\redes2\\RMI\\RMI2
	    RMIHelper obj = new RMIHelper();
	    Buscador stub = (Buscador) UnicastRemoteObject.exportObject(obj, 0);

	    Registry registry = LocateRegistry.getRegistry();
	    registry.bind("buscar", stub);

	    System.out.println("Servidor listo...");
            
	} catch (Exception e) {
	    System.err.println("Server exception: " + e.toString());
	    e.printStackTrace();
	}
    }
    
    String clienteRMI(String nombre){
        try {
            Registry registry = LocateRegistry.getRegistry();
	    
	    Buscador stub = (Buscador) registry.lookup("buscar");
	    String response = stub.buscar(nombre);
            
            return response;
	}
        catch (Exception e) {
	    e.printStackTrace();
	}
        return "";
    }
    
    @Override
    public String buscar(String nombre) {
        try{
            File aux = new File("archivos_servidor");

            File[] archivos = aux.listFiles();

            String respuesta = "";
            
            if(archivos != null){
                for(int i = 0; i < archivos.length; i++)
                    if(archivos[i].getName().contains(nombre)){
                        respuesta = respuesta.concat(archivos[i].getName()+"\n") ;
                    }
                return respuesta;
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
        
        return "-1";
    }
}
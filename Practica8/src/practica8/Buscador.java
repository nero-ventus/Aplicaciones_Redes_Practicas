/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package practica8;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 *
 * @author devil
 */
public interface Buscador extends Remote{
    public String buscar(String nombre) throws RemoteException;
}

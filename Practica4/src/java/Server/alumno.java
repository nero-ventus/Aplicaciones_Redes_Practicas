package Server;

public class alumno {
    private String nombre;
    private int boleta;
    private String grupo;

    public alumno(String nombre, int boleta, String grupo) {
        this.nombre = nombre;
        this.boleta = boleta;
        this.grupo = grupo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public int getBoleta() {
        return boleta;
    }

    public void setBoleta(int boleta) {
        this.boleta = boleta;
    }

    public String getGrupo() {
        return grupo;
    }

    public void setGrupo(String grupo) {
        this.grupo = grupo;
    }
    
}
package cmd;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class ComandosFile {

    private File pathActual;

    public ComandosFile(String pathInicial) {
        this.pathActual = new File(pathInicial);
    }

    public void cd(File nuevaRuta) {
        pathActual = nuevaRuta;
    }

    public boolean mkdir(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            return false;
        }

        File objetivo = new File(pathActual, nombre);

        if (objetivo.exists()) {
            return false;
        }

        return objetivo.mkdirs();
    }

    public boolean Mfile(String nombre) throws IOException {
        if (nombre == null || nombre.isBlank()) {
            return false;
        }

        File objetivo = new File(pathActual, nombre);

        if (objetivo.exists()) {
            return false;
        }

        return objetivo.createNewFile();
    }

    public boolean rm(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            return false;
        }

        File target = new File(pathActual, nombre);

        if (!target.exists()) {
            return false;
        }

        return target.delete();
    }

    public boolean cdBack() {
        File padre = pathActual.getParentFile();
        if (padre == null) {
            return false;
        }
        pathActual = padre;
        return true;
    }

    void dir(String nombre) {
        File ArchivoDir = new File(pathActual, nombre);

        if (!ArchivoDir.exists()) {
            System.out.println("Error: el archivo o carpeta no existe: " + ArchivoDir.getPath());
            return;
        }

        if (!ArchivoDir.isDirectory()) {
            System.out.println("Error: no es un directorio: " + ArchivoDir.getPath());
            return;
        }

        dir(ArchivoDir);
    }

    void dir(File ArchivoDir) {
        if (ArchivoDir.isDirectory()) {
            System.out.println("fOLDER: " + ArchivoDir.getName());
            int dirs = 0, files = 0, bytes = 0;

            for (File child : ArchivoDir.listFiles()) {
                System.out.print(new Date(child.lastModified()));

                if (child.isDirectory()) {
                    System.out.print("\t<DIR>\t");
                    dirs++;
                }

                if (child.isFile()) {
                    System.out.print("\t    \t");
                    System.out.print(child.length());
                    files++;
                    bytes += child.length();
                }

                System.out.println("\t" + child.getName());
            }

            System.out.println("{" + files + "} files y {" + dirs + "} dirs");
            System.out.println(bytes + " bytes");

        } else {
            System.out.println("Accion no permitida");
        }
    }

    public boolean escribirTexto(String nombre, String texto) {

        if (nombre == null || nombre.trim().isEmpty()) {
            System.out.println("Error: nombre no valido");
            return false;
        }

        File target = new File(nombre, nombre);

        if (!target.exists()) {
            System.out.println("Error: el archivo no existe");
            return false;
        }

        if (target.isDirectory()) {
            System.out.println("Error: no se puede escribir en una carpeta");
            return false;
        }

        try (FileWriter fw = new FileWriter(target, true); PrintWriter pw = new PrintWriter(fw)) {

            pw.println(texto);
            System.out.println("Texto guardado correctamente en:");
            System.out.println(target.getAbsolutePath());
            return true;

        } catch (IOException e) {
            System.out.println("Error al escribir archivo: " + e.getMessage());
            return false;
        }
    }

    void leerTexto() throws IOException {
        if (!pathActual.exists()) {
            System.out.println("El archivo no existe");
            return;
        }

        FileReader fr = new FileReader(pathActual);
        BufferedReader br = new BufferedReader(fr);

        System.out.println(" del archivo");
        String linea;
        while ((linea = br.readLine()) != null) {
            System.out.println(linea);
        }
        br.close();
    }

    void horaActual() {
        Calendar c = Calendar.getInstance();
        SimpleDateFormat formatoHora = new SimpleDateFormat("HH:mm:ss");

        String horaStr = formatoHora.format(c.getTime());
        System.out.println("Hora actual: " + horaStr);
    }

    void fechaActual() {
        Calendar c = Calendar.getInstance();
        SimpleDateFormat formatoFecha = new SimpleDateFormat("dd/MM/yyyy");

        String fechaStr = formatoFecha.format(c.getTime());
        System.out.println("Fecha actual: " + fechaStr);
    }

}

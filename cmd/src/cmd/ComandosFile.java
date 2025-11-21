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

    public String dir(String nombre) {
        File ArchivoDir = new File(pathActual, nombre);
        if (!ArchivoDir.exists()) {
            return "Error: el archivo o carpeta no existe: " + ArchivoDir.getPath();
        }
        if (!ArchivoDir.isDirectory()) {
            return "Error: no es un directorio: " + ArchivoDir.getPath();
        }
        return dir(ArchivoDir);
    }

    public String dir(File ArchivoDir) {
        if (!ArchivoDir.isDirectory()) {
            return "Acción no permitida";
        }

        String contenido = String.format("%-20s %-10s %-12s %-30s\n", "Última Modificación", "Tipo", "Tamaño", "Nombre");

        int archivos = 0;
        int directorios = 0;
        long bytesTotal = 0;

        File[] hijos = ArchivoDir.listFiles();
        if (hijos != null) {
            for (File child : hijos) {
                String fecha = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                        .format(new Date(child.lastModified()));

                String tipo;
                String tamaño;
                if (child.isDirectory()) {
                    tipo = "<DIR>";
                    tamaño = "-";
                    directorios++;
                } else {
                    tipo = "FILE";
                    long b = child.length();
                    tamaño = formatearTamaño(b);
                    archivos++;
                    bytesTotal += b;
                }

                String nombre = child.getName();

                contenido += String.format("%-20s %-10s %-12s %-30s\n", fecha, tipo, tamaño, nombre);
            }
        }

        long espacioLibre = ArchivoDir.getUsableSpace();

        contenido += "\n" + archivos + " archivos\t" + formatearTamaño(bytesTotal) + "\n";
        contenido += directorios + " directorios\t" + formatearTamaño(espacioLibre) + " libres\n";

        return contenido;
    }

    private String formatearTamaño(long bytes) {
        double kb = bytes / 1024.0;
        double mb = kb / 1024.0;
        double gb = mb / 1024.0;

        if (gb >= 1) {
            return String.format("%.2f GB", gb);
        } else if (mb >= 1) {
            return String.format("%.2f MB", mb);
        } else if (kb >= 1) {
            return String.format("%.2f KB", kb);
        } else {
            return bytes + " B";
        }
    }

    public String escribirTexto(String nombre, String texto) {
        if (nombre == null || nombre.trim().isEmpty()) {
            return "Error: nombre no válido";
        }

        File target = new File(nombre, nombre);

        if (!target.exists()) {
            return "Error: el archivo no existe";
        }

        if (target.isDirectory()) {
            return "Error: no se puede escribir en una carpeta";
        }

        try (FileWriter fw = new FileWriter(target, true); PrintWriter pw = new PrintWriter(fw)) {
            pw.println(texto);
            return "Texto guardado correctamente en:\n" + target.getAbsolutePath();
        } catch (IOException e) {
            return "Error al escribir archivo: " + e.getMessage();
        }
    }

    public String leerTexto() {
        if (!pathActual.exists()) {
            return "El archivo no existe";
        }

        String contenido = "Contenido del archivo:\n";

        try (BufferedReader br = new BufferedReader(new FileReader(pathActual))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                contenido = contenido + linea + "\n";
            }
        } catch (IOException e) {
            return "Error al leer el archivo: " + e.getMessage();
        }

        return contenido;
    }

    public String horaActual() {
        Calendar c = Calendar.getInstance();
        SimpleDateFormat formatoHora = new SimpleDateFormat("HH:mm:ss");

        return formatoHora.format(c.getTime());
    }

    public String fechaActual() {
        Calendar c = Calendar.getInstance();
        SimpleDateFormat formatoFecha = new SimpleDateFormat("dd/MM/yyyy");

        return formatoFecha.format(c.getTime());
    }

}

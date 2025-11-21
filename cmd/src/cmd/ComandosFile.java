package cmd;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class ComandosFile {

    private File pathActual;

    public ComandosFile(String pathInicial) {
        this.pathActual = new File(pathInicial);
    }

    public void cd(File nuevaRuta) {
        if (nuevaRuta != null && nuevaRuta.exists() && nuevaRuta.isDirectory()) {
            pathActual = nuevaRuta;
        }
    }

    public boolean mkdir(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            return false;
        }

        if (!pathActual.exists() || !pathActual.isDirectory()) {
            return false;
        }

        File objetivo = new File(pathActual, nombre);

        if (objetivo.exists()) {
            return false;
        }

        return objetivo.mkdirs();
    }

    public boolean Mfile(String nombre) throws IOException {
        if (nombre == null || nombre.trim().isEmpty()) {
            return false;
        }

        if (!pathActual.exists() || !pathActual.isDirectory()) {
            return false;
        }

        File objetivo = new File(pathActual, nombre);

        if (objetivo.exists()) {
            return false;
        }

        File parent = objetivo.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
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

        return borrarRecursivo(target);
    }

    private boolean borrarRecursivo(File file) {
        if (file.isDirectory()) {
            File[] children = file.listFiles();
            if (children != null) {
                for (File child : children) {
                    if (!borrarRecursivo(child)) {
                        return false;
                    }
                }
            }
        }
        return file.delete();
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
        File ArchivoDir;
        if (nombre == null || nombre.trim().isEmpty() || nombre.equals(".")) {
            ArchivoDir = pathActual;
        } else {
            ArchivoDir = new File(pathActual, nombre);
        }

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

        StringBuilder contenido = new StringBuilder();
        contenido.append(String.format("%-20s %-10s %-12s %-30s%n", "Última Modificación", "Tipo", "Tamaño", "Nombre"));

        int archivos = 0;
        int directorios = 0;
        long bytesTotal = 0;

        File[] hijos = ArchivoDir.listFiles();
        if (hijos != null) {
            for (File child : hijos) {
                String fecha = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(child.lastModified()));

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

                contenido.append(String.format("%-20s %-10s %-12s %-30s%n", fecha, tipo, tamaño, nombre));
            }
        }

        long espacioLibre = ArchivoDir.getUsableSpace();

        contenido.append(System.lineSeparator())
                 .append(archivos).append(" archivos\t").append(formatearTamaño(bytesTotal)).append(System.lineSeparator());
        contenido.append(directorios).append(" directorios\t").append(formatearTamaño(espacioLibre)).append(" libres").append(System.lineSeparator());

        return contenido.toString();
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

        File target = new File(pathActual, nombre);

        if (!target.exists()) {
            return "Error: el archivo no existe: " + target.getAbsolutePath();
        }

        if (target.isDirectory()) {
            return "Error: no se puede escribir en una carpeta";
        }

        try (FileWriter fw = new FileWriter(target, true);
             PrintWriter pw = new PrintWriter(fw)) {

            if (texto == null) texto = "";
            pw.println(texto);
            return "";
        } catch (IOException e) {
            return "Error al escribir archivo: " + e.getMessage();
        }
    }

    public String leerTexto() {
        if (pathActual == null) {
            return "Error: path actual no definido";
        }

        if (!pathActual.exists()) {
            return "Error: el archivo no existe: " + pathActual.getAbsolutePath();
        }

        if (pathActual.isDirectory()) {
            return "Error: pathActual es un directorio. Use leerTexto(nombre) para leer un archivo específico.";
        }

        StringBuilder contenido = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(pathActual))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                contenido.append(linea).append(System.lineSeparator());
            }
        } catch (IOException e) {
            return "Error al leer el archivo: " + e.getMessage();
        }
        return contenido.toString();
    }

    public String leerTexto(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            return "Error: nombre no válido";
        }

        File target = new File(pathActual, nombre);

        if (!target.exists()) {
            return "Error: el archivo no existe: " + target.getAbsolutePath();
        }

        if (target.isDirectory()) {
            return "Error: no se puede leer una carpeta";
        }

        StringBuilder contenido = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(target))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                contenido.append(linea).append(System.lineSeparator());
            }
        } catch (IOException e) {
            return "Error al leer el archivo: " + e.getMessage();
        }
        return contenido.toString();
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

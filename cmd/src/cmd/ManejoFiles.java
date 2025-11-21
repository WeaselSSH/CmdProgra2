package cmd;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ManejoFiles {

    private File pathActual;

    public ManejoFiles(String pathInicial) {
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

}

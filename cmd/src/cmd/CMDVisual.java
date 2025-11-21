package cmd;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;

public class CMDVisual extends JFrame {

    private final JTextArea area;
    private int inicioEntrada = 0;
    private final ComandosFile manejador;
    private File rutaActual;

    private boolean modoEscritura = false;
    private String escribeTarget = null;
    private StringBuilder escribeBuffer = null;

    public CMDVisual() {
        super("CMD Insano");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(1100, 650);
        setLocationRelativeTo(null);

        String dirUsuario = System.getProperty("user.dir");
        rutaActual = new File(dirUsuario);
        manejador = new ComandosFile(rutaActual.getAbsolutePath());

        area = new JTextArea();
        area.setEditable(true);
        area.setBackground(Color.BLACK);
        area.setForeground(Color.WHITE);
        area.setCaretColor(Color.WHITE);
        area.setFont(new Font("Consolas", Font.PLAIN, 14));
        area.setLineWrap(true);
        area.setWrapStyleWord(true);

        JScrollPane scroll = new JScrollPane(area);
        add(scroll);

        appendText("Microsoft Windows [Versión 10.0.22621.521]\n");
        appendText("(c) Microsoft Corporation. Todos los derechos reservados.\n");
        appendText("Si ocupas ayuda usa el comando 'help'.\n");
        writePrompt();

        area.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                int caretPos = area.getCaretPosition();

                if (!modoEscritura) {
                    if ((e.getKeyCode() == KeyEvent.VK_LEFT || e.getKeyCode() == KeyEvent.VK_HOME) && caretPos <= inicioEntrada) {
                        e.consume();
                        area.setCaretPosition(inicioEntrada);
                        return;
                    }
                    if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE && caretPos <= inicioEntrada) {
                        e.consume();
                        return;
                    }
                    if (e.getKeyCode() == KeyEvent.VK_DELETE && caretPos < inicioEntrada) {
                        e.consume();
                        return;
                    }
                }

                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    e.consume();
                    if (modoEscritura) {
                        handleInteractiveWrite();
                        return;
                    }

                    String command = "";
                    try {
                        int len = area.getDocument().getLength();
                        command = area.getText(inicioEntrada, len - inicioEntrada).trim();
                    } catch (BadLocationException ex) {
                        appendText("\nError leyendo la entrada: " + ex.getMessage() + "\n");
                        writePrompt();
                        return;
                    }

                    appendText("\n");
                    processCommand(command);
                    writePrompt();
                }
            }
        });

        setVisible(true);
    }

    private void appendText(String s) {
        area.append(s);
        area.setCaretPosition(area.getDocument().getLength());
    }

    private void writePrompt() {
        appendText(rutaActual.getAbsolutePath() + ">");
        inicioEntrada = area.getDocument().getLength();
    }

    private void handleInteractiveWrite() {
        try {
            int len = area.getDocument().getLength();
            String full = area.getText(inicioEntrada, len - inicioEntrada);
            String[] lines = full.split("\\r?\\n", -1);
            String lastLine = lines.length > 0 ? lines[lines.length - 1] : "";
            if (lastLine.equals(".")) {
                String textoFinal = escribeBuffer == null ? "" : escribeBuffer.toString();
                manejador.escribirTexto(escribeTarget, textoFinal);
                modoEscritura = false;
                escribeTarget = null;
                escribeBuffer = null;
                writePrompt();
                return;
            } else {
                if (escribeBuffer == null) escribeBuffer = new StringBuilder();
                escribeBuffer.append(lastLine).append(System.lineSeparator());
                inicioEntrada = area.getDocument().getLength();
                return;
            }
        } catch (BadLocationException ex) {
            appendText("\nError en modo escritura: " + ex.getMessage() + "\n");
            modoEscritura = false;
            escribeTarget = null;
            escribeBuffer = null;
            writePrompt();
        }
    }

    private void processCommand(String raw) {
        if (raw.isEmpty()) return;
        String[] parts = raw.split("\\s+");
        String cmd = parts[0].toLowerCase();

        try {
            switch (cmd) {
                case "help":
                    appendText("Comandos disponibles:\n");
                    appendText("  cd <ruta>\n");
                    appendText("  ...\n");
                    appendText("  mkdir <nombre>\n");
                    appendText("  mfile <nombre>\n");
                    appendText("  rm <nombre>\n");
                    appendText("  dir\n");
                    appendText("  wr <archivo> [texto...]\n");
                    appendText("  rd <archivo>\n");
                    appendText("  time\n");
                    appendText("  date\n");
                    appendText("  cls\n");
                    appendText("  exit\n");
                    break;

                case "cd":
                    if (parts.length < 2) {
                        appendText("Uso: cd <ruta>\n");
                    } else {
                        String ruta = raw.substring(raw.indexOf(' ') + 1).trim();
                        File nueva;
                        if (ruta.equals("..")) {
                            nueva = rutaActual.getParentFile();
                        } else {
                            File posible = new File(ruta);
                            if (posible.isAbsolute()) {
                                nueva = posible;
                            } else {
                                nueva = new File(rutaActual, ruta);
                            }
                        }
                        if (nueva != null && nueva.exists() && nueva.isDirectory()) {
                            manejador.cd(nueva);
                            rutaActual = nueva;
                        } else {
                            appendText("No existe la ruta.\n");
                        }
                    }
                    break;

                case "...":
                case "cd..":
                case "cdback":
                    if (manejador.cdBack()) {
                        File padre = rutaActual.getParentFile();
                        if (padre != null) rutaActual = padre;
                    } else {
                        appendText("No se puede subir más\n");
                    }
                    break;

                case "mkdir":
                    if (parts.length < 2) {
                        appendText("Uso: mkdir <carpeta>\n");
                    } else {
                        manejador.mkdir(parts[1]);
                    }
                    break;

                case "mfile":
                    if (parts.length < 2) {
                        appendText("Uso: mfile <archivo>\n");
                    } else {
                        try {
                            manejador.Mfile(parts[1]);
                        } catch (IOException ioe) {
                            appendText("Error: " + ioe.getMessage() + "\n");
                        }
                    }
                    break;

                case "rm":
                    if (parts.length < 2) {
                        appendText("Uso: rm <nombre>\n");
                    } else {
                        manejador.rm(parts[1]);
                    }
                    break;

                case "dir":
                    String resultadoDir = manejador.dir(".");
                    appendText(resultadoDir + "\n");
                    break;

                case "wr":
                    if (parts.length < 2) break;
                    String nombre = parts[1];
                    if (parts.length >= 3) {
                        int startIdx = raw.indexOf(parts[2]);
                        String textoInline = raw.substring(startIdx);
                        manejador.escribirTexto(nombre, textoInline);
                    } else {
                        modoEscritura = true;
                        escribeTarget = nombre;
                        escribeBuffer = new StringBuilder();
                        inicioEntrada = area.getDocument().getLength();
                    }
                    break;

                case "rd":
                    if (parts.length < 2) break;
                    String objetivo = parts[1];
                    String contenido = manejador.leerTexto(objetivo);
                    appendText(contenido + "\n");
                    break;

                case "time":
                    appendText(manejador.horaActual() + "\n");
                    break;

                case "date":
                    appendText(manejador.fechaActual() + "\n");
                    break;

                case "cls":
                    area.setText("");
                    appendText("Microsoft Windows [Versión 10.0.22621.521]\n");
                    appendText("(c) Microsoft Corporation. Todos los derechos reservados.\n");
                    appendText("Si ocupas ayuda usa el comando 'help'.\n");
                    break;

                case "exit":
                    dispose();
                    break;

                default:
                    appendText("Comando no reconocido: " + cmd + "\n");
                    break;
            }
        } catch (Exception ex) {
            appendText("Error al ejecutar comando: " + ex.getMessage() + "\n");
        }
    }
}

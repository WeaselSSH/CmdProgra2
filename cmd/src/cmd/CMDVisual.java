package cmd;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;

/**
 * @author esteb
 */
public class CMDVisual extends JFrame {

    private final JTextArea area;
    private int inicioEntrada = 0;
    private final ComandosFile manejador;
    private File rutaActual;

    public CMDVisual() {
        super("CMD Insano - Integrado");
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

                if ((e.getKeyCode() == KeyEvent.VK_LEFT || e.getKeyCode() == KeyEvent.VK_HOME)
                        && caretPos <= inicioEntrada) {
                    e.consume();
                    area.setCaretPosition(inicioEntrada);
                    return;
                }

                if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE && caretPos <= inicioEntrada) {
                    e.consume();
                    return;
                }

                if (e.getKeyCode() == KeyEvent.VK_DELETE) {
                    if (caretPos < inicioEntrada) {
                        e.consume();
                        return;
                    }
                }

                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    e.consume();
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

    private void processCommand(String raw) {
        if (raw.isEmpty()) {
            return;
        }

        String[] parts = raw.split("\\s+");
        String cmd = parts[0].toLowerCase();

        try {
            switch (cmd) {
                case "help":
                    appendText("Comandos disponibles:\n");
                    appendText("  cd <ruta>          - cambia a la ruta especificada (File)\n");
                    appendText("  cdback             - subir al directorio padre\n");
                    appendText("  mkdir <nombre>     - crear carpeta\n");
                    appendText("  mfile <nombre>     - crear archivo\n");
                    appendText("  rm <nombre>        - borrar archivo/carpeta\n");
                    appendText("  dir [nombre]       - listar directorio (sin args lista pathActual)\n");
                    appendText("  write <file> <txt> - escribe texto (texto es lo que sigue)\n");
                    appendText("  leer                - llamar a leerTexto() de ComandosFile (usa pathActual)\n");
                    appendText("  hora                - muestra hora\n");
                    appendText("  fecha               - muestra fecha\n");
                    appendText("  cls                 - Limpia el cmd\n");
                    appendText("  exit                - cerrar CMD\n");
                    break;

                case "cd":
                    if (parts.length < 2) {
                        appendText("Uso: cd <ruta>\n");
                    } else {
                        String ruta = raw.substring(raw.indexOf(' ') + 1).trim();
                        File nuevaRuta;
                        if (ruta.equals("..")) {
                            nuevaRuta = rutaActual.getParentFile();
                        } else {
                            nuevaRuta = new File(ruta);
                            if (!nuevaRuta.isAbsolute()) {
                                nuevaRuta = new File(rutaActual, ruta);
                            }
                        }
                        if (nuevaRuta != null && nuevaRuta.exists() && nuevaRuta.isDirectory()) {
                            manejador.cd(nuevaRuta);
                            rutaActual = nuevaRuta;
                            appendText("Directorio actual: " + rutaActual.getAbsolutePath() + "\n");
                        } else {
                            appendText("El sistema no puede encontrar la ruta especificada.\n");
                        }
                    }
                    break;

                case "cdback":
                case "cd..":
                    boolean ok = manejador.cdBack();
                    if (ok) {
                        File padre = rutaActual.getParentFile();
                        if (padre != null) {
                            rutaActual = padre;
                        }
                        appendText("Subido al directorio padre\n");
                    } else {
                        appendText("No se pudo subir (sin padre)\n");
                    }
                    break;

                case "mkdir":
                    if (parts.length < 2) {
                        appendText("Uso: mkdir <nombre>\n");
                    } else {
                        boolean creado = manejador.mkdir(parts[1]);
                        appendText(creado ? "Directorio creado\n" : "No se pudo crear (ya existe o nombre inválido)\n");
                    }
                    break;

                case "mfile":
                    if (parts.length < 2) {
                        appendText("Uso: mfile <nombre>\n");
                    } else {
                        try {
                            boolean creadoF = manejador.Mfile(parts[1]);
                            appendText(creadoF ? "Archivo creado\n" : "No se pudo crear (ya existe o nombre inválido)\n");
                        } catch (IOException ioe) {
                            appendText("Error creando archivo: " + ioe.getMessage() + "\n");
                        }
                    }
                    break;

                case "rm":
                    if (parts.length < 2) {
                        appendText("Uso: rm <nombre>\n");
                    } else {
                        boolean borrado = manejador.rm(parts[1]);
                        appendText(borrado ? "Borrado OK\n" : "No se pudo borrar (no existe o error)\n");
                    }
                    break;

                case "dir":
                    if (parts.length < 2) {
                        appendText("Listado del directorio actual:\n");
                        manejador.dir(".");
                    } else {
                        manejador.dir(parts[1]);
                    }
                    appendText("\n");
                    break;

                case "write":
                    if (parts.length < 3) {
                        appendText("Uso: write <archivo> <texto...>\n");
                    } else {
                        String file = parts[1];
                        String texto = raw.substring(raw.indexOf(file) + file.length()).trim();
                        boolean escrito = manejador.escribirTexto(file, texto);
                        appendText(escrito ? "Escrito correctamente\n" : "No se pudo escribir (error)\n");
                    }
                    break;

                case "leer":
                    try {
                        manejador.leerTexto();
                    } catch (IOException e) {
                        appendText("Error leyendo texto: " + e.getMessage() + "\n");
                    }
                    break;

                case "hora":
                    manejador.horaActual();
                    appendText("\n");
                    break;

                case "fecha":
                    manejador.fechaActual();
                    appendText("\n");
                    break;

                case "exit":
                    appendText("Cerrando...\n");
                    dispose();
                    break;

                case "cls":
                    area.setText("");
                    appendText("Microsoft Windows [Versión 10.0.22621.521]\n");
                    appendText("(c) Microsoft Corporation. Todos los derechos reservados.\n");
                    appendText("Si ocupas ayuda usa el comando 'help'.\n");
                    break;

                default:
                    appendText("Comando no reconocido: " + cmd + "\n");
                    break;
            }
        } catch (Exception ex) {
            appendText("Error al ejecutar comando: " + ex.getMessage() + "\n");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new CMDVisual().setVisible(true));
    }
}

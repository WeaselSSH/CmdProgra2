package cmd;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class CMDVisual extends JFrame {

    private final JTextArea area;
    private int inicioEntrada = 0;
    private final ComandosFile manejador;

    public CMDVisual() {
        super("CMD Insano");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(1100, 650);
        setLocationRelativeTo(null);

        String dirUsuario = System.getProperty("user.dir");
        manejador = new ComandosFile(dirUsuario);

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

                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    e.consume();
                    String command;
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
        File actual = manejador.getPathActual();
        String ruta = actual != null ? actual.getAbsolutePath() : "";
        appendText(ruta + ">");
        inicioEntrada = area.getDocument().getLength();
    }

    private void processCommand(String raw) {
        if (raw == null || raw.isEmpty()) {
            return;
        }

        String[] parts = raw.split("\\s+");
        String cmd = parts[0].toLowerCase();

        try {
            switch (cmd) {
                case "help":
                    appendText("Comandos disponibles:\n");
                    appendText("  cd <ruta>\n");
                    appendText("  cd.. | ... | cdback\n");
                    appendText("  mkdir <nombre>\n");
                    appendText("  mfile <nombre>\n");
                    appendText("  rm <nombre>\n");
                    appendText("  dir [ruta]\n");
                    appendText("  wr <archivo> <texto>\n");
                    appendText("  rd <archivo>\n");
                    appendText("  time\n");
                    appendText("  date\n");
                    appendText("  cls\n");
                    appendText("  exit\n");
                    break;

                case "cd":
                    if (parts.length < 2) {
                        File actual = manejador.getPathActual();
                        if (actual != null) {
                            appendText(actual.getAbsolutePath() + "\n");
                        }
                    } else {
                        String ruta = raw.substring(raw.indexOf(' ') + 1).trim();
                        File base = manejador.getPathActual();
                        File nueva;
                        if ("..".equals(ruta)) {
                            nueva = base != null ? base.getParentFile() : null;
                        } else {
                            File posible = new File(ruta);
                            if (posible.isAbsolute()) {
                                nueva = posible;
                            } else {
                                nueva = base != null ? new File(base, ruta) : posible;
                            }
                        }
                        if (manejador.cd(nueva)) {
                            File actual = manejador.getPathActual();
                            if (actual != null) {
                                appendText(actual.getAbsolutePath() + "\n");
                            }
                        } else {
                            appendText("No existe la ruta.\n");
                        }
                    }
                    break;

                case "...":
                case "cd..":
                case "cdback":
                    if (manejador.cdBack()) {
                        File actual = manejador.getPathActual();
                        if (actual != null) {
                            appendText(actual.getAbsolutePath() + "\n");
                        }
                    } else {
                        appendText("No se puede subir más.\n");
                    }
                    break;

                case "mkdir":
                    if (parts.length < 2) {
                        appendText("Uso: mkdir <carpeta>\n");
                    } else {
                        String msg = manejador.mkdir(parts[1]);
                        appendText(msg + "\n");
                    }
                    break;

                case "mfile":
                    if (parts.length < 2) {
                        appendText("Uso: mfile <archivo>\n");
                    } else {
                        String msg = manejador.mfile(parts[1]);
                        appendText(msg + "\n");
                    }
                    break;

                case "rm":
                    if (parts.length < 2) {
                        appendText("Uso: rm <nombre>\n");
                    } else {
                        String msg = manejador.rm(parts[1]);
                        appendText(msg + "\n");
                    }
                    break;

                case "dir":
                    String argDir;
                    if (parts.length < 2) {
                        argDir = ".";
                    } else {
                        argDir = raw.substring(raw.indexOf(' ') + 1).trim();
                    }
                    String resultadoDir = manejador.dir(argDir);
                    appendText(resultadoDir + "\n");
                    break;

                case "wr":
                    if (parts.length < 3) {
                        appendText("Uso: wr <archivo> <texto>\n");
                    } else {
                        String nombre = parts[1];
                        int startIdx = raw.indexOf(parts[2]);
                        String texto = raw.substring(startIdx);
                        String msg = manejador.escribirTexto(nombre, texto);
                        appendText(msg + "\n");
                    }
                    break;

                case "rd":
                    if (parts.length < 2) {
                        appendText("Uso: rd <archivo>\n");
                    } else {
                        String contenido = manejador.leerTexto(parts[1]);
                        appendText(contenido + "\n");
                    }
                    break;

                case "time":
                    String hora = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
                    appendText(hora + "\n");
                    break;

                case "date":
                    String fecha = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                    appendText(fecha + "\n");
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

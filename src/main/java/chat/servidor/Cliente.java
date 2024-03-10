package chat.servidor;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Cliente {

    private JTextField mensajeField;
    private JTextArea chatArea;
    private PrintWriter salida;
    private BufferedReader entrada;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                new Cliente().iniciarInterfaz();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private void iniciarInterfaz() throws IOException {
        JFrame frame = new JFrame("Cliente");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 600);

        JPanel panel = new JPanel(new BorderLayout());

        JPanel mensajePanel = new JPanel(new BorderLayout());
        mensajeField = new JTextField();
        JButton enviarMensajeButton = new JButton("Enviar");

        enviarMensajeButton.addActionListener(e -> {
            try {
                enviarMensaje();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });

        mensajePanel.add(mensajeField, BorderLayout.CENTER);
        mensajePanel.add(enviarMensajeButton, BorderLayout.EAST);

        panel.add(mensajePanel, BorderLayout.SOUTH);

        chatArea = new JTextArea();
        chatArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(chatArea);
        panel.add(scrollPane, BorderLayout.CENTER);

        frame.getContentPane().add(panel);
        frame.setVisible(true);

        String usuario = JOptionPane.showInputDialog("Ingrese su usuario:");
        char[] contrasenaChars = JOptionPane.showInputDialog("Ingrese su contraseña:").toCharArray();
        String contrasena = new String(contrasenaChars);

        Socket socket = new Socket("192.168.1.67", 5000);
        salida = new PrintWriter(socket.getOutputStream(), true);

        salida.println(usuario);
        salida.println(hashSHA256(contrasena));

        entrada = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        String respuesta = entrada.readLine();

        if ("OK".equals(respuesta)) {
            new Thread(this::recibirMensajes).start();
        } else {
            mostrarMensajeError();
            cerrarAplicacion();
        }
    }

    private void enviarMensaje() throws IOException {
        String mensaje = mensajeField.getText();
        salida.println(mensaje);
        mensajeField.setText("");
    }

    private void recibirMensajes() {
        try {
            String mensaje;
            while ((mensaje = entrada.readLine()) != null) {
                if ("[ALMACEN]".equals(mensaje)) {
                    mostrarConversacionAlmacenada();
                } else {
                    mostrarMensaje(mensaje);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void mostrarMensaje(String mensaje) {
        chatArea.append(mensaje + "\n");
    }

    private void cerrarAplicacion() {
        System.exit(0);
    }

    private void mostrarMensajeError() {
        JOptionPane.showMessageDialog(null, "Credenciales incorrectas. Conexión terminada.", "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void mostrarConversacionAlmacenada() {
        chatArea.setText(""); // Limpiar el área de chat antes de mostrar mensajes almacenados
        try {
            String mensaje;
            while (!(mensaje = entrada.readLine()).equals("[FIN_ALMACEN]")) {
                mostrarMensaje(mensaje);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String hashSHA256(String texto) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(texto.getBytes(StandardCharsets.UTF_8));

            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }

            return hexString.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}

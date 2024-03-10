package chat.servidor;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class ServidorPrincipal {

    private HashMap<String, String> usuarios = new HashMap<>();
    private HashMap<String, Long> registros = new HashMap<>();
    private List<PrintWriter> clientesConectados = new ArrayList<>();
    private List<String> conversacion = new ArrayList<>();

    public static final String AES_KEY = "0123456789abcdef";

    public static void main(String[] args) {
        ServidorPrincipal servidor = new ServidorPrincipal();
        servidor.cargarUsuariosDesdeArchivo("usuarios.txt");

        try (ServerSocket serverSocket = new ServerSocket(5000)) {
            System.out.println("ServidorPrincipal esperando conexiones...");

            while (true) {
                Socket socket = serverSocket.accept();
                servidor.agregarCliente(socket);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized void agregarCliente(Socket socket) {
        try {
            BufferedReader entrada = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter salida = new PrintWriter(socket.getOutputStream(), true);

            String usuario = entrada.readLine();
            String contrasenaCifrada = entrada.readLine();

            if (validarCredenciales(usuario, contrasenaCifrada)) {
                salida.println("OK");
                manejarConexion(socket.getInetAddress().getHostAddress(), usuario, salida);

                // Enviar mensajes almacenados al nuevo cliente
                enviarAlmacenAlCliente(salida);

                // Lógica del chat grupal
                new Thread(() -> {
                    try {
                        String mensajeCliente;
                        while ((mensajeCliente = entrada.readLine()) != null) {
                            if ("[ALMACEN]".equals(mensajeCliente)) {
                                // Indicador de inicio de mensajes almacenados
                                mostrarConversacionAlmacenada(salida);
                            } else if ("[FIN_ALMACEN]".equals(mensajeCliente)) {
                                // Indicador de fin de mensajes almacenados
                                continue;
                            } else {
                                broadcastMensaje(usuario + ": " + mensajeCliente);
                            }
                        }
                    } catch (SocketException e) {
                        // Cliente desconectado
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        // Eliminar cliente cuando se desconecta
                        eliminarCliente(socket, usuario, salida);
                        manejarDesconexion(socket.getInetAddress().getHostAddress(), usuario, salida);
                    }
                }).start();
            } else {
                salida.println("ERROR");
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized void eliminarCliente(Socket socket, String usuario, PrintWriter salida) {
        try {
            socket.close();
            salida.close();
            clientesConectados.remove(salida);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized void manejarConexion(String ip, String usuario, PrintWriter salida) {
        registros.put(ip, System.currentTimeMillis());
        imprimirMensaje("Cliente " + usuario + " conectado desde la IP " + ip);
        clientesConectados.add(salida);

        // Enviar mensaje de bienvenida al nuevo cliente
        salida.println("¡Bienvenido al chat grupal, " + usuario + "!");
        broadcastMensaje(usuario + " se ha unido al chat.");
    }

    public synchronized void manejarDesconexion(String ip, String usuario, PrintWriter salida) {
        registros.remove(ip);
        imprimirMensaje("Cliente " + usuario + " desconectado desde la IP " + ip);
        clientesConectados.remove(salida);
        broadcastMensaje(usuario + " se ha desconectado.");
    }

    private void enviarAlmacenAlCliente(PrintWriter salida) {
        salida.println("[ALMACEN]");
        for (String mensaje : conversacion) {
            salida.println(mensaje);
        }
        // Indicar fin de mensajes almacenados
        salida.println("[FIN_ALMACEN]");
    }

    private void mostrarConversacionAlmacenada(PrintWriter salida) {
        for (String mensaje : conversacion) {
            salida.println(mensaje);
        }
        // Indicar fin de mensajes almacenados
        salida.println("[FIN_ALMACEN]");
    }

    public synchronized void broadcastMensaje(String mensaje) {
        Date fechaHoraActual = new Date();
        SimpleDateFormat formatoFecha = new SimpleDateFormat("dd-MM-yyyy");
        SimpleDateFormat formatoHora = new SimpleDateFormat("HH:mm:ss");

        String fecha = formatoFecha.format(fechaHoraActual);
        String hora = formatoHora.format(fechaHoraActual);

        String mensajeConFecha = mensaje + " (" + hora + " " + fecha + ")";
        conversacion.add(mensajeConFecha);

        // Escribir el mensaje en el archivo de almacenamiento
        guardarMensajeEnAlmacen(mensajeConFecha);

        // Enviar el mensaje a todos los clientes conectados
        for (PrintWriter cliente : clientesConectados) {
            cliente.println(mensajeConFecha);
        }
    }

    public synchronized void guardarMensajeEnAlmacen(String mensaje) {
        try (PrintWriter almacenWriter = new PrintWriter(new FileWriter("almacen.txt", true))) {
            String mensajeSinFecha = obtenerMensajeSinFecha(mensaje);
            almacenWriter.println(mensajeSinFecha);
            almacenWriter.flush();

            // También envía el mensaje al ServidorReplicacion
            enviarMensajeAServidorReplicacion(mensajeSinFecha);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String obtenerMensajeSinFecha(String mensaje) {
        String[] partes = mensaje.split(" - ", 2);
        if (partes.length == 2) {
            return partes[1];
        }
        return mensaje;
    }

    private void enviarMensajeAServidorReplicacion(String mensaje) {
        try (Socket socketReplicacion = new Socket("localhost", 5001);
             PrintWriter salidaReplicacion = new PrintWriter(socketReplicacion.getOutputStream(), true)) {
            salidaReplicacion.println(mensaje);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized boolean validarCredenciales(String usuario, String contrasenaCifrada) {
        if (usuarios.containsKey(usuario)) {
            return usuarios.get(usuario).equals(contrasenaCifrada);
        }
        return false;
    }

    private void cargarUsuariosDesdeArchivo(String archivo) {
        try (BufferedReader br = new BufferedReader(new FileReader(archivo))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                String[] partes = linea.split(":");
                if (partes.length == 2) {
                    String usuario = partes[0];
                    String contrasenaCifrada = partes[1];
                    usuarios.put(usuario, contrasenaCifrada);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void imprimirMensaje(String mensaje) {
        Date fechaHoraActual = new Date();
        SimpleDateFormat formato = new SimpleDateFormat("HH:mm:ss dd-MM-yyyy");
        String fechaHora = formato.format(fechaHoraActual);
        System.out.println(mensaje + " a las " + fechaHora);
    }
}

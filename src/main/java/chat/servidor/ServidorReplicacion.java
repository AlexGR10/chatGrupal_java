package chat.servidor;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ServidorReplicacion {

    public static void main(String[] args) {
        ServidorReplicacion servidorReplicacion = new ServidorReplicacion();
        servidorReplicacion.iniciarServidor();
    }

    public void iniciarServidor() {
        try (ServerSocket serverSocket = new ServerSocket(5001)) {
            System.out.println("ServidorReplicacion esperando conexiones...");

            while (true) {
                Socket socket = serverSocket.accept();
                manejarConexion(socket);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void manejarConexion(Socket socket) {
        try (BufferedReader entrada = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            String mensaje;
            while ((mensaje = entrada.readLine()) != null) {
                // Aquí puedes procesar el mensaje replicado
                System.out.println("Mensaje replicado: " + mensaje);

                // También guarda el mensaje en el archivo almacen_replica.txt
                guardarMensajeEnAlmacenReplica(mensaje);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void guardarMensajeEnAlmacenReplica(String mensaje) {
        try (PrintWriter almacenReplicaWriter = new PrintWriter(new FileWriter("almacen_replica.txt", true))) {
            almacenReplicaWriter.println(mensaje);
            almacenReplicaWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}


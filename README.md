# Chat Grupal con Sockets en Java

Este proyecto implementa un chat grupal utilizando la arquitectura cliente-servidor en Java. Proporciona una plataforma de comunicación segura y eficiente, con características clave que incluyen autenticación de usuarios, cifrado de mensajes, persistencia de conversaciones y sincronización horaria.

## Características

1. **Servidor Principal:**
   - Inicia el servidor principal para gestionar las conexiones de los clientes.
   - Almacena nombres de usuario, direcciones IP y contraseñas cifradas en una base de datos en un archivo .txt.

2. **Autenticación Segura:**
   - Los clientes deben autenticarse para acceder al chat.
   - La comunicación entre clientes y servidor durante el proceso de inicio de sesión está cifrada con el algoritmo AES.

3. **Persistencia de Conversaciones:**
   - El servidor carga la conversación desde un archivo .txt, permitiendo a los usuarios ver el historial de mensajes incluso cuando estén fuera de línea.

4. **Servidor Secundario:**
   - Replica la conversación del servidor principal para respaldar la información.
   - Garantiza la disponibilidad de los datos incluso en situaciones de falla.

5. **Sincronización Horaria:**
   - Utiliza el algoritmo de Berkeley para sincronizar la hora entre el servidor principal y los clientes.
   - Evita desfases horarios y asegura una experiencia de chat coherente.

## Instrucciones de Uso

1. **Iniciar el Servidor Principal:**
   - Ejecuta el servidor principal para gestionar las conexiones de los clientes.

2. **Conectar Clientes:**
   - Inicia el cliente y únete al chat proporcionando las credenciales de inicio de sesión.

3. **Disfruta del Chat Grupal:**
   - Participa en conversaciones grupales de forma segura y accede al historial de mensajes en cualquier momento.

## Requisitos del Proyecto

- Java SDK instalado.
- Conexión a Internet para la sincronización horaria.

## Notas Adicionales

- Este proyecto se centra en la seguridad y la persistencia de datos, brindando una experiencia de chat confiable y funcional.

**¡Diviértete chateando de manera segura y colaborativa con el Chat Grupal en Java!**

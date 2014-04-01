# Infostander Frontend in Java
This is a frontend for Infostander written in Java.

The latest compiled jar file is in the jar/ folder.

The project directory is InfostanderFrontend/ .

The socket.io-java-client folder is a modified socket.io-client library (https://github.com/Gottox/socket.io-java-client).

# Run from Eclipse.
Open the workspace in the root folder of the project.

Import the two subfolders (InfostanderFrontend/ and socket.io-java-client) as projects.

Run as Java Application.

# Compile to jar.
Eclipse:

  - Export -> Java -> Runnable JAR file
  
    - Launch configuration: Infostander - InfostanderFrontend
    - Library handling: Extract required libraries into generated JAR

# To start program
- Make folder infostander/

- Put jar file into infostander/

- Make directory infostander/files

- Copy example.config.properties to infostander/config.properties

- Change relevant entries in config.properties

- Run the .jar file

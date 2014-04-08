# Infostander Frontend in Java
This is a frontend for Infostander written in Java.

The latest compiled jar file is in the jar/ folder.

The project directory is InfostanderFrontend/ .

The socket.io-java-client folder is a modified socket.io-client library (https://github.com/Gottox/socket.io-java-client).

## Run from Eclipse.
Open the workspace in the root folder of the project.

Import the two subfolders (InfostanderFrontend/ and socket.io-java-client) as projects.

Run as Java Application.

## Compile to jar.
Eclipse:

  - Export -> Java -> Runnable JAR file
  
    - Launch configuration: Infostander - InfostanderFrontend
    - Library handling: Extract required libraries into generated JAR

## To start program
- Make folder infostander/

- Put the jar file into infostander/

- Make directory infostander/files

- Copy example.config.properties to infostander/config.properties

- Change relevant entries in config.properties

- Run the .jar file. To run from console:
<pre>
java -jar infostander.jar
</pre>

## To use selfsigned certificate in development
Generate a selfsigned certificate: http://www.selfsignedcertificate.com/

Use this on the server.

To use it in Java: 

Create PKCS12 keystore from private key and public certificate.
<pre>
openssl pkcs12 -export -name myservercert -in selfsigned.cert -inkey selfsigned.key -out keystore.p12
</pre>

Convert PKCS12 keystore into a JKS keystore:
<pre>
keytool -importkeystore -destkeystore [KEYSTORE_FILENAME].jks -srckeystore keystore.p12 -srcstoretype pkcs12 -alias myservercert
</pre>

## To use valid certificate in producion
Open the server frontend location. Save the certificate
<pre>
keytool -genkey -keyalg RSA -sigalg SHA1withRSA -keystore [KEYSTORE_FILENAME].jks -storepass [PASSWORD] -alias truststore 
</pre>

<pre>
keytool -import -trustcacerts -keystore [KEYSTORE_FILENAME].jks -storepass [PASSWORD] -alias [CERTIFICATE_ALIAS] -file [CERTIFICATE_FILENAME]
</pre>

## To use a keystore
Put the keystorefile [KEYSTORE_FILENAME].jks in the files folder.

Change the option truststorefilename
<pre>
secure=true
truststorefilename=[KEYSTORE_FILENAME].jks
truststorepassword=[PASSWORD]
</pre>

## Current issues
No known issues.

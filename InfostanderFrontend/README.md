infostander_frontend_java
=========================

Java version of frontend for Infostander.

## Setup
Copy example.config.properties to config.properties and change relevant settings.

## To run the infostander.jar file
Copy the jar file to a folder. 

Make a directory named files in the folder and copy config.properties to the folder as well.

Execute the infostander.jar file.

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
keytool -importkeystore -destkeystore mykeystore.jks -srckeystore keystore.p12 -srcstoretype pkcs12 -alias myservercert
</pre>

Set the parameter
<pre>
selfsigned=true
</pre>
in config.properties.

Put the file mykeystore.jks in the files folder.
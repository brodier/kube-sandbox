 # Build de l'image
 docker build -t dev-tool  .
 # commande de lancement java dans l'image
 java -cp "lib/*:tools-0.0.1-SNAPSHOT.jar" io.bat4j.tools.Application toto 
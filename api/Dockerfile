# Utiliser une image de base OpenJDK
FROM openjdk:17-jdk-alpine

# Créer un répertoire pour l'application
WORKDIR /app

# Copier le fichier JAR généré dans le conteneur
COPY target/o-b.jar /app/app.jar

# Exposer le port sur lequel l'application va tourner (ex: 8080)
EXPOSE 8080

# Démarrer l'application
ENTRYPOINT ["java", "-jar", "/app/app.jar"]


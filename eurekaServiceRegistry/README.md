Nachdem ich die Dokumentation zum Spring Cloud Netflix- Eureka Client/Serverz überflogen habe, habe ich chatgpt die Teilaufgabe zur Service Registry gegeben, mit dem Wunsch zu zeigen, wie ich Eureka in meinem Spring Cloud Projekt verwenden kann, die Antwort war bereits so umfangreich, dass ich sie hier als README.md eingefügt habe.

Die einzelnen Schritte entsprechen weitgehend der Antwort von chatgpt, bei Intellji habe ich nur anstatt ein neues Projekt anzulegen ein neues Modul erstellt und über den Spring Initializer direkt den Eureka Server eingefügt, so mussten die Abhängigkeiten nicht mehr angepasst werden.

Nach einem ersten erfolgreich Testlauf/Start musste ich nur noch die Test einfügen.

**Auszug Chatgpt Chat**

Hier ist ein Beispiel für die Integration von Spring Cloud Eureka in ein Spring Boot-Projekt. In diesem Beispiel wird ein einfaches Eureka-Server-Projekt erstellt, das die Service-Registrierung und -Entdeckung für andere Microservices ermöglicht.

**Schritt 1: Projekt erstellen und Abhängigkeiten hinzufügen**

Erstellen Sie ein neues Spring Boot-Projekt in IntelliJ IDEA oder einem anderen Java-Entwicklungstool und fügen Sie die erforderlichen Abhängigkeiten hinzu:

```xml
<dependencies>
    <!-- Spring Boot Starter Web for REST API -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>

    <!-- Spring Cloud Eureka Server -->
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-netflix-eureka-server</artifactId>
    </dependency>
</dependencies>
```

**Schritt 2: Eureka-Server konfigurieren**

Konfigurieren Sie den Eureka-Server in Ihrer `application.properties`-Datei:

```properties
# application.properties

spring.application.name=eureka-server
server.port=8761

# Eureka Configuration
eureka.client.register-with-eureka=false
eureka.client.fetch-registry=false
eureka.server.enable-self-preservation=false
eureka.server.eviction-interval-timer-in-ms=60000
```

**Schritt 3: Hauptklasse mit `@EnableEurekaServer` annotieren**

```java
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@EnableEurekaServer
@SpringBootApplication
public class EurekaServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(EurekaServerApplication.class, args);
    }
}
```

**Schritt 4: Projekt ausführen**

Führen Sie Ihr Eureka-Server-Projekt aus. Dies startet den Eureka-Server, der auf Port 8761 lauscht.

**Schritt 5: Service-Registrierung in Microservices**

In Ihren anderen Microservice-Projekten, die sich bei Eureka registrieren sollen, müssen Sie die folgenden Abhängigkeiten hinzufügen:

```xml
<!-- Spring Cloud Eureka Client -->
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
</dependency>
```

Konfigurieren Sie dann den Eureka-Client in den `application.properties`-Dateien Ihrer Microservices:

```properties
# application.properties

# Spring Boot Application Name (Service Name)
spring.application.name=my-service

# Eureka Configuration
eureka.client.service-url.defaultZone=http://localhost:8761/eureka/
```

**Schritt 6: Starten Sie Ihre Microservices**

Starten Sie Ihre Microservices, und sie sollten sich automatisch bei Ihrem Eureka-Server registrieren.

**Schritt 7: Überprüfen Sie die Eureka-Server-Dashboard**

Öffnen Sie einen Webbrowser und navigieren Sie zu `http://localhost:8761`. Sie sollten das Eureka-Server-Dashboard sehen, auf dem die registrierten Microservices aufgelistet sind.

Mit dieser Konfiguration haben Sie erfolgreich Spring Cloud Eureka für die Service-Registrierung und -Entdeckung in Ihrem Microservices-Projekt verwendet. Ihre Microservices können sich jetzt bei Eureka registrieren, und andere Microservices können sie mithilfe von Eureka entdecken und aufrufen.
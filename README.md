# Jobatator java client

This is the java client for the [Jobatator server](https://github.com/lefuturiste/jobatator), a light alternative to RabbitMQ in order to manage queues.

## Requirements

Java 1.8 or higher

## Usage

### Publisher usage

```java
import fr.lefuturiste.jobatator.Client;
import fr.lefuturiste.jobatator.ClientOptions;

public class Publisher {
    public static void main(String[] args) {
        ClientOptions clientOptions = new ClientOptions();
        // full code example, but all theses methods are chainable
        clientOptions.setHost('yourjobatatorinstance.example.com'); // by default 127.0.0.1
        clientOptions.setPort(8962); // by default 8962
        clientOptions.setCredentials("YOUR_USERNAME", "YOUR_PASSWORD");
        clientOptions.setGroup("YOUR_GROUP");
        // build the client
        Client client = clientOptions.getClient();
        client.createConnexion();
        
        // will publish a job named 'my_job_type' on the queue 'my_queue' with a string payload
        client.publish("my_queue", "my_job_type", "my_payload");
    }
}
``` 

### Consumer usage

```java
import fr.lefuturiste.jobatator.Client;
import fr.lefuturiste.jobatator.ClientOptions;

public class Consumer {
    public static void main(String[] args) {
        Client client = new ClientOptions()
            .setUsername("user1")
            .setPassword("pass1")
            .setGroup("group1")
            .getClient();
        client
            .createConnexion()
            .addHandler("my_job_type", payload -> {
                System.out.println("Got job!");
                System.out.println(payload); // will return 'my_payload'
                // process the job
                return true;
            })
            .startWorker("my_queue");
    }
}

```
package fr.lefuturiste.jobatator;

public class App {
    public static void main(String args[]) {
        System.out.println("Starting things");
        Client client = new ClientOptions()
                .setUsername("user1")
                .setPassword("pass1")
                .setGroup("group1")
                .getClient();
        try {
            client.createConnexion().publish("my.job","payload");
            client.publish("my.job","second payload");

            client.addHandler("my.job", payload -> {
                System.out.println("Got job!");
                System.out.println(payload);
                // process the job
                return true;
            });
            client.startWorker("default", 2);


        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

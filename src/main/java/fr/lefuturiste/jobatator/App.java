package fr.lefuturiste.jobatator;

import java.io.IOException;

public class App
{
    public static void main() throws IOException {
        Client client = new ClientOptions()
                .setUsername("user1")
                .setPassword("pass1")
                .setGroup("group1")
                .getClient();
        try {
            client.createConnexion();
            System.out.println(client.ping());
        } catch (IOException | InvalidGroupException | InvalidCredentialsException e) {
            e.printStackTrace();
        }
    }
}

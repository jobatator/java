package fr.lefuturiste.jobatator;

import java.io.*;
import java.net.Socket;

public class Client {
    private Socket socket = null;
    private BufferedReader input = null;
    private PrintWriter output = null;
    private ClientOptions options;

    public Client(ClientOptions options) {
        this.options = options;
    }

    public void createConnexion() throws IOException, InvalidCredentialsException, InvalidGroupException {
        socket = new Socket(options.getHost(), options.getPort());
        input = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
        output = new PrintWriter(this.socket.getOutputStream(), true);
        authenticate();
        useGroup();
    }

    private void authenticate() throws IOException, InvalidCredentialsException {
        this.output.println("AUTH " + options.getUsername() + " " + options.getPassword());
        String result = this.input.readLine();
        if (!result.equals("Welcome!")) {
            destroyConnexion();
            throw new InvalidCredentialsException();
        }
    }

    private void useGroup() throws IOException, InvalidGroupException {
        this.output.println("USE_GROUP " + options.getGroup());
        String result = this.input.readLine();
        if (!result.equals("OK")) {
            destroyConnexion();
            throw new InvalidGroupException();
        }
    }

    public boolean hasConnexion() {
        return socket != null && input != null && output != null;
    }

    public Socket getSocket() {
        return this.socket;
    }

    public ClientOptions getOptions() {
        return options;
    }

    public void destroyConnexion() throws IOException {
        output.close();
        input.close();
        socket.close();
        output = null;
        input = null;
        socket = null;
    }

    public boolean ping() throws IOException {
        output.println("PING");
        return input.readLine().equals("PONG");
    }

    public void publish(String jobType, String payload) throws IOException {
        publish("default", jobType, payload);
    }

    public void publish(String queue, String jobType, String payload) throws IOException {
        output.println("PUBLISH " + queue + " " + jobType + " '" + payload + "'");
        assert input.readLine().equals("OK");
    }

    public void subscribe() throws IOException {
        subscribe("default");
    }

    private void subscribe(String queue) throws IOException {
        output.println("SUBSCRIBE " + queue);
        assert input.readLine().equals("OK");
    }

    public void startWorker(String queue) throws IOException {

    }
}

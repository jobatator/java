package fr.lefuturiste.jobatator;

public class ClientOptions {

    private String host = "127.0.0.1";
    private int port = 8962;
    private String username = "root";
    private String password = "root";
    private String group = "root";

    public ClientOptions setHost(String host) {
        this.host = host;
        return this;
    }

    public ClientOptions setPort(int port) {
        this.port = port;
        return this;
    }

    public ClientOptions setUsername(String username) {
        this.username = username;
        return this;
    }

    public ClientOptions setPassword(String password) {
        this.password = password;
        return this;
    }

    public ClientOptions setGroup(String group) {
        this.group = group;
        return this;
    }

    public Client getClient() {
        return new Client(this);
    }

    public int getPort() {
        return port;
    }

    public String getHost() {
        return host;
    }

    public String getGroup() {
        return group;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}

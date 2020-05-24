package fr.lefuturiste.jobatator;

import org.json.JSONObject;

import java.io.*;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class Client {
    private Socket socket = null;
    private BufferedReader input = null;
    private PrintWriter output = null;
    private final ClientOptions options;
    private final Map<String, HandlerInterface> handlerMap = new HashMap<>();

    public static final String DEFAULT_QUEUE = "default";
    public static final String JOB_DONE = "done";
    public static final String JOB_ERRORED = "errored";

    public Client(ClientOptions options) {
        this.options = options;
    }

    public Client createConnexion() throws IOException, InvalidCredentialsException, InvalidGroupException {
        socket = new Socket(options.getHost(), options.getPort());
        input = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
        output = new PrintWriter(this.socket.getOutputStream(), true);
        authenticate();
        useGroup();
        return this;
    }

    private void send(String message) {
        output.write(message + "\n");
        output.flush();
    }

    private void authenticate() throws IOException, InvalidCredentialsException {
        this.send("AUTH " + options.getUsername() + " " + options.getPassword());
        String result = this.input.readLine();
        if (!result.equals("Welcome!")) {
            destroyConnexion();
            throw new InvalidCredentialsException();
        }
    }

    private void useGroup() throws IOException, InvalidGroupException {
        this.send("USE_GROUP " + options.getGroup());
        String result = this.input.readLine();
        if (!result.equals("OK")) {
            destroyConnexion();
            throw new InvalidGroupException();
        }
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

    public Client ping() throws IOException, InvalidResponseException {
        send("PING");
        if (!input.readLine().equals("PONG"))
            throw new InvalidResponseException("Pong");
        return this;
    }

    public Client publish(String jobType, String payload) throws IOException, InvalidResponseException {
        return publish(DEFAULT_QUEUE, jobType, payload);
    }

    public Client publish(String queue, String jobType, String payload) throws IOException, InvalidResponseException {
        this.send("PUBLISH " + queue + " " + jobType + " '" + payload + "'");
        if (!input.readLine().equals("OK"))
            throw new InvalidResponseException("Publish");
        return this;
    }

    public interface HandlerInterface {
        boolean execute(String payload);
    }

    public Client addHandler(String jobType, HandlerInterface handler) {
        handlerMap.put(jobType, handler);
        return this;
    }

    public void subscribe(String queue) throws IOException, InvalidResponseException {
        send("SUBSCRIBE " + queue);
        if (!input.readLine().equals("OK"))
            throw new InvalidResponseException("Subscribe");
    }

    public void startWorker() throws Exception {
        startWorker(DEFAULT_QUEUE, -1);
    }

    public void startWorker(int jobToProcess) throws Exception {
        startWorker(DEFAULT_QUEUE, jobToProcess);
    }

    public void startWorker(String queue) throws Exception {
        startWorker(queue, -1);
    }

    public void updateJob(String jobId, String status) throws IOException, InvalidResponseException {
        send("UPDATE_JOB " + jobId + " " + status);
        if (!input.readLine().equals("OK"))
            throw new InvalidResponseException("Update job");
    }

    public void startWorker(String queue, int jobToProcess) throws Exception {
        subscribe(queue);
        boolean workerIsRunning = true;
        int jobCount = 0;
        while (workerIsRunning) {
            String jobRaw = input.readLine();
            if (jobRaw.charAt(0) != '{')
                continue;
            JSONObject dispatchData = new JSONObject(jobRaw);
            JSONObject job = dispatchData.getJSONObject("Job");
            if (handlerMap.containsKey(job.getString("Type"))) {
                boolean result = false;
                try {
                    result = handlerMap.get(job.getString("Type"))
                            .execute(job.getString("Payload"));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (result) {
                    updateJob(job.getString("ID"), JOB_DONE);
                } else {
                    updateJob(job.getString("ID"), JOB_ERRORED);
                }
            }
            if (jobToProcess > 0) {
                jobCount++;
                if (jobCount >= jobToProcess)
                    workerIsRunning = false;
            }
        }
    }

    public JSONObject debug() throws IOException {
        send("DEBUG_JSON");
        return new JSONObject(input.readLine());
    }

    public void quit() throws IOException {
        send("QUIT");
        input.readLine();
        destroyConnexion();
    }

    public boolean hasConnexion() {
        return socket != null && input != null && output != null && socket.isConnected();
    }
}

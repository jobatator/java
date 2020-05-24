package fr.lefuturiste.jobatator;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

import static org.junit.Assert.*;

public class ClientTest
{
    private Client getClient() {
        return new ClientOptions()
                .setUsername("user1")
                .setPassword("pass1")
                .setGroup("group1")
                .getClient();
    }

    private Client getConnexion() throws Exception {
        Client client = getClient();
        client.createConnexion();
        return client;
    }

    @Test
    public void shouldBuildClient() {
        ClientOptions options = new ClientOptions();
        Client client = options.getClient();
        options = client.getOptions();
        assertEquals("root", options.getUsername());
        assertEquals("root", options.getPassword());
        assertEquals("root", options.getGroup());

        options = new ClientOptions()
                .setUsername("user1")
                .setPassword("pass1");
        assertEquals("root", options.getGroup());
        assertEquals("127.0.0.1", options.getHost());
        assertEquals(8962, options.getPort());

        options = new ClientOptions().setCredentials("foo", "bar");
        assertEquals("foo", options.getUsername());
        assertEquals("bar", options.getPassword());
    }

    @Test
    public void shouldCreateConnexion() throws Exception {
        Client client = getClient();
        client.createConnexion();
        assertTrue(client.hasConnexion());
    }

    @Test
    public void shouldPing() throws Exception {
        Client client = getConnexion();
        assertTrue(client.ping().hasConnexion());
    }

    @Test
    public void shouldDestroyConnexion() throws Exception {
        Client client = getConnexion();
        assertTrue(client.hasConnexion());
        client.destroyConnexion();
        assertFalse(client.hasConnexion());
        assertNull(client.getSocket());
    }

    @Test
    public void shouldQuit() throws Exception {
        Client client = getConnexion();
        assertTrue(client.hasConnexion());
        client.quit();
        assertFalse(client.hasConnexion());
        assertNull(client.getSocket());
    }

    @Test
    public void shouldPublishAndConsumeJob() throws Exception {
        Client client = getConnexion();
        JSONObject payload = new JSONObject()
                .put("something", 14)
                .put("other", true);
        Client result = client.publish("a.job.type", payload.toString());
        assertEquals(result.getClass(), client.getClass());

        client.addHandler("a.job.type", (String receivedPayload) -> {
            assertNotNull(receivedPayload);
            JSONObject parsedPayload = new JSONObject(receivedPayload);
            assertEquals(14, parsedPayload.getInt("something"));
            assertTrue(parsedPayload.getBoolean("other"));
            return true;
        });
        client.startWorker(1);
    }

    @Test
    public void shouldPublishAndConsumeJobInCustomQueue() throws Exception {
        Client client = getConnexion();
        String payload = "hello_world";
        Client result = client.publish("custom", "another_job_type", payload);
        assertEquals(result.getClass(), client.getClass());
        client.addHandler("another_job_type", (String receivedPayload) -> {
            assertEquals("hello_world", receivedPayload);
            return true;
        }).startWorker("custom", 1);

        JSONObject debug = client.debug();
        JSONArray queues = debug.getJSONArray("Queues");
        JSONObject queue = null;
        for (int i = 0; i < queues.length(); i++) {
            if (queues.getJSONObject(i).getString("Slug").equals("custom"))
                queue = queues.getJSONObject(i);
        }
        assertNotNull(queue);
        assertEquals("custom", queue.getString("Slug"));
        JSONObject job = queue.getJSONArray("Jobs").getJSONObject(0);
        assertEquals(Client.JOB_DONE, job.getString("State"));
    }

    @Test
    public void shouldPublishAndConsumeErroredJob() throws Exception {
        String payload = "This is an errored job";
        Client client = getConnexion().publish("errored_queue", "job.errored", payload);
        client.addHandler("job.errored", (String receivedPayload) -> {
            assertEquals(payload, receivedPayload);
            return false;
        }).startWorker("errored_queue", 1);

        JSONObject debug = client.debug();
        JSONArray queues = debug.getJSONArray("Queues");
        JSONObject queue = null;
        for (int i = 0; i < queues.length(); i++)
            if (queues.getJSONObject(i).getString("Slug").equals("errored_queue"))
                queue = queues.getJSONObject(i);
        assertNotNull(queue);
        assertEquals("errored_queue", queue.getString("Slug"));

        JSONArray jobs = queue.getJSONArray("Jobs");
        JSONObject job = null;
        for (int i = 0; i < jobs.length(); i++)
            if (jobs.getJSONObject(i).getString("State").equals(Client.JOB_ERRORED))
                job = jobs.getJSONObject(i);
        assertNotNull(job);
        assertEquals(Client.JOB_ERRORED, job.getString("State"));
        client.updateJob(job.getString("ID"), Client.JOB_DONE);
    }
}

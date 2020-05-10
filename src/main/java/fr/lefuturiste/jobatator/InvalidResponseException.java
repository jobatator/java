package fr.lefuturiste.jobatator;

public class InvalidResponseException extends Exception {
    InvalidResponseException() {
        super("There were a invalid response from the jobatator server, no additional debug");
    }

    InvalidResponseException(String command) {
        super("There were a invalid response from the jobatator server, with the command " + command);
    }
}

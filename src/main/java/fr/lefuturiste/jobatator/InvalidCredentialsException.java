package fr.lefuturiste.jobatator;

public class InvalidCredentialsException extends Exception {
    InvalidCredentialsException() {
        super("Can't create connexion to Jobatator server because of invalid credentials");
    }
}

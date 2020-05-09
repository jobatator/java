package fr.lefuturiste.jobatator;

public class InvalidGroupException extends Exception {
    InvalidGroupException() {
        super("Can't create connexion to Jobatator server because of unauthorized or invalid group");
    }
}

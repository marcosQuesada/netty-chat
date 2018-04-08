package com.marcosquesada.netty.chat.server.auth;


public class NopAuthenticator implements Authenticator{

    // Do nothing Authenticator
    public NopAuthenticator(){
    }

    // Validate all credentials
    public boolean validateCredentials(String user, String pass) {
        return true;
    }
}

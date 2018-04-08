package com.marcosquesada.netty.chat.server.auth;

public interface Authenticator {

    boolean validateCredentials(String user, String pass);

}

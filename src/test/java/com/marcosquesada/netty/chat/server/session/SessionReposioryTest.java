package com.marcosquesada.netty.chat.server.session;

import org.junit.Assert;
import org.junit.Test;

public class SessionReposioryTest {

    @Test
    public void BasicSessionRepositoryWorkFlow(){
        InMemoryRepository repository = new InMemoryRepository();

        Session session = InMemoryRepository.buildSession(null, "foo");
        repository.add(session);

        Assert.assertTrue(repository.contains("foo"));
        Assert.assertTrue(session.equals(repository.get("foo")));

        repository.remove("foo");

        Assert.assertFalse(repository.contains("foo"));
    }
}

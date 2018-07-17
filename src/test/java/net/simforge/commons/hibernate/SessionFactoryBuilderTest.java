package net.simforge.commons.hibernate;

import org.junit.Test;

import static org.junit.Assert.fail;

public class SessionFactoryBuilderTest {
    @Test
    public void forDatabaseThrowsNPEWhenNonRequiredParameterAbsent() {
        try {
            SessionFactoryBuilder.forDatabase("no-username");
        } catch (NullPointerException e) {
            fail("NPE happens");
        }
    }
}
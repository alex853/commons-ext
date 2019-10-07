package net.simforge.commons.hibernate;

import net.simforge.commons.legacy.misc.Settings;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public class SessionFactoryBuilder {

    private Configuration configuration = new Configuration();

    private SessionFactoryBuilder() {
    }

    public static SessionFactoryBuilder forDatabase(String databaseName) {
        SessionFactoryBuilder builder = new SessionFactoryBuilder();
        Configuration configuration = builder.configuration;

        String driverClass = Settings.get(databaseName + ".db.driver-class");
        String url = Settings.get(databaseName + ".db.url");
        String username = Settings.get(databaseName + ".db.username");
        String password = Settings.get(databaseName + ".db.password");
        String poolSize = Settings.get(databaseName + ".db.pool-size");

        configuration.setProperty("hibernate.connection.driver_class", driverClass);
        configuration.setProperty("hibernate.connection.url", url);

        if (username != null)
            configuration.setProperty("hibernate.connection.username", username);

        if (password != null)
            configuration.setProperty("hibernate.connection.password", password);

        if (poolSize != null)
            configuration.setProperty("hibernate.connection.pool_size", poolSize);

        configuration.setInterceptor(new AuditInterceptor());

        return builder;
    }

    public SessionFactoryBuilder entities(Class[] entities) {
        for (Class entityClass : entities) {
            configuration.addAnnotatedClass(entityClass);
        }

        return this;
    }

    public SessionFactoryBuilder createSchemaIfNeeded() {
        configuration.setProperty("hibernate.hbm2ddl.auto", "create");
        return this;
    }

    public SessionFactoryBuilder updateSchemaIfNeeded() {
        configuration.setProperty("hibernate.hbm2ddl.auto", "update");
        return this;
    }

    public SessionFactory build() {
        return configuration.buildSessionFactory();
    }

}

package net.simforge.commons.hibernate;

import net.simforge.commons.legacy.BM;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HibernateUtils {
    private static Logger logger = LoggerFactory.getLogger(HibernateUtils.class);

    public static void transaction(Session session, Runnable action) {
        transaction(session, null, action);
    }

    public static void transaction(Session session, String bmPoint, Runnable action) {
        BM.start(bmPoint != null ? bmPoint : "HibernateUtils.transaction");
        try {
            session.getTransaction().begin();


            action.run();


            BM.start(bmPoint != null ? bmPoint + "/commit" : "HibernateUtils.transaction/commit");
            try {
                session.getTransaction().commit();
            } finally {
                BM.stop();
            }
        } catch (HibernateException e) {
            logger.error("Error on transaction", e);
            rollback(session, bmPoint);
            throw new RuntimeException("Error on transaction", e);
        } finally {
            BM.stop();
        }
    }

    public static void rollback(Session session) {
        rollback(session, null);
    }

    public static void rollback(Session session, String bmPoint) {
        BM.start(bmPoint != null ? bmPoint : "HibernateUtils.rollback");
        try {
            session.getTransaction().rollback();
        } catch (HibernateException e) {
            logger.error("Error while rolling back session", e);
        } finally {
            BM.stop();
        }
    }

    public static void updateAndCommit(Session session, Object... objects) {
        updateAndCommit(session, null, objects);
    }

    public static void updateAndCommit(Session session, String bmPoint, Object... objects) {
        transaction(session, bmPoint, () -> {
            for (Object object : objects) {
                if (object == null) {
                    continue;
                }

                session.update(object);
            }
        });
    }

    public static void saveAndCommit(Session session, Object... objects) {
        saveAndCommit(session, null, objects);
    }

    public static void saveAndCommit(Session session, String bmPoint, Object... objects) {
        transaction(session, bmPoint, () -> {
            for (Object object : objects) {
                if (object == null) {
                    continue;
                }

                session.save(object);
            }
        });
    }
}

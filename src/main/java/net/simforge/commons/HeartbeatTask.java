package net.simforge.commons;

import net.simforge.commons.hibernate.HibernateUtils;
import net.simforge.commons.legacy.BM;
import net.simforge.commons.misc.JavaTime;
import net.simforge.commons.runtime.BaseTask;
import net.simforge.commons.runtime.ThreadMonitor;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * It processes entities by heartbeatDt field.
 * Entities with heartbeatDt = null are out of processing queue.
 */
public abstract class HeartbeatTask<T extends HeartbeatObject> extends BaseTask {
    private final String entityName;
    private final SessionFactory sessionFactory;
    private int batchSize = 100;

    protected HeartbeatTask(String entityName, SessionFactory sessionFactory) {
        this(entityName, entityName, sessionFactory);
    }

    protected HeartbeatTask(String entityName, String loggerName, SessionFactory sessionFactory) {
        super(loggerName);
        this.entityName = entityName;
        this.sessionFactory = sessionFactory;
    }

    @Override
    protected void process() {
        if (entityName == null) { // todo AK BM
            throw new Error("Entity name should be specified");
        }

        List<T> objects = loadNextBatch();

        if (objects == null || objects.isEmpty()) {
            logger.debug("No objects found");
            return;
        }

        logger.debug("Processing queue: " + objects.size());

        for (T object : objects) {
            ThreadMonitor.alive();

            if (ThreadMonitor.isStopRequested()) {
                logger.info("Stop requested");
                break;
            }

            logger.debug("Heartbeat for " + object);

            LocalDateTime before = object.getHeartbeatDt();

            try {
                object = heartbeat(object);
            } catch (Throwable t) {
                logger.error("Error during Heartbeat for " + object, t);
            }

            LocalDateTime after = object.getHeartbeatDt();

            if (after != null
                    && (after.equals(before)
                    || after.isBefore(after))) {
                logger.warn(String.format("HeartbeatDt for %s is not changed or changed in wrong way: before %s, after %s", object, before, after));
            }
        }
    }

    private List<T> loadNextBatch() {
        BM.start("HeartbeatTask.loadNextBatch");
        try (Session session = sessionFactory.openSession()) {
            //noinspection JpaQlInspection
            String query = "select e from ENTITY e where e.heartbeatDt <= :dt order by e.heartbeatDt asc";
            query = query.replace("ENTITY", entityName);

            //noinspection ConstantConditions,unchecked
            return session
                    .createQuery(query)
                    .setParameter("dt", JavaTime.nowUtc())
                    .setMaxResults(batchSize)
                    .list();
        } finally {
            BM.stop();
        }
    }

    protected abstract T heartbeat(T object);

    protected void setNextHeartbeatDtInMillis(Session session, T object, long millisToNextHeartbeat) {
        LocalDateTime heartbeatDt = JavaTime.nowUtc().plus(millisToNextHeartbeat, ChronoUnit.MILLIS);
        object.setHeartbeatDt(heartbeatDt);

        HibernateUtils.updateAndCommit(session, "HeartbeatTask.setNextHeartbeatDtInMillis", object);
    }

    protected void setNextHeartbeatDt(Session session, T object, LocalDateTime nextHearbeatDt) {
        object.setHeartbeatDt(nextHearbeatDt);

        HibernateUtils.updateAndCommit(session, "HeartbeatTask.setHeartbeatDt", object);
    }

    public int getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }
}

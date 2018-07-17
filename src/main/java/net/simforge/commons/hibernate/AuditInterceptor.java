package net.simforge.commons.hibernate;

import net.simforge.commons.misc.JavaTime;
import org.hibernate.EmptyInterceptor;
import org.hibernate.type.Type;

import java.io.Serializable;

public class AuditInterceptor extends EmptyInterceptor {
    @Override
    public boolean onFlushDirty(Object entity, Serializable id, Object[] currentState, Object[] previousState, String[] propertyNames, Type[] types) {
        if (entity instanceof Auditable) {
            return updateAuditFields(currentState, propertyNames, false, true);
        }
        return false;
    }

    @Override
    public boolean onSave(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types) {
        if (entity instanceof Auditable) {
            return updateAuditFields(state, propertyNames, true, true);
        }
        return false;
    }

    private boolean updateAuditFields(Object[] state, String[] propertyNames, boolean setCreateDt, boolean setModifyDt) {
        boolean result = false;
        for (int i = 0; i < propertyNames.length; i++) {
            if (setCreateDt && "createDt".equals(propertyNames[i])) {
                state[i] = JavaTime.nowUtc();
                result = true;
            } else if (setModifyDt && "modifyDt".equals(propertyNames[i])) {
                state[i] = JavaTime.nowUtc();
                result = true;
            }
        }
        return result;
    }
}

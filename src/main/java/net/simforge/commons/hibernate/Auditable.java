package net.simforge.commons.hibernate;

import java.time.LocalDateTime;

public interface Auditable {
    LocalDateTime getCreateDt();

    LocalDateTime getModifyDt();
}

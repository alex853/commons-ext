package net.simforge.commons;

import java.time.LocalDateTime;

public interface HeartbeatObject {

    LocalDateTime getHeartbeatDt();

    void setHeartbeatDt(LocalDateTime heartbeatDt);

}

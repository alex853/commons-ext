package net.simforge.commons.hibernate;

public interface BaseEntity {
    Integer getId();

    void setId(Integer id);

    Integer getVersion();

    void setVersion(Integer version);
}

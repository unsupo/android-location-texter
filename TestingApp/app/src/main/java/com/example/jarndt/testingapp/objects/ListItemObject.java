package com.example.jarndt.testingapp.objects;

import android.location.Location;

import org.joda.time.DateTime;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Created by jarndt on 8/2/17.
 */

public class ListItemObject {
    private Location location;
    private LocalDateTime createDate, lastUpdatedDate;
    private boolean active = true;
    private long intervalSeconds;
    private String name, smsNumber, message, id;

    public ListItemObject() {
        this.createDate = LocalDateTime.now();
        this.id = UUID.randomUUID().toString();
        this.name = "name: "+id;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public LocalDateTime getCreateDate() {
        return createDate;
    }

    public void setCreateDate(LocalDateTime createDate) {
        this.createDate = createDate;
    }

    public LocalDateTime getLastUpdatedDate() {
        return lastUpdatedDate;
    }

    public void setLastUpdatedDate(LocalDateTime lastUpdatedDate) {
        this.lastUpdatedDate = lastUpdatedDate;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public long getIntervalSeconds() {
        return intervalSeconds;
    }

    public void setIntervalSeconds(long intervalSeconds) {
        this.intervalSeconds = intervalSeconds;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSmsNumber() {
        return smsNumber;
    }

    public void setSmsNumber(String smsNumber) {
        this.smsNumber = smsNumber;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ListItemObject that = (ListItemObject) o;

        if (active != that.active) return false;
        if (intervalSeconds != that.intervalSeconds) return false;
        if (location != null ? !location.equals(that.location) : that.location != null)
            return false;
        if (createDate != null ? !createDate.equals(that.createDate) : that.createDate != null)
            return false;
        if (lastUpdatedDate != null ? !lastUpdatedDate.equals(that.lastUpdatedDate) : that.lastUpdatedDate != null)
            return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (smsNumber != null ? !smsNumber.equals(that.smsNumber) : that.smsNumber != null)
            return false;
        if (message != null ? !message.equals(that.message) : that.message != null) return false;
        return id != null ? id.equals(that.id) : that.id == null;

    }

    @Override
    public int hashCode() {
        int result = location != null ? location.hashCode() : 0;
        result = 31 * result + (createDate != null ? createDate.hashCode() : 0);
        result = 31 * result + (lastUpdatedDate != null ? lastUpdatedDate.hashCode() : 0);
        result = 31 * result + (active ? 1 : 0);
        result = 31 * result + (int) (intervalSeconds ^ (intervalSeconds >>> 32));
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (smsNumber != null ? smsNumber.hashCode() : 0);
        result = 31 * result + (message != null ? message.hashCode() : 0);
        result = 31 * result + (id != null ? id.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return name;
    }
}

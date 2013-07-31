package com.devicehive.messages.data.hash.subscriptions.dao;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.enterprise.context.Dependent;

import com.devicehive.messages.data.subscriptions.model.NotificationsSubscription;
import com.devicehive.model.Device;

@Dependent
public class NotificationSubscriptionDAO implements com.devicehive.messages.data.subscriptions.dao.NotificationSubscriptionDAO {

    private long counter = 0L;

    private Map<Key, NotificationsSubscription> keyToObject = new HashMap<>();
    private Map<Long, Set<NotificationsSubscription>> deviceToObject = new HashMap<>();
    private Map<String, Set<NotificationsSubscription>> sessionToObject = new HashMap<>();

    public NotificationSubscriptionDAO() {
    }

    @Override
    public synchronized void insertSubscriptions(Collection<Long> deviceIds, String sessionId) {
        if (sessionId != null) {
            if (deviceIds == null || deviceIds.isEmpty()) {
                insertSubscriptions((Long) null, sessionId);
            }
            else {
                for (Long deviceId : deviceIds) {
                    deleteByDeviceAndSession(deviceId, sessionId);
                    insertSubscriptions(deviceId, sessionId);
                }
            }
        }
    }

    @Override
    public synchronized void insertSubscriptions(Long deviceId, String sessionId) {
        NotificationsSubscription entity = new NotificationsSubscription(deviceId, sessionId);
        entity.setId(Long.valueOf(counter));

        Key key = new Key(deviceId, sessionId);
        keyToObject.put(key, entity);

        Set<NotificationsSubscription> deviceRecords = deviceToObject.get(deviceId);
        if (deviceRecords == null) {
            deviceRecords = new HashSet<>();
        }
        deviceRecords.add(entity);
        deviceToObject.put(deviceId, deviceRecords);

        Set<NotificationsSubscription> sessionRecords = sessionToObject.get(sessionId);
        if (sessionRecords == null) {
            sessionRecords = new HashSet<>();
        }
        sessionRecords.add(entity);
        sessionToObject.put(sessionId, sessionRecords);

        ++counter;
    }

    @Override
    public void insertSubscriptions(String sessionId) {
        insertSubscriptions((Long) null, sessionId);
    }

    @Override
    public synchronized void deleteBySession(String sessionId) {
        Set<NotificationsSubscription> entities = sessionToObject.remove(sessionId);
        if (entities != null) {
            for (NotificationsSubscription entity : entities) {
                deviceToObject.remove(entity.getDeviceId());
                keyToObject.remove(new Key(entity.getDeviceId(), sessionId));
            }
        }
    }

    @Override
    public synchronized void deleteByDeviceAndSession(Long deviceId, String sessionId) {
        NotificationsSubscription entity = keyToObject.remove(new Key(deviceId, sessionId));
        if (entity != null) {
            deviceToObject.get(entity.getDeviceId()).remove(entity);
            sessionToObject.get(entity.getSessionId()).remove(entity);;
        }
    }

    @Override
    public synchronized void deleteByDeviceAndSession(Device device, String sessionId) {
        deleteByDeviceAndSession(device.getId(), sessionId);
    }

    @Override
    public synchronized void deleteByDevice(Long deviceId) {
        Set<NotificationsSubscription> entities = deviceToObject.remove(deviceId);
        if (entities != null) {
            for (NotificationsSubscription entity : entities) {
                sessionToObject.get(entity.getSessionId()).remove(entity);
                keyToObject.remove(new Key(deviceId, entity.getSessionId()));
            }
        }
    }

    @Override
    public synchronized List<String> getSessionIdSubscribedForAll() {
        return getSessionIdSubscribedByDevice(null);
    }

    @Override
    public synchronized List<String> getSessionIdSubscribedByDevice(Long deviceId) {
        Set<NotificationsSubscription> records = deviceToObject.get(deviceId);
        if (records != null) {
            List<String> sessions = new ArrayList<>(records.size());
            for (NotificationsSubscription entity : records) {
                sessions.add(entity.getSessionId());
            }
            return sessions;
        }
        return Collections.emptyList();
    }

    private class Key {
        final Long deviceId;
        final String sessionId;

        public Key(Long deviceId, String sessionId) {
            this.deviceId = deviceId;
            this.sessionId = sessionId;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + getOuterType().hashCode();
            result = prime * result + ((deviceId == null) ? 0 : deviceId.hashCode());
            result = prime * result + ((sessionId == null) ? 0 : sessionId.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            Key other = (Key) obj;
            if (!getOuterType().equals(other.getOuterType()))
                return false;
            if (deviceId == null) {
                if (other.deviceId != null)
                    return false;
            }
            else if (!deviceId.equals(other.deviceId))
                return false;
            if (sessionId == null) {
                if (other.sessionId != null)
                    return false;
            }
            else if (!sessionId.equals(other.sessionId))
                return false;
            return true;
        }

        private NotificationSubscriptionDAO getOuterType() {
            return NotificationSubscriptionDAO.this;
        }

    }

}
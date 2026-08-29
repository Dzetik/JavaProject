package com.example.demo.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

@Service
public class WebSocketSessionService {
    private final TaskScheduler taskScheduler;
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final Map<String, ScheduledFuture<?>> expirationTasks = new ConcurrentHashMap<>();

    public WebSocketSessionService(@Qualifier("jwtExpirationTaskScheduler") TaskScheduler taskScheduler) {
        this.taskScheduler = taskScheduler;
    }

    public void addSession(WebSocketSession session) {
        sessions.put(session.getId(), session);
        System.out.println("WebSocket session opened: " + session.getId());
    }

    public void registerSession(String sessionId, Date expiration) {
        WebSocketSession session = sessions.get(sessionId);

        if (session == null) {
            System.out.println("WebSocket session not found: " + sessionId);
            return;
        }

        long delay = expiration.getTime() - System.currentTimeMillis();
        if (delay <= 0) {
            closeSession(sessionId);
            return;
        }

        ScheduledFuture<?> future =
                taskScheduler.schedule(
                        () -> closeSession(sessionId),
                        expiration.toInstant()
                );

        expirationTasks.put(sessionId, future);
        System.out.println("WebSocket session registered: " + sessionId + ", expires at: " + expiration);
    }

    public void removeSession(String sessionId) {
        sessions.remove(sessionId);

        ScheduledFuture<?> future = expirationTasks.remove(sessionId);
        if (future != null) {
            future.cancel(false);
        }

        System.out.println("WebSocket session removed: " + sessionId);
    }

    private void closeSession(String sessionId) {
        WebSocketSession session = sessions.remove(sessionId);

        ScheduledFuture<?> future = expirationTasks.remove(sessionId);
        if (future != null) {
            future.cancel(false);
        }

        if (session != null && session.isOpen()) {
            try {
                System.out.println("JWT expired. Closing WebSocket session: " + sessionId);
                session.close(CloseStatus.POLICY_VIOLATION);
            }
            catch (Exception e) {
                System.out.println("Failed to close WebSocket session: " + sessionId);
            }
        }
    }
}
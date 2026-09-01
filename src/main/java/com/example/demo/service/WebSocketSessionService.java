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
    // хранилище запланированных задач на очистку
    private final TaskScheduler taskScheduler;
    // хранилище активных WebSocket-сессий
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    // хранилище ссылок на запланированные задачи на очистку для согласованности при отключении сессии раньше срока
    private final Map<String, ScheduledFuture<?>> expirationTasks = new ConcurrentHashMap<>();
    // JWT expiration, полученный во время STOMP CONNECT,до момента появления физической WebSocket-сессии.
    private final Map<String, Date> pendingExpirations = new ConcurrentHashMap<>();

    public WebSocketSessionService(@Qualifier("jwtExpirationTaskScheduler") TaskScheduler taskScheduler) {
        this.taskScheduler = taskScheduler;
    }

    public void addSession(WebSocketSession session) {
        String sessionId = session.getId();
        sessions.put(sessionId, session);

        Date expiration = pendingExpirations.remove(sessionId);
        if (expiration != null) {
            registerExpirationTask(sessionId, expiration);
        }

        System.out.println("WebSocket session opened: " + sessionId);
    }

    public void registerSession(String sessionId, Date expiration) {
        WebSocketSession session = sessions.get(sessionId);

        if (session == null) {
            pendingExpirations.put(sessionId, expiration);

            System.out.println("WebSocket session is not established yet: " + sessionId);
            return;
        }

        registerExpirationTask(sessionId, expiration);
    }

    private void registerExpirationTask(String sessionId, Date expiration) {
        long delay = expiration.getTime() - System.currentTimeMillis();
        if (delay <= 0) {
            closeSession(sessionId);
            return;
        }

        ScheduledFuture<?> future = taskScheduler.schedule(
                        () -> closeSession(sessionId),
                        expiration.toInstant()
                );

        expirationTasks.put(sessionId, future);

        System.out.println("WebSocket session registered: " + sessionId + ", expires at: " + expiration);
    }

    public void removeSession(String sessionId) {
        sessions.remove(sessionId);
        pendingExpirations.remove(sessionId);
        ScheduledFuture<?> future = expirationTasks.remove(sessionId);

        if (future != null) {
            future.cancel(false);
        }

        System.out.println("WebSocket session removed: " + sessionId);
    }

    private void closeSession(String sessionId) {
        pendingExpirations.remove(sessionId);
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
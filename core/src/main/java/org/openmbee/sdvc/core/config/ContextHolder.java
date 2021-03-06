package org.openmbee.sdvc.core.config;

public class ContextHolder {

    private static final ThreadLocal<ContextObject> contextHolder = new ThreadLocal<>();

    private ContextHolder() {
    }

    public static ContextObject getContext() {
        return contextHolder.get() != null ? contextHolder.get() : new ContextObject();
    }

    public static void setContext(String projectId) {
        contextHolder.set(new ContextObject(projectId));
    }

    public static void setContext(String projectId, String refId) {
        contextHolder.set(new ContextObject(projectId, refId));
    }

    public static void clearContext() {
        contextHolder.remove();
    }
}

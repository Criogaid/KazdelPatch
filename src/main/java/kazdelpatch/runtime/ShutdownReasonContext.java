package kazdelpatch.runtime;

public final class ShutdownReasonContext {

    private static volatile String shutdownReason;

    private ShutdownReasonContext() {
    }

    public static void setReason(String reason) {
        shutdownReason = reason;
    }

    public static void clear() {
        shutdownReason = null;
    }

    public static String consumeReason() {
        String current = shutdownReason;
        shutdownReason = null;
        return current;
    }
}

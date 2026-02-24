package kazdelpatch.runtime;

public final class ServerThreadContext {

    private static volatile Thread serverThread;

    private ServerThreadContext() {
    }

    public static void captureCurrentThread() {
        serverThread = Thread.currentThread();
    }

    public static boolean isServerThreadCurrent() {
        Thread captured = serverThread;
        return captured != null && captured == Thread.currentThread();
    }
}

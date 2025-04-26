package bgu.spl.mics;

import java.util.concurrent.ConcurrentHashMap;

/**
 * The MicroService is an abstract class that any micro-service in the system
 * must extend. The abstract MicroService class is responsible for interacting with
 * the singleton {@link MessageBus} instance.
 *
 * <p>
 * Derived classes of MicroService should never directly access the message bus.
 * Instead, they should use protected wrapper methods (e.g., {@link #sendBroadcast(Broadcast)}, {@link #sendEvent(Event)},
 * etc.) to interact with the system. Subscribing to message types is done via the {@link Callback}
 * interface, which specifies the action to perform when a message is received.
 * </p>
 *
 * <p>
 * Only private fields and methods may be added to this class.
 * </p>
 */
public abstract class MicroService implements Runnable {

    private boolean terminated = false;
    private final String name;
    private final ConcurrentHashMap<Class<? extends Message>, Callback<?>> messageToCallBack;

    /**
     * Constructs a new MicroService with the given name.
     *
     * @param name The name of the micro-service (mainly used for debugging purposes).
     * @pre name != null
     * @post this.name.equals(name)
     */
    public MicroService(String name) {
        this.name = name;
        this.messageToCallBack = new ConcurrentHashMap<>();
    }

    /**
     * Subscribes to events of a specific type and associates a callback with them.
     *
     * @param <E>      The type of event to subscribe to.
     * @param <T>      The type of result expected for the subscribed event.
     * @param type     The class representing the type of event to subscribe to.
     * @param callback The callback to execute when the event is received.
     * @pre type != null && callback != null
     * @post messageToCallBack.containsKey(type)
     */
    protected final <T, E extends Event<T>> void subscribeEvent(Class<E> type, Callback<E> callback) {
        MessageBusImpl.getInstance().subscribeEvent(type, this);
        this.messageToCallBack.putIfAbsent(type, callback);
    }

    /**
     * Subscribes to broadcast messages of a specific type and associates a callback with them.
     *
     * @param <B>      The type of broadcast message to subscribe to.
     * @param type     The class representing the type of broadcast to subscribe to.
     * @param callback The callback to execute when the broadcast is received.
     * @pre type != null && callback != null
     * @post messageToCallBack.containsKey(type)
     */
    protected final <B extends Broadcast> void subscribeBroadcast(Class<B> type, Callback<B> callback) {
        MessageBusImpl.getInstance().subscribeBroadcast(type, this);
        this.messageToCallBack.putIfAbsent(type, callback);
    }

    /**
     * Sends an event using the message bus and receives a {@link Future} object.
     *
     * @param <T> The type of result expected from the event.
     * @param e   The event to send.
     * @return A {@link Future} object that may resolve later with a result.
     *         Returns {@code null} if no micro-service is subscribed to the event type.
     * @pre e != null
     * @post event is sent to the message bus.
     */
    protected final <T> Future<T> sendEvent(Event<T> e) {
        return MessageBusImpl.getInstance().sendEvent(e);
    }

    /**
     * Sends a broadcast message to all subscribed micro-services.
     *
     * @param b The broadcast message to send.
     * @pre b != null
     * @post All subscribed micro-services receive the broadcast message.
     */
    protected final void sendBroadcast(Broadcast b) {
        MessageBusImpl.getInstance().sendBroadcast(b);
    }

    /**
     * Completes an event with the given result.
     *
     * @param <T>    The type of the expected result.
     * @param e      The event to complete.
     * @param result The result to resolve the relevant {@link Future} object.
     * @pre e != null && result != null
     * @post The future associated with the event is resolved.
     */
    protected final <T> void complete(Event<T> e, T result) {
        MessageBusImpl.getInstance().complete(e, result);
    }

    /**
     * Initializes the micro-service. This method must be implemented by derived classes
     * to define specific initialization behavior.
     *
     * @pre The micro-service is registered to the message bus.
     * @post The micro-service is ready to handle messages.
     */
    protected abstract void initialize();

    /**
     * Signals the event loop that it must terminate after handling the current message.
     *
     * @pre The micro-service is running.
     * @post The micro-service will exit after processing the current message.
     */
    protected final void terminate() {
        this.terminated = true;
    }

    /**
     * Retrieves the name of the micro-service.
     *
     * @return The name of the micro-service.
     * @pre none
     * @post result != null
     */
    public final String getName() {
        return name;
    }

    /**
     * The entry point for the micro-service.
     *
     * <p>
     * Registers the micro-service with the message bus, initializes it, and
     * begins processing messages in an event loop until termination is signaled.
     * </p>
     *
     * @pre The micro-service is not already running.
     * @post The micro-service is unregistered from the message bus upon termination.
     */
    @Override
    public final void run() {
        MessageBusImpl.getInstance().register(this);
        initialize();
        while (!terminated) {
            try {
                Message mes = MessageBusImpl.getInstance().awaitMessage(this);

                if (this.messageToCallBack.containsKey(mes.getClass())) {
                    Callback<Message> callback = (Callback<Message>) this.messageToCallBack.get(mes.getClass());
                    callback.call(mes);
                }

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        MessageBusImpl.getInstance().unregister(this);
    }
}

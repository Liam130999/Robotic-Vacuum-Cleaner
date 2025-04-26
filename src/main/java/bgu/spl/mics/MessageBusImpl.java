package bgu.spl.mics;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * The {@link MessageBusImpl} class is the implementation of the {@link MessageBus} interface.
 * <p>
 * It provides mechanisms for communication between {@link MicroService} instances using {@link Event}
 * and {@link Broadcast} messages. It ensures safe and synchronized delivery of messages.
 */
public class MessageBusImpl implements MessageBus {

	private final ConcurrentHashMap<MicroService, BlockingQueue<Message>> eachServiceMap;
	private final ConcurrentHashMap<Class<? extends Message>, List<MicroService>> messageRecieversMap;
	private final ConcurrentHashMap<Event<?>, Future<?>> eventToFutureMap;

	private static class SingletonHolder {
		private static final MessageBusImpl instance = new MessageBusImpl();
	}

	/**
	 * Private constructor for MessageBusImpl.
	 * <p>
	 * Initializes the internal data structures for managing message queues,
	 * subscribers, and event-future mappings.
	 *
	 * @pre None
	 * @post eachServiceMap != null && messageRecieversMap != null && eventToFutureMap != null
	 */
	private MessageBusImpl() {
		this.eachServiceMap = new ConcurrentHashMap<>();
		this.messageRecieversMap = new ConcurrentHashMap<>();
		this.eventToFutureMap = new ConcurrentHashMap<>();
	}

	/**
	 * Provides access to the singleton instance of {@link MessageBusImpl}.
	 *
	 * @return The singleton instance of {@link MessageBusImpl}.
	 * @pre None
	 * @post result != null
	 */
	public static MessageBusImpl getInstance() {
		return SingletonHolder.instance;
	}

	/**
	 * Subscribes a {@link MicroService} to a specific type of {@link Event}.
	 *
	 * @param type The class type of the {@link Event} to subscribe to.
	 * @param m    The {@link MicroService} subscribing to the {@link Event}.
	 * @pre type != null && m != null
	 * @post messageRecieversMap.get(type).contains(m)
	 */
	@Override
	public <T> void subscribeEvent(Class<? extends Event<T>> type, MicroService m) {
		this.messageRecieversMap.putIfAbsent(type, new ArrayList<>());
		this.messageRecieversMap.get(type).add(m);
	}

	/**
	 * Subscribes a {@link MicroService} to a specific type of {@link Broadcast}.
	 *
	 * @param type The class type of the {@link Broadcast} to subscribe to.
	 * @param m    The {@link MicroService} subscribing to the {@link Broadcast}.
	 * @pre type != null && m != null
	 * @post messageRecieversMap.get(type).contains(m)
	 */
	@Override
	public void subscribeBroadcast(Class<? extends Broadcast> type, MicroService m) {
		this.messageRecieversMap.putIfAbsent(type, new ArrayList<>());
		this.messageRecieversMap.get(type).add(m);
	}

	/**
	 * Completes an {@link Event} with a given result.
	 *
	 * @param e      The {@link Event} to complete.
	 * @param result The result associated with the completed {@link Event}.
	 * @pre e != null && eventToFutureMap.containsKey(e)
	 * @post eventToFutureMap.get(e).isDone()
	 */
	@Override
	public <T> void complete(Event<T> e, T result) {
		if (this.eventToFutureMap.containsKey(e)) {
			Future<T> future = (Future<T>) this.eventToFutureMap.get(e);
			future.resolve(result);
		}
	}

	/**
	 * Sends a {@link Broadcast} message to all subscribed {@link MicroService} instances.
	 *
	 * @param b The {@link Broadcast} message to send.
	 * @pre b != null
	 * @post All subscribed MicroServices have the Broadcast in their queue.
	 */
	@Override
	public void sendBroadcast(Broadcast b) {
		for (MicroService m : this.messageRecieversMap.get(b.getClass())) {
			try {
				if (this.eachServiceMap.isEmpty()) {
					return;
				} else {
					if (m != null) {
						this.eachServiceMap.get(m).put(b);
					}
				}
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}
	}

	/**
	 * Sends an {@link Event} to one of the subscribed {@link MicroService} instances.
	 *
	 * @param e The {@link Event} to send.
	 * @return A {@link Future} object representing the result of the Event, or {@code null} if no subscriber is available.
	 * @pre e != null
	 * @post eventToFutureMap.containsKey(e)
	 */
	@Override
	public <T> Future<T> sendEvent(Event<T> e) {
		if (!this.messageRecieversMap.containsKey(e.getClass()) ||
				(this.messageRecieversMap.get(e.getClass()).isEmpty())) {
			return null;
		}

		MicroService m = this.messageRecieversMap.get(e.getClass()).remove(0);
		this.messageRecieversMap.get(e.getClass()).add(m);

		if (this.eachServiceMap.containsKey(m)) {
			try {
				this.eachServiceMap.get(m).put(e);
			} catch (InterruptedException interruptedException) {
				Thread.currentThread().interrupt();
			}
		}

		Future<T> result = new Future<>();
		this.eventToFutureMap.put(e, result);
		return result;
	}

	/**
	 * Registers a {@link MicroService} to the MessageBus.
	 *
	 * @param m The {@link MicroService} to register.
	 * @pre m != null
	 * @post eachServiceMap.containsKey(m)
	 */
	@Override
	public void register(MicroService m) {
		this.eachServiceMap.putIfAbsent(m, new LinkedBlockingQueue<>());
	}

	/**
	 * Unregisters a {@link MicroService} from the MessageBus.
	 *
	 * @param m The {@link MicroService} to unregister.
	 * @pre m != null
	 * @post !eachServiceMap.containsKey(m)
	 */
	@Override
	public void unregister(MicroService m) {
		if (m == null) {
			System.out.println("Attempted to unregister a null MicroService. Ignoring...");
			return;
		}

		this.eachServiceMap.remove(m);
		for (List<MicroService> subscribers : messageRecieversMap.values()) {
			subscribers.remove(m);
		}
		eventToFutureMap.entrySet().removeIf(entry ->
				entry.getKey() instanceof Event && entry.getValue().equals(m));
	}

	/**
	 * Fetches the next {@link Message} from a {@link MicroService}'s queue.
	 *
	 * @param m The {@link MicroService} requesting the next Message.
	 * @return The next {@link Message} for the {@link MicroService}.
	 * @throws InterruptedException If interrupted while waiting for a Message.
	 * @pre m != null && eachServiceMap.containsKey(m)
	 */
	@Override
	public Message awaitMessage(MicroService m) throws InterruptedException {
		if (!this.eachServiceMap.containsKey(m)) {
			return null;
		}
		return this.eachServiceMap.get(m).take();
	}
}

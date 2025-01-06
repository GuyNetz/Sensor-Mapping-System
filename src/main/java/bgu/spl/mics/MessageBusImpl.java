package bgu.spl.mics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

/**
 * The {@link MessageBusImpl class is the implementation of the MessageBus interface.
 * Write your implementation here!
 * Only private fields and methods can be added to this class.
 */
public class MessageBusImpl implements MessageBus {

	//Fields
	private final Map<Class<? extends Message>, List<MicroService>> subscriptions;
	private final Map<MicroService, Queue<Message>> queues;
	private final Map<Event<?>, Future<?>> futures;
	private final Object lock = new Object();
	private static MessageBusImpl instance;

	//Constructor
	private MessageBusImpl() {
		subscriptions = new HashMap<>();
		queues = new HashMap<>();
		futures = new HashMap<>();
	}

	//Methods
	public static MessageBusImpl getInstance() {
		if (instance == null) {
			synchronized (MessageBusImpl.class) {
				if (instance == null) {
					instance = new MessageBusImpl();
				}
			}
		}
		return instance;
	}

	@Override
	public <T> void subscribeEvent(Class<? extends Event<T>> type, MicroService m) {
		synchronized (lock) {
			subscriptions.putIfAbsent(type, new ArrayList<>());
			if (!subscriptions.get(type).contains(m)) {
				subscriptions.get(type).add(m); 
			}
		}
	}

	@Override
	public void subscribeBroadcast(Class<? extends Broadcast> type, MicroService m) {
		synchronized (lock) {
			subscriptions.putIfAbsent(type, new ArrayList<>());
			if (!subscriptions.get(type).contains(m)) {
				subscriptions.get(type).add(m);
			}
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> void complete(Event<T> e, T result) {
		synchronized (lock) {
			// Gets the future of the event
			Future<T> future = (Future<T>) futures.get(e);

			// Resolve the future and removes it from the futures map
			if (future != null) {
				future.resolve(result);
				futures.remove(e);
			}
		}
	}

	@Override
	public void sendBroadcast(Broadcast b) {
		synchronized (lock) {
			List<MicroService> subscribers = subscriptions.get(b.getClass());

			if (subscribers != null && !subscribers.isEmpty()) {
				for (MicroService m : subscribers) {
					Queue<Message> queue = queues.get(m);
					if (queue != null) {
						queue.add(b); 
					}
				}
				lock.notifyAll();
			}
		}
	}

	
	@Override
	public <T> Future<T> sendEvent(Event<T> e) {
		synchronized (lock) {
			List<MicroService> subscribers = subscriptions.get(e.getClass());
			if (subscribers != null && !subscribers.isEmpty()){

				// Round Robin selection
				MicroService curMicroService = subscribers.remove(0); 
				subscribers.add(curMicroService); 

				// Get selected MicroService's queue
				Queue<Message> queue = queues.get(curMicroService); 

				if (queue != null){
					Future<T> future = new Future<>(); // Create a new future for the event
					futures.put(e, future);	// Adds future to futures map
					queue.add(e);	//Adds the event to the selected MicroService's queue
					lock.notifyAll();
					return future;
				}
			}
			return null; // If no micro-service has subscribed to the event's type
		}
	}
	

	@Override
	public void register(MicroService m) {
    	synchronized (lock) {
        	if (!queues.containsKey(m)) {
            	queues.put(m, new LinkedList<>()); // creates a new queue for the micro-service in the queues map
        	}
    	}
	}

	@Override
	public void unregister(MicroService m) {
		synchronized (lock) {
			queues.remove(m); // removing the micro-service from the queues map 
			
			for (List<MicroService> subscriberList : subscriptions.values()) { // removing the micro-service from the subscription map
				subscriberList.remove(m);
			} 

			// Removing all futures that relates to the Micro-Service
			futures.keySet().removeIf(event -> isEventRelatedToMicroService(event, m));
		}
	}

	@Override
	public Message awaitMessage(MicroService m) throws InterruptedException {
		synchronized (lock) {
			// Checks if the Micro-Service is registered, throws an exception if not
			Queue<Message> queue = queues.get(m);
			if (queue == null) {
				throw new IllegalStateException("MicroService is not registered");
			}

			// Waits until there's a message in the queue
			while (queue.isEmpty()){
				lock.wait(); 
			}

			//returns the first message in line
			return queue.poll();
		}
	}
	// Helper method to check if an event is related to a Micro-Service
	private boolean isEventRelatedToMicroService(Event<?> event, MicroService m) {
		return queues.containsKey(m) && futures.get(event) != null;
	}

	//Getters
	public Map<Class<? extends Message>, List<MicroService>> getSubscriptions() {
		return subscriptions;
	}

	public Map<MicroService, Queue<Message>> getQueues() {
		return queues;
	}

	public Map<Event<?>, Future<?>> getFutures() {
		return futures;
	}
}

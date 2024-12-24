package bgu.spl.mics;

import java.util.ArrayList;
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

	@Override
	public <T> void subscribeEvent(Class<? extends Event<T>> type, MicroService m) {
		synchronized (lock) {
			subscriptions.putIfAbsent(type, new ArrayList<>()); // יצירת רשימה אם לא קיימת
			if (!subscriptions.get(type).contains(m)) {
				subscriptions.get(type).add(m); // הוספת המיקרו-שירות לרשימה
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

	@Override
	public <T> void complete(Event<T> e, T result) {
		synchronized (lock) {
			Future<T> future = (Future<T>) futures.remove(e); // Remove the completed future from the futures map
			if (future != null) {
				future.resolve(result); // If the future is not null, resolve it with the given result
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
						queue.add(b); // מוסיפים את ה-Broadcast לתור של המיקרו-שירות
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
				MicroService curMicroService = subscribers.remove(0); // Round Robin selection
				subscribers.add(curMicroService); // Round Robin selection
				Queue<Message> queue = queues.get(curMicroService); // Get selected MicroService's queue

				if (queue != null){
					Future<T> future = new Future<>(); // Create a new future for the event
					futures.put(e, future);
					queue.add(e);
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

			// צריך להוסיף למימוש ניקוי של פיוצ'רים שמחכים למיקרו סרביס הזה, אבל זה דורש עוד התעסקות
			/*  Resolve any pending futures associated with the micro-service
			futures.entrySet().removeIf(entry -> {
				Event<?> event = entry.getKey();
				Future<?> future = entry.getValue();
				if (event.getMicroService() == m) {
					future.resolve(null);
					return true;
				}
				return false;
			}); */
		}
	}

	@Override
	public Message awaitMessage(MicroService m) throws InterruptedException {
		synchronized (lock) {
			Queue<Message> queue = queues.get(m);
			while (queue == null || queue.isEmpty()){
				lock.wait(); // Wait until a message is available in the queue
				queue = queues.get(m); // Get the updated queue
			}

			return queue.poll();
		}
	}
}

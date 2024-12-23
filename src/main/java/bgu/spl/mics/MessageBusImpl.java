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
	}

	@Override
	public <T> void complete(Event<T> e, T result) {
		// TODO Auto-generated method stub

	}

	@Override
	public void sendBroadcast(Broadcast b) {
		synchronized (lock) {
			List<MicroService> subscribers = subscriptions.get(b.getClass());
			if (subscribers != null) {
				for (MicroService m : subscribers) {
					Queue<Message> queue = queues.get(m);
					if (queue != null) {
						queue.add(b); // מוסיפים את ה-Broadcast לתור של המיקרו-שירות
					}
				}
			}
		}
	}

	
	@Override
	public <T> Future<T> sendEvent(Event<T> e) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void register(MicroService m) {
    synchronized (lock) {
        if (!queues.containsKey(m)) {
            queues.put(m, new LinkedList<>()); // תור חדש למיקרו-שירות
        }
    }
}

	@Override
	public void unregister(MicroService m) {
		synchronized (lock) {
			queues.remove(m); // הסרת תור ההודעות של המיקרו-שירות
			subscriptions.values().forEach(list -> list.remove(m)); // הסרת הרשמות
		}
	}

	@Override
	public Message awaitMessage(MicroService m) throws InterruptedException {
		// TODO Auto-generated method stub
		return null;
	}

	

}

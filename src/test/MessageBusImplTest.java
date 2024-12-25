package bgu.spl.mics;

import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class MessageBusImplTest {

    @Test
    void testRegister() {
        MessageBusImpl bus = MessageBusImpl.getInstance();
        MicroService service = new DummyMicroService("ServiceA");

        bus.register(service);
        assertTrue(bus.queues.containsKey(service), "Service should be registered.");
    }

    @Test
    void testUnregister() {
        MessageBusImpl bus = MessageBusImpl.getInstance();
        MicroService service = new DummyMicroService("ServiceB");

        bus.register(service);
        bus.unregister(service);
        assertFalse(bus.queues.containsKey(service), "Service should be unregistered.");
    }

    @Test
    void testSubscribeEvent() {
        MessageBusImpl bus = MessageBusImpl.getInstance();
        MicroService service = new DummyMicroService("ServiceC");
        Event<String> event = new Event<>() {};

        bus.register(service);
        bus.subscribeEvent(event.getClass(), service);

        assertTrue(bus.subscriptions.containsKey(event.getClass()), "Event type should exist in subscriptions.");
        assertTrue(bus.subscriptions.get(event.getClass()).contains(service), "Service should be subscribed to the event.");
    }

    @Test
    void testSubscribeBroadcast() {
        MessageBusImpl bus = MessageBusImpl.getInstance();
        MicroService service = new DummyMicroService("ServiceD");
        Broadcast broadcast = new Broadcast() {};

        bus.register(service);
        bus.subscribeBroadcast(broadcast.getClass(), service);

        assertTrue(bus.subscriptions.containsKey(broadcast.getClass()), "Broadcast type should exist in subscriptions.");
        assertTrue(bus.subscriptions.get(broadcast.getClass()).contains(service), "Service should be subscribed to the broadcast.");
    }

    @Test
    void testSendEvent() {
        MessageBusImpl bus = MessageBusImpl.getInstance();
        MicroService service = new DummyMicroService("ServiceE");
        Event<String> event = new Event<>() {};

        bus.register(service);
        bus.subscribeEvent(event.getClass(), service);

        Future<String> future = bus.sendEvent(event);

        assertNotNull(future, "Future should not be null.");
        assertFalse(future.isDone(), "Future should not be resolved yet.");
        assertEquals(event, bus.queues.get(service).poll(), "Event should be added to the service's queue.");
    }

    @Test
    void testSendBroadcast() {
        MessageBusImpl bus = MessageBusImpl.getInstance();
        MicroService service1 = new DummyMicroService("ServiceF");
        MicroService service2 = new DummyMicroService("ServiceG");
        Broadcast broadcast = new Broadcast() {};

        bus.register(service1);
        bus.register(service2);
        bus.subscribeBroadcast(broadcast.getClass(), service1);
        bus.subscribeBroadcast(broadcast.getClass(), service2);

        bus.sendBroadcast(broadcast);

        assertEquals(broadcast, bus.queues.get(service1).poll(), "Broadcast should be added to ServiceF's queue.");
        assertEquals(broadcast, bus.queues.get(service2).poll(), "Broadcast should be added to ServiceG's queue.");
    }

    @Test
    void testComplete() {
        MessageBusImpl bus = MessageBusImpl.getInstance();
        MicroService service = new DummyMicroService("ServiceH");
        Event<String> event = new Event<>() {};

        bus.register(service);
        bus.subscribeEvent(event.getClass(), service);

        Future<String> future = bus.sendEvent(event);
        bus.complete(event, "Result");

        assertTrue(future.isDone(), "Future should be resolved.");
        assertEquals("Result", future.get(), "Future result should match the expected value.");
    }

    @Test
    void testAwaitMessage() throws InterruptedException {
        MessageBusImpl bus = MessageBusImpl.getInstance();
        MicroService service = new DummyMicroService("ServiceI");
        Event<String> event = new Event<>() {};

        bus.register(service);
        bus.subscribeEvent(event.getClass(), service);
        bus.sendEvent(event);

        Message message = bus.awaitMessage(service);

        assertEquals(event, message, "Awaited message should match the sent event.");
    }

    @Test
    void testAwaitMessageThrowsException() {
        MessageBusImpl bus = MessageBusImpl.getInstance();
        MicroService service = new DummyMicroService("ServiceJ");

        assertThrows(IllegalStateException.class, () -> bus.awaitMessage(service), "Should throw IllegalStateException for unregistered service.");
    }
}

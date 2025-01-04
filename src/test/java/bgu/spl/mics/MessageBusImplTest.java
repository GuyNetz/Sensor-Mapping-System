package bgu.spl.mics;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import bgu.spl.mics.application.objects.Camera;
import bgu.spl.mics.application.objects.GPSIMU;
import bgu.spl.mics.application.services.CameraService;
import bgu.spl.mics.application.services.PoseService;
import bgu.spl.mics.application.objects.Pose;
import bgu.spl.mics.application.messages.PoseEvent;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.services.TimeService;



class MessageBusImplTest {

    /************** testing register **************/
    // @Test
    // void testRegister() {
    //     //objects
    //     MessageBusImpl bus = MessageBusImpl.getInstance();
    //     Camera camera = new Camera(1, 3);
    //     MicroService cameraService = new CameraService(camera);

    //     //register
    //     bus.register(cameraService);

    //     //check if the service is in the queue
    //     assertTrue(bus.getQueues().containsKey(cameraService));
    // }

    /************** testing unregister **************/
    // @Test
    // void testUnregister() {
    //     //objects
    //     MessageBusImpl bus = MessageBusImpl.getInstance();
    //     Camera camera = new Camera(1, 3);
    //     MicroService cameraService = new CameraService(camera);

    //     //register and unregister
    //     bus.register(cameraService);
    //     bus.unregister(cameraService);

    //     //check if the service is in the queue
    //     assertFalse(bus.getQueues().containsKey(cameraService));
    // }

    /************** testing subscribeEvent **************/
    @Test
    void testSubscribeEvent() {
        //objects
        MessageBusImpl bus = MessageBusImpl.getInstance();
        GPSIMU gpsimu = new GPSIMU();
        MicroService service = new PoseService(gpsimu);
        Pose pose = new Pose(1.2f, 2.5f, 3.5f, 4);
        PoseEvent event = new PoseEvent(pose);

        //register and subscribe
        bus.register(service);
        bus.subscribeEvent(event.getClass(), service);

        //check if the service is in the queue
        assertTrue(bus.getSubscriptions().containsKey(event.getClass()));
        assertTrue(bus.getSubscriptions().get(event.getClass()).contains(service));
    }
    /************** testing subscribeBroadcast **************/
    @Test
    void testSubscribeBroadcast() {
        //objects
        MessageBusImpl bus = MessageBusImpl.getInstance();
        MicroService timeService = new TimeService(1, 30);
        TickBroadcast tickBroadcast = new TickBroadcast(3);

        //register and subscribe
        bus.register(timeService);
        bus.subscribeBroadcast(tickBroadcast.getClass(), timeService);

        //check if the service is in the queue
        assertTrue(bus.getSubscriptions().containsKey(tickBroadcast.getClass()));
        assertTrue(bus.getSubscriptions().get(tickBroadcast.getClass()).contains(timeService)); 
    }

    /************** testing awaitMessage **************/
    //Ensures awaitMessage throws IllegalStateException for unregistered MicroService.
    @Test
    void testAwaitMessageUnregisteredMicroService() {
        MessageBusImpl bus = MessageBusImpl.getInstance();
        MicroService unregisteredService = new TimeService(1, 30);

        // Expect an exception
        assertThrows(IllegalStateException.class, () -> bus.awaitMessage(unregisteredService));
    }  

    //Verifies awaitMessage returns the correct message when the queue has messages.
    @Test
    void testAwaitMessageWithMessageInQueue() throws InterruptedException {
        MessageBusImpl bus = MessageBusImpl.getInstance();
        MicroService timeService = new TimeService(1, 30);
        Broadcast tickBroadcast = new TickBroadcast(1);

        // Register the service and add a message to its queue
        bus.register(timeService);
        bus.subscribeBroadcast(TickBroadcast.class, timeService);
        bus.sendBroadcast(tickBroadcast);

        // Await message and verify it is the expected one
        Message receivedMessage = bus.awaitMessage(timeService);
        assertEquals(tickBroadcast, receivedMessage);
    }

    //Checks that awaitMessage blocks until a message is added to the queue.
    @Test
    void testAwaitMessageBlocking() throws InterruptedException {
        MessageBusImpl bus = MessageBusImpl.getInstance();
        MicroService timeService = new TimeService(1, 30);
        Broadcast tickBroadcast = new TickBroadcast(1);

        // Register the service
        bus.register(timeService);
        bus.subscribeBroadcast(TickBroadcast.class, timeService);

        // Create a thread to send a message after a delay
        Thread senderThread = new Thread(() -> {
            try {
                Thread.sleep(500); // Simulate delay
                bus.sendBroadcast(tickBroadcast);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        senderThread.start();

        // Await message and verify it is the expected one
        Message receivedMessage = bus.awaitMessage(timeService);
        assertEquals(tickBroadcast, receivedMessage);

        // Ensure senderThread completes
        senderThread.join();
    }

    /************** testing awaitMessage **************/
    //Ensures sendEvent returns null when no MicroService is subscribed to the event type.
    @Test
    void testSendEventNoSubscribers() {
        MessageBusImpl bus = MessageBusImpl.getInstance();
        Event<Pose> event = new PoseEvent(new Pose(1.2f, 2.5f, 3.5f, 4));

        // No subscribers registered for the event
        Future<Pose> future = bus.sendEvent(event);

        // Verify that the method returns null
        assertNull(future);
    }

    /**
     * Ensures sendEvent adds the event to the correct MicroService queue
     * and returns a non-null Future when there are subscribers.
     */
    @Test
    void testSendEventWithSubscribers() throws InterruptedException {
        MessageBusImpl bus = MessageBusImpl.getInstance();
        MicroService subscriber = new TimeService(1, 30);
        Event<Pose> event = new PoseEvent(new Pose(1.2f, 2.5f, 3.5f, 4));

        // Register the subscriber and subscribe it to the event type
        bus.register(subscriber);
        bus.subscribeEvent(PoseEvent.class, subscriber);

        // Send the event
        Future<Pose> future = bus.sendEvent(event);

        // Verify that a Future is returned and the event is added to the subscriber's queue
        assertNotNull(future);
        Message receivedMessage = bus.awaitMessage(subscriber);
        assertEquals(event, receivedMessage);
    }

    
    //Ensures sendEvent distributes events in a round-robin fashion among subscribers.
    @Test
    void testSendEventRoundRobin() throws InterruptedException {
        MessageBusImpl bus = MessageBusImpl.getInstance();
        MicroService subscriber1 = new TimeService(1, 30);
        MicroService subscriber2 = new TimeService(2, 60); // Another TimeService for testing
        Event<Pose> event1 = new PoseEvent(new Pose(1.2f, 2.5f, 3.5f, 4));
        Event<Pose> event2 = new PoseEvent(new Pose(5.1f, 6.3f, 7.8f, 8));

        // Register both subscribers and subscribe them to the event type
        bus.register(subscriber1);
        bus.register(subscriber2);
        bus.subscribeEvent(PoseEvent.class, subscriber1);
        bus.subscribeEvent(PoseEvent.class, subscriber2);

        // Send the first event
        bus.sendEvent(event1);
        Message receivedBySubscriber1 = bus.awaitMessage(subscriber1);

        // Send the second event
        bus.sendEvent(event2);
        Message receivedBySubscriber2 = bus.awaitMessage(subscriber2);

        // Verify that events are distributed in round-robin order
        assertEquals(event1, receivedBySubscriber1, "Subscriber 1 should receive the first event.");
        assertEquals(event2, receivedBySubscriber2, "Subscriber 2 should receive the second event.");
    }

        /**
     * Ensures the Future returned by sendEvent can be resolved correctly
     * when the event processing is complete.
     */
    @Test
    void testSendEventFutureResolution() throws InterruptedException {
        MessageBusImpl bus = MessageBusImpl.getInstance();
        MicroService subscriber = new TimeService(1, 30);
        Event<Pose> event = new PoseEvent(new Pose(1.2f, 2.5f, 3.5f, 4));

        // Register the subscriber and subscribe it to the event type
        bus.register(subscriber);
        bus.subscribeEvent(PoseEvent.class, subscriber);

        // Send the event
        Future<Pose> future = bus.sendEvent(event);
        assertNotNull(future, "sendEvent should return a Future for a subscribed event.");

        // Process the event and resolve the Future
        new Thread(() -> {
            try {
                Message receivedMessage = bus.awaitMessage(subscriber);
                if (receivedMessage.equals(event)) {
                    bus.complete(event, new Pose(10.0f, 20.0f, 30.0f, 40));
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();

        // Verify the Future is resolved with the correct result
        Pose result = future.get(); // This will block until the Future is resolved
        assertEquals(10.0f, result.getX());
        assertEquals(20.0f, result.getY());
        assertEquals(30.0f, result.getYaw());
        assertEquals(40, result.getTime());
    }
}

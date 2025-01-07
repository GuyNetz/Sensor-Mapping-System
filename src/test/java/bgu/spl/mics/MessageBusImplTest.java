package bgu.spl.mics;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import bgu.spl.mics.application.objects.Pose;
import bgu.spl.mics.application.messages.PoseEvent;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.services.TimeService;



class MessageBusImplTest {

    // }
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

    /************** testing sendEvent **************/
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

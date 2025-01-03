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
    @Test
    void testRegister() {
        //objects
        MessageBusImpl bus = MessageBusImpl.getInstance();
        Camera camera = new Camera(1, 3);
        MicroService cameraService = new CameraService(camera);

        //register
        bus.register(cameraService);

        //check if the service is in the queue
        assertTrue(bus.getQueues().containsKey(cameraService));
    }

    /************** testing unregister **************/
    @Test
    void testUnregister() {
        //objects
        MessageBusImpl bus = MessageBusImpl.getInstance();
        Camera camera = new Camera(1, 3);
        MicroService cameraService = new CameraService(camera);

        //register and unregister
        bus.register(cameraService);
        bus.unregister(cameraService);

        //check if the service is in the queue
        assertFalse(bus.getQueues().containsKey(cameraService));
    }

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




//     @Test
//     void testSendEvent() {
//         MessageBusImpl bus = MessageBusImpl.getInstance();
//         MicroService service = new DummyMicroService("ServiceE");
//         Event<String> event = new Event<>() {};

//         bus.register(service);
//         bus.subscribeEvent(event.getClass(), service);

//         Future<String> future = bus.sendEvent(event);

//         assertNotNull(future, "Future should not be null.");
//         assertFalse(future.isDone(), "Future should not be resolved yet.");
//         assertEquals(event, bus.queues.get(service).poll(), "Event should be added to the service's queue.");
//     }

//     @Test
//     void testSendBroadcast() {
//         MessageBusImpl bus = MessageBusImpl.getInstance();
//         MicroService service1 = new DummyMicroService("ServiceF");
//         MicroService service2 = new DummyMicroService("ServiceG");
//         Broadcast broadcast = new Broadcast() {};

//         bus.register(service1);
//         bus.register(service2);
//         bus.subscribeBroadcast(broadcast.getClass(), service1);
//         bus.subscribeBroadcast(broadcast.getClass(), service2);

//         bus.sendBroadcast(broadcast);

//         assertEquals(broadcast, bus.queues.get(service1).poll(), "Broadcast should be added to ServiceF's queue.");
//         assertEquals(broadcast, bus.queues.get(service2).poll(), "Broadcast should be added to ServiceG's queue.");
//     }

//     @Test
//     void testComplete() {
//         MessageBusImpl bus = MessageBusImpl.getInstance();
//         MicroService service = new DummyMicroService("ServiceH");
//         Event<String> event = new Event<>() {};

//         bus.register(service);
//         bus.subscribeEvent(event.getClass(), service);

//         Future<String> future = bus.sendEvent(event);
//         bus.complete(event, "Result");

//         assertTrue(future.isDone(), "Future should be resolved.");
//         assertEquals("Result", future.get(), "Future result should match the expected value.");
//     }


}

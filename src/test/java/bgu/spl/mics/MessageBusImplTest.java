package bgu.spl.mics;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;

import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the {@link MessageBusImpl} class.
 */
class MessageBusImplTest {
    private MessageBusImpl messageBus;
    private MicroService microService1;
    private MicroService microService2;

    @BeforeEach
    void setUp() {
        messageBus = MessageBusImpl.getInstance();
        microService1 = new MicroService("Service1") {
            @Override
            protected void initialize() {}
        };
        microService2 = new MicroService("Service2") {
            @Override
            protected void initialize() {}
        };
        messageBus.register(microService1);
        messageBus.register(microService2);
    }

    @AfterEach
    void tearDown() {
        messageBus.unregister(microService1);
        messageBus.unregister(microService2);
    }

    /**
     * Test method for {@link MessageBusImpl#subscribeEvent(Class, MicroService)}.
     * Verifies that subscribing to an event allows the MicroService to receive it.
     */
    @Test
    void testEvent() {
        class TestEvent implements Event<String> {}

        messageBus.subscribeEvent(TestEvent.class, microService1);
        Future<String> future = messageBus.sendEvent(new TestEvent());
        assertNotNull(future, "Future should not be null if MicroService is subscribed");

        assertDoesNotThrow(() -> {
            Message message = messageBus.awaitMessage(microService1);
            assertTrue(message instanceof TestEvent, "The received message should be of type TestEvent");
        });
    }

    /**
     * Test method for {@link MessageBusImpl#sendBroadcast(Broadcast)}.
     * Verifies that all subscribed MicroServices receive a broadcast message.
     */
    @Test
    void testBroadcast() {
        class TestBroadcast implements Broadcast {}

        messageBus.subscribeBroadcast(TestBroadcast.class, microService1);
        messageBus.subscribeBroadcast(TestBroadcast.class, microService2);

        messageBus.sendBroadcast(new TestBroadcast());

        assertDoesNotThrow(() -> {
            Message message1 = messageBus.awaitMessage(microService1);
            assertTrue(message1 instanceof TestBroadcast, "microService1 should receive the broadcast");

            Message message2 = messageBus.awaitMessage(microService2);
            assertTrue(message2 instanceof TestBroadcast, "microService2 should receive the broadcast");
        });
    }

    /**
     * Test method for {@link MessageBusImpl#awaitMessage(MicroService)}.
     * Verifies that a MicroService can successfully retrieve an event message.
     */
    @Test
    void testAwaitMessage() throws InterruptedException {
        class TestEvent implements Event<String> {}

        messageBus.subscribeEvent(TestEvent.class, microService1);
        messageBus.sendEvent(new TestEvent());

        Message message = messageBus.awaitMessage(microService1);
        assertNotNull(message, "Message should not be null when retrieved");
        assertTrue(message instanceof TestEvent, "The received message should match the sent event type");
    }

}

package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.CrashedBroadcast;
import bgu.spl.mics.application.messages.TerminatedBroadcast;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.objects.STATUS;
import bgu.spl.mics.application.objects.StatisticalFolder;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * TimeService acts as the global timer for the system, broadcasting {@link TickBroadcast} messages
 * at regular intervals and controlling the simulation's overall duration.
 * <p>
 * It manages sensor termination, detects crashes via {@link CrashedBroadcast}, and updates statistics
 * in the {@link StatisticalFolder}.
 */
public class TimeService extends MicroService {
    private final int tickTime;
    private final int duration;
    private int currentTick;
    private final StatisticalFolder folder;
    private final AtomicInteger sensorsCounter;
    private STATUS whichFinish;
    private String errorMaker;
    private String errorMessage;

    /**
     * Constructs a new TimeService instance.
     *
     * @param tickTime       The duration of each tick in milliseconds.
     * @param duration       The total number of ticks before the service terminates.
     * @param folder         A {@link StatisticalFolder} object for storing statistical data.
     * @param sensorsCounter An {@link AtomicInteger} representing the counter for active sensors.
     * @pre tickTime > 0 && duration > 0 && folder != null && sensorsCounter != null
     * @post this.tickTime == tickTime && this.duration == duration && this.folder == folder &&
     *       this.sensorsCounter == sensorsCounter
     */
    public TimeService(int tickTime, int duration, StatisticalFolder folder, AtomicInteger sensorsCounter) {
        super("TimeService");
        this.tickTime = tickTime;
        this.duration = duration;
        this.currentTick = 0;
        this.folder = folder;
        this.sensorsCounter = sensorsCounter;
        this.whichFinish = STATUS.DOWN;
        this.errorMaker = "";
        this.errorMessage = "";
    }

    /**
     * Initializes the TimeService.
     * <ul>
     *   <li>Subscribes to {@link TerminatedBroadcast} for graceful shutdown.</li>
     *   <li>Subscribes to {@link CrashedBroadcast} for error handling.</li>
     *   <li>Broadcasts {@link TickBroadcast} messages at regular intervals.</li>
     *   <li>Terminates gracefully or due to errors.</li>
     * </ul>
     *
     * @pre tickTime > 0 && duration > 0 && sensorsCounter.get() >= 0
     * @post The service broadcasts TickBroadcast messages and handles termination appropriately.
     */
    @Override
    protected void initialize() {
        System.out.println(getName() + " initialized. TickTime: " + tickTime + "ms, Duration: " + duration + " ticks");

        // Handle Termination Broadcast
        subscribeBroadcast(TerminatedBroadcast.class, broadcast -> {
            if (broadcast.getSender().equals("TimeService")) {
                System.out.println(getName() + " Shutting down.");
                terminate();
            }
            if (broadcast.getSender().startsWith("Camera") ||
                    broadcast.getSender().startsWith("Lidar") ||
                    broadcast.getSender().startsWith("Pose")) {
                System.out.println(getName() + " received TerminatedBroadcast from a sensor.");
                this.sensorsCounter.decrementAndGet();
                System.out.println(this.sensorsCounter.get());
            }
        });

        // Handle Crash Broadcast
        subscribeBroadcast(CrashedBroadcast.class, broadcast -> {
            if (broadcast.getSender().equals("TimeService")) {
                System.out.println(getName() + " Shutting down due to error.");
                terminate();
            } else {
                System.out.println("Received CrashedBroadcast from " + broadcast.getSender() +
                        " in sensor " + broadcast.getErrorMaker());
                this.whichFinish = STATUS.ERROR;
                this.errorMaker = broadcast.getErrorMaker();
                this.errorMessage = broadcast.getMessage();
            }
        });

        // Main Timer Logic
        new Thread(() -> {
            try {
                while ((currentTick < duration) && !Thread.currentThread().isInterrupted()) {
                    // Early termination if no active sensors or error detected
                    if ((this.sensorsCounter.get() == new AtomicInteger(0).get()) ||
                            this.whichFinish.equals(STATUS.ERROR)) {
                        break;
                    }

                    // Increment tick counter
                    currentTick++;
                    System.out.println("Tick " + currentTick + " broadcasted");

                    // Broadcast TickBroadcast message
                    sendBroadcast(new TickBroadcast(currentTick));

                    // Update statistics folder
                    this.folder.addTick();

                    // Pause for the tick duration
                    Thread.sleep(tickTime * 1000L); // Convert to seconds
                }

            } catch (InterruptedException e) {
                System.out.println(getName() + " interrupted during sleep. Shutting down...");
                Thread.currentThread().interrupt(); // Restore the interrupt status
            } finally {
                if (this.whichFinish == STATUS.DOWN) {
                    // Regular finish
                    System.out.println(getName() + " terminating. Broadcasting termination signal...");
                    sendBroadcast(new TerminatedBroadcast(this.getName()));
                } else {
                    // Error finish
                    System.out.println(getName() + " terminating due to error. Broadcasting crash signal...");
                    sendBroadcast(new CrashedBroadcast(this.getName(), this.errorMaker, this.errorMessage));
                }
            }
        }).start();
    }
}

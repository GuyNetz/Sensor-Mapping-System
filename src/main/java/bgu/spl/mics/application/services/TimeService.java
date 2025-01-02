package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.TerminatedBroadcast;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.objects.StatisticalFolder;

/**
 * TimeService acts as the global timer for the system, broadcasting TickBroadcast messages
 * at regular intervals and controlling the simulation's duration.
 */
public class TimeService extends MicroService {
    private int TickTime;
    private int Duration;

    /**
     * Constructor for TimeService.
     *
     * @param TickTime  The duration of each tick in milliseconds.
     * @param Duration  The total number of ticks before the service terminates.
     */
    public TimeService(int TickTime, int Duration) {
        super("TimeService");
        this.TickTime = TickTime;
        this.Duration = Duration;
    }

    /**
     * Initializes the TimeService.
     * Starts broadcasting TickBroadcast messages and terminates after the specified duration.
     */
    @Override
    protected void initialize() {
        Thread timerThread = new Thread(() -> {
            for (int currentTick = 1; currentTick <= Duration; currentTick++) {
                try {
                    // Update the systemRuntime in the StatisticalFolder
                    StatisticalFolder stats = StatisticalFolder.getInstance();
                    stats.incrementSystemRuntime();

                    // Broadcast the current tick
                    sendBroadcast(new TickBroadcast(currentTick));

                    // Sleep for the duration of the tick
                    Thread.sleep(TickTime);

                } catch (InterruptedException e) {
                    // Graceful shutdown if interrupted
                    Thread.currentThread().interrupt();
                    break;
                }
            }

            // After the duration ends, broadcast TerminatedBroadcast
            sendBroadcast(new TerminatedBroadcast());

            // Terminate the service
            terminate();
        });

        // Start the timer thread
        timerThread.start();
    }
}
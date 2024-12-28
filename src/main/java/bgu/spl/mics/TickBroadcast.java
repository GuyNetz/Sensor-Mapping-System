package bgu.spl.mics;

public class TickBroadcast implements Broadcast{
    // Fields
    private final int currentTick;

    // Constructor
    public TickBroadcast(int currentTick) {
        this.currentTick = currentTick;
    }

    // Methods
    public int getCurrentTick() {
        return currentTick;
    }
}

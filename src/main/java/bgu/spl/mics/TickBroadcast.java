package bgu.spl.mics;

public class TickBroadcast implements Broadcast{
    private final int tick;
    private final boolean isFinalTick;

    // Constructor
    public TickBroadcast(int tick, boolean isFinalTick){
        this.tick = tick;
        this.isFinalTick = isFinalTick;
    }

    // Methods
    public int getTick(){
        return tick;
    }

    public boolean isFinalTick(){
        return isFinalTick;
    }
}

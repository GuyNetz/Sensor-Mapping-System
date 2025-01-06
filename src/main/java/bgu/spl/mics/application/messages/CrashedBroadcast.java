package bgu.spl.mics.application.messages;

import bgu.spl.mics.Broadcast;

public class CrashedBroadcast implements Broadcast{
    private String faultySensor; // Faulty sensor ID or name
   
    public CrashedBroadcast(String faultySensor) {
        this.faultySensor = faultySensor;
    }

    public String getFaultySensor(){ // Getter
        return faultySensor;
    }
}
    


package com.argentum.utils.time;

public class TickTimer {
    private int passed = 0;
    private int required = 0;

    public void handleTick(){
        passed += 1;
    }
    public boolean isTimePassed(){
        return passed >= required;
    }
    public void wait(int ticks){
        passed = 0;
        required = ticks;
    }
    public void reset(){
        passed = 0;
    }
}

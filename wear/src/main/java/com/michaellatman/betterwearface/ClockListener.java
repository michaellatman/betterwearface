package com.michaellatman.betterwearface;


import java.util.Calendar;

/**
 * An interface to listen to the clock ticking.
 */
public interface ClockListener {
    void onTimeChanged(Calendar calendar);
}
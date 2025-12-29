package de.dasoftware.cryptpad.model;

/**
 * Observer interface for model change notifications
 */
public interface IObserver {
    
    /**
     * Called when the observed model changes
     */
    void refresh();
}
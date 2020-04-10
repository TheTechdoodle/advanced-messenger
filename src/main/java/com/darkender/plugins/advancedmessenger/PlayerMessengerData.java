package com.darkender.plugins.advancedmessenger;

import java.util.UUID;

import static com.darkender.plugins.advancedmessenger.AdvancedMessenger.OFFLINE_CLEANUP_TIMEOUT;
import static com.darkender.plugins.advancedmessenger.AdvancedMessenger.TYPING_TIMEOUT;

public class PlayerMessengerData
{
    private long loggedOff = 0;
    private boolean online = true;
    private long typingTime = 0;
    private String typingMessage = null;
    private UUID typingTarget = null;
    private UUID lastMessaged = null;
    private boolean displayingTypingNotification = false;
    
    private boolean wasTyping;
    
    /**
     * Checks if the PlayerMessengerData has expired from a player being offline for too long
     * Currently 5 minutes
     * @return true if expired, false otherwise
     */
    public boolean expired()
    {
        if(online)
        {
            return false;
        }
        return ((System.nanoTime() - loggedOff) > OFFLINE_CLEANUP_TIMEOUT);
    }
    
    /**
     * Updates the online status and loggedOff time
     */
    public void quit()
    {
        online = false;
        loggedOff = System.nanoTime();
    }
    
    /**
     * Updates the typing timings and target
     * @param message The message "draft" in progress
     * @param target The UUID of the target of the message
     */
    public void updateTypingMessage(String message, UUID target)
    {
        typingMessage = message;
        typingTime = System.nanoTime();
        typingTarget = target;
    }
    
    public void resetTypingTime()
    {
        typingTime = 0;
    }
    
    /**
     * Checks if the last typingTime is less than 3 seconds old and the player is online
     * @return true if the last typing update is less than 3 seconds old and the player is online, false otherwise
     */
    public boolean isTyping()
    {
        if(((System.nanoTime() - typingTime) < TYPING_TIMEOUT) && online)
        {
            wasTyping = true;
            return true;
        }
        return false;
    }
    
    public long getLoggedOff()
    {
        return loggedOff;
    }
    
    public boolean isOnline()
    {
        return online;
    }
    
    public void setOnline(boolean online)
    {
        this.online = online;
    }
    
    public long getTypingTime()
    {
        return typingTime;
    }
    
    public String getTypingMessage()
    {
        return typingMessage;
    }
    
    public UUID getLastMessaged()
    {
        return lastMessaged;
    }
    
    public void setLastMessaged(UUID lastMessaged)
    {
        this.lastMessaged = lastMessaged;
    }
    
    public UUID getTypingTarget()
    {
        return typingTarget;
    }
    
    public boolean isDisplayingTypingNotification()
    {
        return displayingTypingNotification;
    }
    
    public void setDisplayingTypingNotification(boolean displayingTypingNotification)
    {
        this.displayingTypingNotification = displayingTypingNotification;
    }
    
    public boolean wasTyping()
    {
        return wasTyping;
    }
    
    public void setWasTyping(boolean wasTyping)
    {
        this.wasTyping = wasTyping;
    }
}

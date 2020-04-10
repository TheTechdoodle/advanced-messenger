package com.darkender.plugins.advancedmessenger;

import com.darkender.plugins.advancedmessenger.commands.MessageCommand;
import com.darkender.plugins.advancedmessenger.commands.ReplyCommand;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AdvancedMessenger extends JavaPlugin implements Listener
{
    public final static long TYPING_TIMEOUT = 3 * 1000 * 1000000L;
    public final static long OFFLINE_CLEANUP_TIMEOUT = 5 * 60 * 1000 * 1000000L;
    
    private HashMap<UUID, PlayerMessengerData> messengerData;
    
    private long lastTyped = 0;
    private int typingNotificationCount = 0;
    private int ellipsisFrame = 0;
    private String[] loadingPrefix = new String[]{
            "      ",
            ChatColor.BOLD + "  " + ChatColor.RESET + "    ",
            ChatColor.BOLD + "    " + ChatColor.RESET + "  ",
            ChatColor.BOLD + "      " + ChatColor.RESET
    };
    private String[] loadingSuffix = new String[]{
            "      ",
            ".      ",
            "..      ",
            "...      ",
    };
    
    @Override
    public void onEnable()
    {
        // Add players who are on the server when the plugin is enabled
        messengerData = new HashMap<>();
        for(Player p : getServer().getOnlinePlayers())
        {
            messengerData.put(p.getUniqueId(), new PlayerMessengerData());
        }
        
        getServer().getPluginManager().registerEvents(this, this);
        
        // Every 5 seconds, remove data of players that have been offline for too long
        getServer().getScheduler().runTaskTimer(this, new Runnable()
        {
            @Override
            public void run()
            {
                messengerData.entrySet().removeIf(entry -> entry.getValue().expired());
            }
        }, 20, 20 * 5);
    
        // Update the typing notifications (4 times per second)
        getServer().getScheduler().runTaskTimer(this, new Runnable()
        {
            @Override
            public void run()
            {
                // If we don't have to do any work... don't
                if(typingNotificationCount == 0 && (System.nanoTime() - lastTyped) > TYPING_TIMEOUT)
                {
                    return;
                }
                
                ellipsisFrame++;
                if(ellipsisFrame == loadingPrefix.length)
                {
                    ellipsisFrame = 0;
                }
                
                for(Map.Entry<UUID, PlayerMessengerData> entry : messengerData.entrySet())
                {
                    if(entry.getValue().isTyping())
                    {
                        // Only show typing notifications if it's an already established chat
                        if(messengerData.containsKey(entry.getValue().getTypingTarget()) &&
                                messengerData.get(entry.getValue().getTypingTarget()).getLastMessaged() == entry.getKey())
                        {
                            Player receiver = getServer().getPlayer(entry.getValue().getTypingTarget());
                            Player sender = getServer().getPlayer(entry.getKey());
                            if(sender != null && receiver != null)
                            {
                                receiver.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                                        new ComponentBuilder(
                                                loadingPrefix[ellipsisFrame] +
                                                ChatColor.GOLD + sender.getDisplayName() + ChatColor.GOLD + " is typing" +
                                                loadingSuffix[ellipsisFrame]).create());
        
                                PlayerMessengerData receiverData = messengerData.get(entry.getValue().getTypingTarget());
                                if(!receiverData.isDisplayingTypingNotification())
                                {
                                    receiverData.setDisplayingTypingNotification(true);
                                    typingNotificationCount++;
                                }
                            }
                        }
                    }
                    else if(entry.getValue().wasTyping())
                    {
                        entry.getValue().setWasTyping(false);
    
                        if(messengerData.containsKey(entry.getValue().getTypingTarget()) &&
                                messengerData.get(entry.getValue().getTypingTarget()).getLastMessaged() == entry.getKey())
                        {
                            Player receiver = getServer().getPlayer(entry.getValue().getTypingTarget());
                            if(receiver != null)
                            {
                                receiver.spigot().sendMessage(ChatMessageType.ACTION_BAR, new ComponentBuilder("").create());
            
                                PlayerMessengerData receiverData = messengerData.get(entry.getValue().getTypingTarget());
                                if(receiverData.isDisplayingTypingNotification())
                                {
                                    receiverData.setDisplayingTypingNotification(false);
                                    typingNotificationCount--;
                                }
                            }
                        }
                    }
                }
            }
        }, 20, 5);
    
        MessageCommand messageCommand = new MessageCommand(this);
        ReplyCommand replyCommand = new ReplyCommand(this);
        getCommand("message").setExecutor(messageCommand);
        getCommand("message").setTabCompleter(messageCommand);
        getCommand("reply").setExecutor(replyCommand);
        getCommand("reply").setTabCompleter(replyCommand);
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event)
    {
        if(messengerData.containsKey(event.getPlayer().getUniqueId()))
        {
            messengerData.get(event.getPlayer().getUniqueId()).setOnline(true);
        }
        else
        {
            messengerData.put(event.getPlayer().getUniqueId(), new PlayerMessengerData());
        }
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event)
    {
        messengerData.get(event.getPlayer().getUniqueId()).quit();
    }
    
    public void sendMessage(Player from, String to, String message)
    {
        PlayerMessengerData fromData = messengerData.get(from.getUniqueId());
        fromData.resetTypingTime();
    
        Player toPlayer = null;
        if(to.isEmpty())
        {
            if(fromData.getLastMessaged() != null)
            {
                toPlayer = getServer().getPlayer(fromData.getLastMessaged());
            }
            else
            {
                from.sendMessage(ChatColor.RED + "There is nobody to reply to");
                return;
            }
        }
        else
        {
            toPlayer = playerSearch(to);
        }

        if(toPlayer == null)
        {
            from.sendMessage(ChatColor.RED + "That player is currently offline");
        }
        else
        {
            toPlayer.spigot().sendMessage(new ComponentBuilder()
                    .append(ChatColor.GOLD + "[" + from.getDisplayName() + ChatColor.GOLD  + " \u2192 You] ")
                    .event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/r "))
                    .append(ChatColor.WHITE + message).create());
            from.spigot().sendMessage(new ComponentBuilder()
                    .append(ChatColor.GOLD + "[You \u2192 " + toPlayer.getDisplayName() + ChatColor.GOLD + "] ")
                    .event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/r "))
                    .append(ChatColor.WHITE + message).create());
            toPlayer.playSound(toPlayer.getLocation(), Sound.BLOCK_NOTE_BLOCK_BIT, 1.0f, 1.5f);
    
            if(messengerData.containsKey(toPlayer.getUniqueId()))
            {
                messengerData.get(toPlayer.getUniqueId()).setLastMessaged(from.getUniqueId());
            }
            fromData.setLastMessaged(toPlayer.getUniqueId());
        }
    }
    
    public Player playerSearch(String name)
    {
        for(Player p : getServer().getOnlinePlayers())
        {
            if(p.getName().toLowerCase().startsWith(name.toLowerCase()))
            {
                return p;
            }
        }
        return null;
    }
    
    public void typingMessage(Player from, String to, String message)
    {
        lastTyped = System.nanoTime();
        PlayerMessengerData data = messengerData.get(from.getUniqueId());
        
        UUID toID = null;
        if(to.isEmpty())
        {
            toID = data.getLastMessaged();
        }
        else
        {
            Player p = playerSearch(to);
            if(p != null)
            {
                toID = p.getUniqueId();
            }
        }
        
        if(toID != null)
        {
            data.updateTypingMessage(message, toID);
        }
    }
}

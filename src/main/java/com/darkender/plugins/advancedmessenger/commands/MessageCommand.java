package com.darkender.plugins.advancedmessenger.commands;

import com.darkender.plugins.advancedmessenger.AdvancedMessenger;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class MessageCommand implements CommandExecutor, TabCompleter
{
    private AdvancedMessenger advancedMessenger;
    
    public MessageCommand(AdvancedMessenger advancedMessenger)
    {
        this.advancedMessenger = advancedMessenger;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
    {
        if(!(sender instanceof Player))
        {
            sender.sendMessage(ChatColor.RED + "You must be a player to message");
            return true;
        }
        
        if(args.length == 1)
        {
            sender.sendMessage(ChatColor.RED + "There's no message to send!");
            return true;
        }
        
        StringBuilder message = new StringBuilder(args[1]);
        for(int i = 2; i < args.length; i++)
        {
            message.append(" ").append(args[i]);
        }
        
        advancedMessenger.sendMessage((Player) sender, args[0], message.toString());
        return true;
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args)
    {
        if(args.length > 1 && sender instanceof Player)
        {
            StringBuilder message = new StringBuilder(args[1]);
            for(int i = 2; i < args.length; i++)
            {
                message.append(" ").append(args[i]);
            }
            
            advancedMessenger.typingMessage((Player) sender, args[0], message.toString());
            return new ArrayList<>();
        }
        return null;
    }
}

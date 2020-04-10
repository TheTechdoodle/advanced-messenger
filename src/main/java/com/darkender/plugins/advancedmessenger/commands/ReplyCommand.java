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

public class ReplyCommand implements CommandExecutor, TabCompleter
{
    private AdvancedMessenger advancedMessenger;
    
    public ReplyCommand(AdvancedMessenger advancedMessenger)
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
        
        StringBuilder message = new StringBuilder(args[0]);
        for(int i = 1; i < args.length; i++)
        {
            message.append(" ").append(args[i]);
        }
        
        advancedMessenger.sendMessage((Player) sender, "", message.toString());
        return true;
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args)
    {
        if(args.length > 0 && sender instanceof Player)
        {
            StringBuilder message = new StringBuilder(args[0]);
            for(int i = 1; i < args.length; i++)
            {
                message.append(" ").append(args[i]);
            }
            
            advancedMessenger.typingMessage((Player) sender, "", message.toString());
            return new ArrayList<>();
        }
        return null;
    }
}

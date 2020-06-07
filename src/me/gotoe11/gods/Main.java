package me.gotoe11.gods;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * 
 * @author Ethan Neece
 * @version 6/7/2020
 */
public class Main extends JavaPlugin implements Listener {
    
    //contains player name and what god they are pledged to. 
    private Map<String, Gods> pledges; 
    
    //cost to unpledge from a god. 
    private static final int REMOVE_GOD_COST = 5; 
    
    //Used for saving and writing to the save files this program uses
    private static final String DIRECTORY_LOCATION = "plugins/GodsPlugin";
    private static final String SAVE_FILE_NAME = "save.txt"; 
    
    //Used to differentiate between the 6 gods. 
    private enum Gods {
        FORGE, PATHFINDER, NATURE, BLADE, LANTERN, COIN;
        
        public static Gods getGod(String god)
        {
            Gods godType = null; 
            if (god.equalsIgnoreCase("Forge"))
            {
                godType = FORGE; 
            }
            
            else if (god.equalsIgnoreCase("Nature"))
            {
                godType = NATURE; 
            }
            
            else if (god.equalsIgnoreCase("Pathfinder"))
            {
                godType = PATHFINDER; 
            }
            
            else if (god.equalsIgnoreCase("Blade"))
            {
                godType = BLADE; 
            }
            
            else if (god.equalsIgnoreCase("Coin"))
            {
                godType = COIN; 
            }
            else if (god.equalsIgnoreCase("Lantern"))
            {
                godType = LANTERN; 
            }
            
            return godType; 
        }
        
        public static String getGod(Gods godType)
        {
            String god = null; 
            
            switch (godType) {
                case FORGE:
                    god = "Forge";
                    break;
                case NATURE:
                    god = "Nature";
                    break; 
                case PATHFINDER:
                    god = "Pathfinder";
                    break; 
                case BLADE:
                    god = "Blade";
                    break; 
                case LANTERN:
                    god = "Lantern";
                    break; 
                case COIN:
                    god = "Coin";
                    break; 
            }
        
            
            return god; 
        }
    }
    
    /**
     * reads from file save.txt 
     *  unless file is not found then creates one and 
     *  acts as if it is first run. 
     */
    @Override 
    public void onEnable()
    {
        pledges = new HashMap<String, Gods>(); 
        
        File directory = new File(DIRECTORY_LOCATION);
        directory.mkdir(); 
        
        File saveData = new File(directory, SAVE_FILE_NAME);
        try {
            if (!saveData.createNewFile())
            {
                Scanner fileReader = new Scanner(saveData);
                while(fileReader.hasNextLine())
                {
                    String personGod = fileReader.nextLine(); 
                    
                    int delimeter = personGod.indexOf(":");
                    String playerName = personGod.substring(0, delimeter);
                    String godString = personGod.substring(delimeter + 1);
                    
                    Gods god = Gods.getGod(godString);
                    
                    if (god == null)
                    {
                        this.getLogger().warning("Could not read " + playerName + "'s God.");
                    }
                    else
                    {
                        pledges.put(playerName, god);
                    }
                    
                }
                
                fileReader.close(); 
            }
        }
        catch (IOException e) {
            this.getLogger().warning("Something went wrong loading the file");
        }
        
        //adds the eventHandlers 
        Bukkit.getPluginManager().registerEvents(this, this);
    }
    
    /**
     * writes to saveFile all the people that are currently pledged. 
     */
    @Override
    public void onDisable()
    {
        File saveData = new File(DIRECTORY_LOCATION + File.separatorChar + SAVE_FILE_NAME);
        
        try {
            BufferedWriter fileWriter = new BufferedWriter(new FileWriter(saveData));
            
            for (Entry<String, Gods> entry : pledges.entrySet())
            {
                String playerName = entry.getKey(); 
                String god = Gods.getGod(entry.getValue());
                fileWriter.write(playerName +":" + god);
            }
            
            fileWriter.close(); 
        }
        catch (IOException e) {
            this.getLogger().warning("did not write to the file properly");
        }
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
    {
        if (!(sender instanceof Player))
        {
            sender.sendMessage(ChatColor.RED + "Only players can have these effects!");
            return false; 
        }
        
        Player player = (Player) sender; 
        
        if (cmd.getName().equalsIgnoreCase("unpledge"))
        {
            if (!isPledged(player))
            {
                player.sendMessage("You are currently not pledged to any gods!");
                return true; 
            }
            
            ItemStack playerItem = player.getInventory().getItemInMainHand();
                
            if (playerItem.getType() != Material.GOLD_INGOT)
            {
                player.sendMessage("You must sacrifice 5 gold to change gods!");
                player.sendMessage("Hold the gold in your main hand to sacrifice.");
                return true; 
            }
            
            if (playerItem.getAmount() < REMOVE_GOD_COST)
            {
                int missingCost = REMOVE_GOD_COST - playerItem.getAmount(); 
                player.sendMessage("You need " + missingCost + " more ingots" );
                return true; 
            }
            
            playerItem.setAmount(playerItem.getAmount() - REMOVE_GOD_COST);
            player.removePotionEffect(getEffect(player).getType());
            pledges.remove(player.getName());
            player.sendMessage("Your God has been removed.");

            return true; 
        }
        
        if (isPledged(player))
        {
            player.sendMessage("You already have God: " + Gods.getGod(pledges.get(player.getName())));
            return false; 
        }
        
        if (cmd.getName().equalsIgnoreCase("pledge"))
        {
            String god = args[0];
            Gods godType; 
            String aspect = ""; 
            
            if (god.equalsIgnoreCase("Forge"))
            {
                godType = Gods.FORGE; 
                aspect = "Forge"; 
            }
            
            else if (god.equalsIgnoreCase("Lantern"))
            {
                godType = Gods.LANTERN;  
                aspect = "Lantern";
            }
            
            else if (god.equalsIgnoreCase("Pathfinder"))
            {
                godType = Gods.PATHFINDER; 
                aspect = "Pathfinder";  
            }
            
            else if (god.equalsIgnoreCase("Nature"))
            {
                godType = Gods.NATURE; 
                aspect = "Nature";
            }
            
            else if (god.equalsIgnoreCase("Blade"))
            {
                godType = Gods.BLADE; 
                aspect = "Blade";
            }
            
            else if (god.equalsIgnoreCase("Coin"))
            {
                godType = Gods.COIN; 
                aspect = "Coin"; 
            }
            else
            {
                player.sendMessage(ChatColor.RED + "You did not choose one of the gods you can pledge to.");
                return false; 
            }
            
            pledges.put(player.getName(), godType);
            giveBuff(player);
            player.sendMessage(ChatColor.GREEN + "You have pledged yourself to the " + aspect + "!");
            return false; 
        }
        
        return false;
    }
    
    /**
     * apples the god buffs again after someone respawns. 
     * @param e event. 
     */
    @EventHandler
    public void onPlayerSpawn(PlayerRespawnEvent e)
    {
        giveBuff(e.getPlayer());
    }
    
    /**
     * applies the god buffs again after someone drinks milk. 
     * @param e event. 
     */
    @EventHandler
    public void onDrinkMilk(PlayerItemConsumeEvent e)
    {
        if (e.getItem().getType().equals(Material.MILK_BUCKET))
        {
            giveBuff(e.getPlayer());
        }
    }
    
    /**
     * Finds the PotionEffect For the player. 
     * @param p is the player. 
     * @return the PotionEffect if they player has a God. 
     *  and null if the player does not have a god. 
     */
    private PotionEffect getEffect(Player p)
    {
        switch (pledges.get(p.getName())) {
            case FORGE:
                return new PotionEffect(PotionEffectType.FAST_DIGGING, Integer.MAX_VALUE, 0);
            case NATURE:
                return new PotionEffect(PotionEffectType.REGENERATION, Integer.MAX_VALUE, 0);
            case PATHFINDER: 
                return new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 0);
            case BLADE:
                return new PotionEffect(PotionEffectType.INCREASE_DAMAGE, Integer.MAX_VALUE, 0);
            case LANTERN:
                return new PotionEffect(PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, 0); 
            case COIN:
                return new PotionEffect(PotionEffectType.LUCK, Integer.MAX_VALUE, 0); 
            default:
                return null; 
        }
    }
    
    /**
     * Checks if the player is currently pledged. 
     * @param p the player being checked.
     * @return true if the player is pledged to a god
     *  and false otherwise. 
     */
    private boolean isPledged(Player p)
    {
        return pledges.containsKey(p.getName()); 
    }
    
    /**
     * Gives buff to player after a 3 tick delay(makes sure player gets buff). 
     * @param p the player getting buffed. 
     * if the player does not have a god this method will do nothing. 
     */
    private void giveBuff(Player p)
    {
        PotionEffect potion = getEffect(p);
        
        if (potion == null)
        {
            return; 
        }
        
        this.getServer().getScheduler().runTaskLater(this, new Runnable() {
            public void run () {
                p.addPotionEffect(potion);
            }
        }, 3); 
    }
}

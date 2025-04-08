package me.evade.LifeSteel;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.ArrayList;
import java.util.List;
public class RecipeManager {
    public static ShapedRecipe createReviveBeaconRecipe() {
        ItemStack reviveBeacon = createReviveBeaconItem();
        NamespacedKey key = new NamespacedKey(JavaPlugin.getProvidingPlugin(LifeSteel.class), "revive_beacon");
        ShapedRecipe recipe = new ShapedRecipe(key, reviveBeacon);
        recipe.shape("ENE", "NBN", "ENE");
        recipe.setIngredient('E', Material.EMERALD_BLOCK);
        recipe.setIngredient('N', Material.NETHERITE_INGOT);
        recipe.setIngredient('B', Material.BEACON);
        return recipe;
    }
    public static ItemStack createReviveBeaconItem() {
        ItemStack reviveBeacon = new ItemStack(Material.BEACON);
        ItemMeta meta = reviveBeacon.getItemMeta();
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&bRevive Beacon"));
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.translateAlternateColorCodes('&', "&7Right Click this beacon and enter a player's name to revive them!"));
        meta.setLore(lore);
        reviveBeacon.setItemMeta(meta);
        return reviveBeacon;
    }
}
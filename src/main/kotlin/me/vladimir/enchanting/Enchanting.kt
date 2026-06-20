package me.vladimir.enchanting

import me.vladimir.enchanting.commands.Ebook
import me.vladimir.enchanting.listeners.Table
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.ShapedRecipe
import org.bukkit.plugin.java.JavaPlugin


class Enchanting : JavaPlugin() {

    override fun onEnable() {
        logger.info("[Enchanting] Loaded.")
        server.pluginManager.registerEvents(Table(), this)
        registerCommands()
        val eBook = ItemStack(Material.ENCHANTED_BOOK)
        val meta = eBook.itemMeta
        meta.customName(
            Component.text("마법 부여 주문서")
                .decoration(TextDecoration.ITALIC, false)
                .color(TextColor.color(150, 0, 255))
        )
        meta.setMaxStackSize(64)
        meta.setCustomModelData(777777)
        eBook.itemMeta = meta
        val key = NamespacedKey(this, "ebook")
        val recipe = ShapedRecipe(key, eBook).apply {
            shape(
                " L ",
                "LBL",
                " L "
            )
            setIngredient('L', Material.LAPIS_LAZULI)
            setIngredient('B', Material.BOOK)
        }
        server.addRecipe(recipe)
    }


    override fun onDisable() {
        logger.info("[Enchanting] Unloaded.")
    }
    private fun registerCommands() {
        getCommand("ebook")!!.setExecutor(Ebook())
    }

}

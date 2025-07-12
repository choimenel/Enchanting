package me.vladimir.enchanting.listeners

import io.github.monun.invfx.InvFX
import io.github.monun.invfx.openFrame
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.Sound
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.ItemStack
import java.util.*

class Table : Listener {
    private val playerItemMap = mutableMapOf<UUID, ItemStack?>()

    @EventHandler
    fun table(event: PlayerInteractEvent) {
        val block = event.clickedBlock
        if (event.action == Action.RIGHT_CLICK_BLOCK && block?.type == Material.ENCHANTING_TABLE) {
            event.isCancelled = true
            open(event.player)
        }
    }

    private fun open(player: Player) {
        val uuid = player.uniqueId

        val frame = InvFX.frame(
            4,
            Component.text("ꈂꈂꈂꈂꈂꈂꈂꈂй").color(TextColor.color(255, 255, 255))
        ) {
            val centerSlot = slot(4, 1) {
                item = playerItemMap[uuid]

                onClick { e ->
                    e.isCancelled = true

                    if (e.click == ClickType.SHIFT_LEFT || e.click == ClickType.SHIFT_RIGHT) {
                        playerItemMap[uuid]?.let {
                            player.inventory.addItem(it)
                            playerItemMap[uuid] = null
                            item = null
                        }
                        return@onClick
                    }

                    val itemToEnchant = playerItemMap[uuid]
                    if (itemToEnchant != null && itemToEnchant.isEnchantable) {
                        val requiredBook = player.inventory.contents.firstOrNull {
                            it != null &&
                                    it.type == Material.ENCHANTED_BOOK &&
                                    it.itemMeta.customName() == Component.text("마법 부여 주문서")
                                .decoration(TextDecoration.ITALIC, false)
                                .color(TextColor.color(150, 0, 255)) &&
                                    it.itemMeta?.hasCustomModelData() == true &&
                                    it.itemMeta?.customModelData == 777777
                        }

                        if (requiredBook == null) {
                            player.sendMessage(Component.text("마법 부여 주문서가 필요합니다!").color(TextColor.color(255, 50, 50)))
                            player.playSound(player.location, Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f)
                            return@onClick
                        }

                        requiredBook.amount -= 1
                        if (requiredBook.amount <= 0) {
                            player.inventory.removeItem(requiredBook)
                        }
                        if (itemHasBeenUpgraded(itemToEnchant)) {
                            player.sendMessage(Component.text("강화한 장비는 마법을 부여할 수 없습니다!").color(TextColor.color(255, 50, 50)))
                            player.playSound(player.location, Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f)
                            return@onClick
                        }

                        val random = Random()
                        val possible = Enchantment.values().filter { it.canEnchantItem(itemToEnchant) }
                        val count = random.nextInt(3) + 1

                        itemToEnchant.enchantments.keys.forEach {
                            itemToEnchant.removeEnchantment(it)
                        }

                        repeat(count) {
                            val enchant = possible.random()
                            val level = (1..enchant.maxLevel).random()
                            itemToEnchant.addUnsafeEnchantment(enchant, level)
                            player.playSound(player.location, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1.0f, 2.0f)
                        }

                        playerItemMap[uuid] = itemToEnchant
                        this.item = itemToEnchant
                    }
                }
            }

            onClickBottom { e ->
                val clicked = e.currentItem ?: return@onClickBottom
                if (!clicked.isEnchantable) return@onClickBottom

                val clone = clicked.clone()
                if (player.inventory.contains(clicked)) {
                    player.inventory.removeItem(clicked)
                }

                playerItemMap[uuid] = clone
                centerSlot.item = clone
                e.isCancelled = true
            }

            onClose {
                playerItemMap[uuid]?.let {
                    player.inventory.addItem(it)
                    playerItemMap.remove(uuid)
                }
            }

            for (x in 0..8) {
                for (y in 0..3) {
                    if (x == 4 && y == 1) continue
                    slot(x, y) {
                        item = ItemStack(Material.IRON_NUGGET)
                        val meta = item!!.itemMeta
                        meta.itemModel = NamespacedKey("minecraft", "air")
                        meta.isHideTooltip = true
                        item!!.itemMeta = meta
                    }
                }
            }
        }

        player.openFrame(frame)
    }

    private val ItemStack.isEnchantable: Boolean
        get() = type != Material.AIR &&
                Enchantment.values().any { it.canEnchantItem(this) }
    private fun itemHasBeenUpgraded(item: ItemStack): Boolean {
        val meta = item.itemMeta ?: return false
        val lore = meta.lore() ?: return false
        val plainLore = lore.map {
            PlainTextComponentSerializer.plainText().serialize(it)
        }
        return plainLore.any { it.contains("☆") || it.contains("★") }
    }
}

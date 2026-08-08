package immersive_machinery.entity.inventory;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * Abstraction layer for inventory access, allowing compatibility with mods that
 * do not implement the vanilla {@link net.minecraft.world.Container} interface
 * (e.g. Sophisticated Storage on Fabric, which uses the Fabric Transfer API).
 */
public interface InventoryAccess {

    /**
     * Check whether the block at the given position has an accessible inventory.
     */
    boolean hasInventory(Level level, BlockPos pos);

    /**
     * Get a snapshot of all distinct extractable item stacks at the given position.
     * Each entry's count is the total available amount of that item type (aggregated
     * across slots / storage views).
     */
    List<ItemStack> getAvailableItems(Level level, BlockPos pos);

    /**
     * Extract items matching {@code template} from the inventory, up to {@code maxCount}.
     *
     * @param compareTag if true, match item + NBT; if false, match item only
     * @return the extracted stack (may be smaller than maxCount), or EMPTY if nothing matched
     */
    ItemStack extract(Level level, BlockPos pos, ItemStack template, int maxCount, boolean compareTag);

    /**
     * Insert a stack into the inventory.
     *
     * @return the remainder that could not be inserted (EMPTY if everything was inserted)
     */
    ItemStack insert(Level level, BlockPos pos, ItemStack stack);

    /**
     * Simulate insertion without modifying the inventory.
     *
     * @return the number of items that could be inserted
     */
    int simulateInsert(Level level, BlockPos pos, ItemStack stack);
}

package immersive_machinery.entity.inventory;

import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * Default {@link InventoryAccess} implementation backed by the vanilla
 * {@link Container} interface. Used on Forge and as a fallback on Fabric.
 */
public class ContainerInventoryAccess implements InventoryAccess {

    protected Container getContainer(Level level, BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        return be instanceof Container container ? container : null;
    }

    @Override
    public boolean hasInventory(Level level, BlockPos pos) {
        return getContainer(level, pos) != null;
    }

    @Override
    public List<ItemStack> getAvailableItems(Level level, BlockPos pos) {
        Container container = getContainer(level, pos);
        if (container == null) {
            return List.of();
        }
        List<ItemStack> items = new ArrayList<>();
        for (int slot = 0; slot < container.getContainerSize(); slot++) {
            ItemStack stack = container.getItem(slot);
            if (!stack.isEmpty() && container.canTakeItem(container, slot, stack)) {
                // Aggregate by item + NBT
                boolean found = false;
                for (ItemStack existing : items) {
                    if (ItemStack.isSameItemSameTags(stack, existing)) {
                        existing.grow(stack.getCount());
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    items.add(stack.copy());
                }
            }
        }
        return items;
    }

    @Override
    public ItemStack extract(Level level, BlockPos pos, ItemStack template, int maxCount, boolean compareTag) {
        Container container = getContainer(level, pos);
        if (container == null) {
            return ItemStack.EMPTY;
        }
        int remaining = maxCount;
        ItemStack result = ItemStack.EMPTY;
        for (int slot = 0; slot < container.getContainerSize() && remaining > 0; slot++) {
            ItemStack stack = container.getItem(slot);
            if (stack.isEmpty() || !container.canTakeItem(container, slot, stack)) {
                continue;
            }
            boolean matches = compareTag
                    ? ItemStack.isSameItemSameTags(stack, template)
                    : ItemStack.isSameItem(stack, template);
            if (!matches) {
                continue;
            }
            int toExtract = Math.min(remaining, stack.getCount());
            ItemStack extracted = container.removeItem(slot, toExtract);
            if (!extracted.isEmpty()) {
                if (result.isEmpty()) {
                    result = extracted.copy();
                } else {
                    result.grow(extracted.getCount());
                }
                remaining -= extracted.getCount();
            }
        }
        if (!result.isEmpty()) {
            markChanged(level, pos);
        }
        return result;
    }

    @Override
    public ItemStack insert(Level level, BlockPos pos, ItemStack stack) {
        Container container = getContainer(level, pos);
        if (container == null) {
            return stack;
        }
        ItemStack remainder = stack.copy();
        for (int slot = 0; slot < container.getContainerSize(); slot++) {
            if (remainder.isEmpty()) {
                break;
            }
            ItemStack slotStack = container.getItem(slot);
            if (!container.canPlaceItem(slot, remainder)) {
                continue;
            }
            if (ItemStack.isSameItemSameTags(remainder, slotStack)) {
                int count = Math.min(remainder.getCount(), slotStack.getMaxStackSize() - slotStack.getCount());
                if (count > 0) {
                    slotStack.grow(count);
                    remainder.shrink(count);
                    container.setItem(slot, slotStack);
                }
            } else if (slotStack.isEmpty()) {
                container.setItem(slot, remainder.copyAndClear());
            }
        }
        if (remainder.getCount() != stack.getCount()) {
            markChanged(level, pos);
        }
        return remainder;
    }

    @Override
    public int simulateInsert(Level level, BlockPos pos, ItemStack stack) {
        Container container = getContainer(level, pos);
        if (container == null) {
            return 0;
        }
        int remaining = stack.getCount();
        for (int slot = 0; slot < container.getContainerSize(); slot++) {
            ItemStack slotStack = container.getItem(slot);
            if (!container.canPlaceItem(slot, stack)) {
                continue;
            }
            if (ItemStack.isSameItemSameTags(stack, slotStack)) {
                remaining -= (slotStack.getMaxStackSize() - slotStack.getCount());
            } else if (slotStack.isEmpty()) {
                remaining -= stack.getMaxStackSize();
            }
            if (remaining <= 0) {
                return stack.getCount();
            }
        }
        return Math.max(0, stack.getCount() - remaining);
    }

    protected void markChanged(Level level, BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be != null) {
            be.setChanged();
        }
    }
}

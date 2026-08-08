package immersive_machinery.fabric.inventory;

import immersive_machinery.entity.inventory.ContainerInventoryAccess;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

/**
 * Fabric-specific {@link ContainerInventoryAccess} that uses the Fabric Transfer
 * API ({@link ItemStorage}) as the primary inventory interface, falling back to
 * the vanilla {@link net.minecraft.world.Container} when no Transfer API storage
 * is registered.
 * <p>
 * This enables compatibility with mods such as Sophisticated Storage whose
 * blocks do not implement {@code Container} but do expose a {@code Storage}.
 */
@SuppressWarnings("UnstableApiUsage")
public class FabricInventoryAccess extends ContainerInventoryAccess {

    private Storage<ItemVariant> getStorage(Level level, BlockPos pos) {
        return ItemStorage.SIDED.find(level, pos, null);
    }

    @Override
    public boolean hasInventory(Level level, BlockPos pos) {
        if (getStorage(level, pos) != null) {
            return true;
        }
        return super.hasInventory(level, pos);
    }

    @Override
    public List<ItemStack> getAvailableItems(Level level, BlockPos pos) {
        Storage<ItemVariant> storage = getStorage(level, pos);
        if (storage != null) {
            List<ItemStack> items = new ArrayList<>();
            try (Transaction tx = Transaction.openOuter()) {
                for (StorageView<ItemVariant> view : storage) {
                    ItemVariant variant = view.getResource();
                    if (!variant.isBlank()) {
                        int amount = (int) Math.min(view.getAmount(), Integer.MAX_VALUE);
                        if (amount > 0) {
                            items.add(variant.toStack(amount));
                        }
                    }
                }
                // Auto-abort: we only read
            }
            return items;
        }
        return super.getAvailableItems(level, pos);
    }

    @Override
    public ItemStack extract(Level level, BlockPos pos, ItemStack template, int maxCount, boolean compareTag) {
        Storage<ItemVariant> storage = getStorage(level, pos);
        if (storage != null) {
            try (Transaction tx = Transaction.openOuter()) {
                if (compareTag) {
                    // Match exact item + NBT
                    ItemVariant target = ItemVariant.of(template);
                    long extracted = storage.extract(target, maxCount, tx);
                    if (extracted > 0) {
                        tx.commit();
                        return target.toStack((int) extracted);
                    }
                } else {
                    // Match item only — collect matching variants first to avoid
                    // concurrent modification during iteration
                    List<ItemVariant> matching = new ArrayList<>();
                    for (StorageView<ItemVariant> view : storage) {
                        ItemVariant variant = view.getResource();
                        if (!variant.isBlank() && variant.getItem() == template.getItem()) {
                            matching.add(variant);
                        }
                    }
                    int total = 0;
                    ItemStack result = ItemStack.EMPTY;
                    for (ItemVariant variant : matching) {
                        if (total >= maxCount) {
                            break;
                        }
                        long amount = storage.extract(variant, maxCount - total, tx);
                        if (amount > 0) {
                            if (result.isEmpty()) {
                                result = variant.toStack((int) amount);
                            } else {
                                result.grow((int) amount);
                            }
                            total += amount;
                        }
                    }
                    if (total > 0) {
                        tx.commit();
                        return result;
                    }
                }
                // Nothing extracted — auto-abort
            }
            return ItemStack.EMPTY;
        }
        return super.extract(level, pos, template, maxCount, compareTag);
    }

    @Override
    public ItemStack insert(Level level, BlockPos pos, ItemStack stack) {
        Storage<ItemVariant> storage = getStorage(level, pos);
        if (storage != null) {
            ItemStack remainder = stack.copy();
            try (Transaction tx = Transaction.openOuter()) {
                ItemVariant variant = ItemVariant.of(stack);
                long inserted = storage.insert(variant, stack.getCount(), tx);
                tx.commit();
                remainder.shrink((int) inserted);
            }
            return remainder;
        }
        return super.insert(level, pos, stack);
    }

    @Override
    public int simulateInsert(Level level, BlockPos pos, ItemStack stack) {
        Storage<ItemVariant> storage = getStorage(level, pos);
        if (storage != null) {
            try (Transaction tx = Transaction.openOuter()) {
                ItemVariant variant = ItemVariant.of(stack);
                long inserted = storage.insert(variant, stack.getCount(), tx);
                // Do NOT commit — auto-abort so nothing is actually stored
                return (int) Math.min(inserted, Integer.MAX_VALUE);
            }
        }
        return super.simulateInsert(level, pos, stack);
    }
}

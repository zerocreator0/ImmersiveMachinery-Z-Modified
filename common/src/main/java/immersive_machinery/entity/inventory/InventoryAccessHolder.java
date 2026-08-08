package immersive_machinery.entity.inventory;

/**
 * Holds the platform-specific {@link InventoryAccess} instance.
 * The default is {@link ContainerInventoryAccess}; platform modules
 * (e.g. Fabric) replace it during initialisation.
 */
public final class InventoryAccessHolder {

    private static InventoryAccess instance = new ContainerInventoryAccess();

    private InventoryAccessHolder() {
    }

    public static InventoryAccess get() {
        return instance;
    }

    public static void set(InventoryAccess access) {
        instance = access;
    }
}

package immersive_machinery.config;

import immersive_aircraft.config.JsonConfig;
import immersive_aircraft.config.configEntries.BooleanConfigEntry;
import immersive_aircraft.config.configEntries.IntegerConfigEntry;
import immersive_machinery.Common;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public final class Config extends JsonConfig {
    private static final Config INSTANCE = loadOrCreate(new Config(Common.MOD_ID), Config.class);

    public Config(String name) {
        super(name);
    }

    public static Config getInstance() {
        // Gson's Unsafe.allocateInstance bypasses field initializers, so validCrops
        // can be null after deserialization if the JSON file lacks the field.
        // Ensure it's never null at access time.
        if (INSTANCE.validCrops == null) {
            INSTANCE.validCrops = createDefaultValidCrops();
            INSTANCE.save();
        }
        if (INSTANCE.stemFruits == null) {
            INSTANCE.stemFruits = createDefaultStemFruits();
            INSTANCE.save();
        }
        return INSTANCE;
    }

    @BooleanConfigEntry(true)
    public boolean waterRenderingFixForCopperfin;

    @IntegerConfigEntry(5)
    public int redstoneSheepMinHorizontalScanRange = 5;

    @BooleanConfigEntry(true)
    public boolean redstoneSheepChunkOnlyMode = true;

    @IntegerConfigEntry(20)
    public int fuelTicksPerHarvest = 20;

    public Map<String, Boolean> validCrops = createDefaultValidCrops();

    /**
     * Blocks treated as "stem fruits" — solid blocks that grow beside a stem
     * (pumpkin, melon, etc.). The sheep navigates to the block ABOVE these
     * and harvests from there, because the pathfinder can't route through
     * solid blocks.
     *
     * Add other mods' solid fruit blocks here by their registry name, e.g.
     * "farmersdelight:cabbages".
     */
    public Set<String> stemFruits = createDefaultStemFruits();

    private static Map<String, Boolean> createDefaultValidCrops() {
        Map<String, Boolean> map = new HashMap<>();
        map.put("minecraft:grass", true);
        map.put("minecraft:pumpkin", true);
        map.put("minecraft:melon", true);
        return map;
    }

    private static Set<String> createDefaultStemFruits() {
        Set<String> set = new LinkedHashSet<>();
        set.add("minecraft:pumpkin");
        set.add("minecraft:melon");
        return set;
    }
}

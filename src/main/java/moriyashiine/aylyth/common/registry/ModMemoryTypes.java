package moriyashiine.aylyth.common.registry;

import moriyashiine.aylyth.common.entity.ai.brain.WreathedHindBrain;
import moriyashiine.aylyth.common.util.AylythUtil;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Unit;
import net.minecraft.util.registry.Registry;

import java.util.Optional;

public class ModMemoryTypes {
    public static final MemoryModuleType<PlayerEntity> NEAREST_VISIBLE_PLAYER_NEMESIS = register("nearest_visible_player_nemesis");
    public static final MemoryModuleType<PlayerEntity> OWNER_PLAYER = register("owner_player");
    public static final MemoryModuleType<Unit> SHOULD_FOLLOW_OWNER = register("should_follow_owner");
    public static final MemoryModuleType<PlayerEntity> PLEDGED_PLAYER = register("pledged_player");
    public static final MemoryModuleType<WreathedHindBrain.SecondChance> SECOND_CHANCE = register("second_chance");

    private static <U> MemoryModuleType<U> register(String id) {
        return Registry.register(Registry.MEMORY_MODULE_TYPE, AylythUtil.id(id), new MemoryModuleType<>(Optional.empty()));
    }

    public static void init() {}
}

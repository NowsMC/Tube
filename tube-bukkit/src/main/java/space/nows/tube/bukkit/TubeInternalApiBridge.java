package space.nows.tube.bukkit;

import com.destroystokyo.paper.SkinParts;
import com.destroystokyo.paper.util.VersionFetcher;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.datacomponent.item.ResolvableProfile;
import io.papermc.paper.entity.poi.PoiType;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import io.papermc.paper.world.damagesource.CombatEntry;
import io.papermc.paper.world.damagesource.FallLocationType;
import java.io.IOException;
import java.util.Set;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.function.Predicate;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.flattener.ComponentFlattener;
import net.kyori.adventure.util.Services;
import org.bukkit.GameRule;
import org.bukkit.NamespacedKey;
import org.bukkit.Statistic;
import org.bukkit.attribute.Attributable;
import org.bukkit.block.Biome;
import org.bukkit.command.CommandSender;
import org.bukkit.damage.DamageEffect;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Pose;
import org.bukkit.entity.SpawnCategory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.Nullable;
import io.papermc.paper.*;

public class TubeInternalApiBridge implements InternalAPIBridge {

    @Override
    public @ApiStatus.ScheduledForRemoval(inVersion = "1.22") Biome constructLegacyCustomBiome() {
        throw new UnsupportedOperationException("InternalAPIBridge#constructLegacyCustomBiome is not supported by Tube");
    }

    @Override
    public CombatEntry createCombatEntry(LivingEntity entity, DamageSource damageSource, float damage) {
        throw new UnsupportedOperationException("InternalAPIBridge#createCombatEntry is not supported by Tube");
    }

    @Override
    public CombatEntry createCombatEntry(DamageSource damageSource, float damage, @Nullable FallLocationType fallLocationType, float fallDistance) {
        throw new UnsupportedOperationException("InternalAPIBridge#createCombatEntry is not supported by Tube");
    }

    @Override
    public Predicate<CommandSourceStack> restricted(Predicate<CommandSourceStack> predicate) {
        throw new UnsupportedOperationException("InternalAPIBridge#restricted is not supported by Tube");
    }

    @Override
    public ResolvableProfile defaultMannequinProfile() {
        throw new UnsupportedOperationException("InternalAPIBridge#defaultMannequinProfile is not supported by Tube");
    }

    @Override
    public SkinParts.Mutable allSkinParts() {
        throw new UnsupportedOperationException("InternalAPIBridge#allSkinParts is not supported by Tube");
    }

    @Override
    public Component defaultMannequinDescription() {
        throw new UnsupportedOperationException("InternalAPIBridge#defaultMannequinDescription is not supported by Tube");
    }

    @Override
    public <MODERN, LEGACY> GameRule<LEGACY> legacyGameRuleBridge(GameRule<MODERN> rule, Function<LEGACY, MODERN> fromLegacyToModern, Function<MODERN, LEGACY> toLegacyFromModern, Class<LEGACY> legacyClass) {
        throw new UnsupportedOperationException("InternalAPIBridge#legacyGameRuleBridge is not supported by Tube");
    }

    @Override
    public Set<Pose> validMannequinPoses() {
        throw new UnsupportedOperationException("InternalAPIBridge#validMannequinPoses is not supported by Tube");
    }

    @Override
    public PoiType.Occupancy createOccupancy(String enumNameEntry) {
        throw new UnsupportedOperationException("InternalAPIBridge#createOccupancy is not supported by Tube");
    }

    @Override
    public DamageSource.Builder createDamageSourceBuilder(DamageType damageType) {
        throw new UnsupportedOperationException("InternalAPIBridge#createDamageSourceBuilder is not supported by Tube");
    }

    @Override
    public DamageEffect getDamageEffect(String key) {
        throw new UnsupportedOperationException("InternalAPIBridge#getDamageEffect is not supported by Tube");
    }

    @Override
    public String getTranslationKey(EntityType entityType) {
        throw new UnsupportedOperationException("InternalAPIBridge#getTranslationKey is not supported by Tube");
    }

    @Override
    public SpawnCategory getSpawnCategory(EntityType entityType) {
        throw new UnsupportedOperationException("InternalAPIBridge#getSpawnCategory is not supported by Tube");
    }

    @Override
    public ItemStack deserializeItem(byte[] data) {
        throw new UnsupportedOperationException("InternalAPIBridge#deserializeItem is not supported by Tube");
    }

    @Override
    public boolean hasDefaultEntityAttributes(NamespacedKey entityKey) {
        throw new UnsupportedOperationException("InternalAPIBridge#hasDefaultEntityAttributes is not supported by Tube");
    }

    @Override
    public Attributable getDefaultEntityAttributes(NamespacedKey entityKey) {
        throw new UnsupportedOperationException("InternalAPIBridge#getDefaultEntityAttributes is not supported by Tube");
    }

    @Override
    public String getStatisticCriteriaKey(Statistic statistic) {
        throw new UnsupportedOperationException("InternalAPIBridge#getStatisticCriteriaKey is not supported by Tube");
    }

    @Override
    public ItemStack createEmptyStack() {
        throw new UnsupportedOperationException("InternalAPIBridge#createEmptyStack is not supported by Tube");
    }

    @Override
    public Component resolveWithContext(Component component, @Nullable CommandSender context, @Nullable Entity scoreboardSubject, boolean bypassPermissions) throws IOException {
        throw new UnsupportedOperationException("InternalAPIBridge#resolveWithContext is not supported by Tube");
    }

    @Override
    public ComponentFlattener componentFlattener() {
        throw new UnsupportedOperationException("InternalAPIBridge#componentFlattener is not supported by Tube");
    }
    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public LifecycleEventManager<Plugin> createPluginLifecycleEventManager(JavaPlugin plugin, BooleanSupplier registrationCheck) {
        return (LifecycleEventManager<Plugin>) Proxies.of((Class) LifecycleEventManager.class, "LifecycleEventManager")
                .on("registerEventHandler", args -> null)
                .build();
    }
}

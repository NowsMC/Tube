package space.nows.tube.server.player;

import net.kyori.adventure.nbt.BinaryTagIO;
import net.kyori.adventure.nbt.BinaryTagTypes;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.nbt.DoubleBinaryTag;
import net.kyori.adventure.nbt.FloatBinaryTag;
import net.kyori.adventure.nbt.ListBinaryTag;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.item.ItemStack;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Optional;
import java.util.UUID;

public final class PlayerDataStore {

    public record Spawn(String world, Pos position) {
    }

    private static final int STORAGE_SLOTS = 36;
    private static final int FEET = 100;
    private static final int LEGS = 101;
    private static final int CHEST = 102;
    private static final int HEAD = 103;
    private static final int OFFHAND = -106;

    private final Path directory;

    public PlayerDataStore(Path directory) {
        this.directory = directory;
    }

    public Optional<CompoundBinaryTag> read(UUID uuid) {
        Path file = file(uuid);
        if (!Files.isRegularFile(file)) {
            return Optional.empty();
        }
        try {
            return Optional.of(BinaryTagIO.reader().read(file, BinaryTagIO.Compression.GZIP));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public Optional<Spawn> spawn(UUID uuid) {
        return read(uuid).flatMap(data -> {
            ListBinaryTag pos = data.getList("Pos", BinaryTagTypes.DOUBLE);
            ListBinaryTag rotation = data.getList("Rotation", BinaryTagTypes.FLOAT);
            String world = data.getCompound("tube").getString("world");
            if (pos.size() != 3 || world.isEmpty()) {
                return Optional.empty();
            }
            float yaw = rotation.size() == 2 ? rotation.getFloat(0) : 0f;
            float pitch = rotation.size() == 2 ? rotation.getFloat(1) : 0f;
            return Optional.of(new Spawn(world, new Pos(pos.getDouble(0), pos.getDouble(1), pos.getDouble(2), yaw, pitch)));
        });
    }

    public void save(Player player, String world) {
        Pos position = player.getPosition();
        ListBinaryTag.Builder<DoubleBinaryTag> pos = ListBinaryTag.builder(BinaryTagTypes.DOUBLE);
        pos.add(DoubleBinaryTag.doubleBinaryTag(position.x()));
        pos.add(DoubleBinaryTag.doubleBinaryTag(position.y()));
        pos.add(DoubleBinaryTag.doubleBinaryTag(position.z()));
        ListBinaryTag.Builder<FloatBinaryTag> rotation = ListBinaryTag.builder(BinaryTagTypes.FLOAT);
        rotation.add(FloatBinaryTag.floatBinaryTag(position.yaw()));
        rotation.add(FloatBinaryTag.floatBinaryTag(position.pitch()));

        CompoundBinaryTag data = CompoundBinaryTag.builder()
                .put("Pos", pos.build())
                .put("Rotation", rotation.build())
                .put("Inventory", inventory(player))
                .putInt("SelectedItemSlot", player.getHeldSlot())
                .putFloat("Health", player.getHealth())
                .putInt("foodLevel", player.getFood())
                .putFloat("foodSaturationLevel", player.getFoodSaturation())
                .putInt("XpLevel", player.getLevel())
                .putFloat("XpP", player.getExp())
                .putInt("playerGameType", player.getGameMode().ordinal())
                .putString("Dimension", "minecraft:overworld")
                .put("tube", tubeData(player, world))
                .build();
        write(player.getUuid(), data);
    }

    private static CompoundBinaryTag tubeData(Player player, String world) {
        CompoundBinaryTag.Builder tube = CompoundBinaryTag.builder().putString("world", world);
        RespawnPoint respawn = player.getTag(RespawnPoint.TAG);
        if (respawn != null) {
            tube.putString("respawnWorld", respawn.world())
                    .putInt("respawnX", respawn.position().blockX())
                    .putInt("respawnY", respawn.position().blockY())
                    .putInt("respawnZ", respawn.position().blockZ());
        }
        return tube.build();
    }

    public void apply(CompoundBinaryTag data, Player player, boolean restoreGameMode) {
        CompoundBinaryTag tube = data.getCompound("tube");
        if (tube.keySet().contains("respawnWorld")) {
            player.setTag(RespawnPoint.TAG, new RespawnPoint(tube.getString("respawnWorld"),
                    new Pos(tube.getInt("respawnX"), tube.getInt("respawnY"), tube.getInt("respawnZ"))));
        }
        var registries = MinecraftServer.getRegistries();
        for (var element : data.getList("Inventory", BinaryTagTypes.COMPOUND)) {
            CompoundBinaryTag tag = (CompoundBinaryTag) element;
            ItemStack stack = ItemStack.fromItemNBT(tag, registries);
            switch (tag.getByte("Slot")) {
                case FEET -> player.setEquipment(EquipmentSlot.BOOTS, stack);
                case LEGS -> player.setEquipment(EquipmentSlot.LEGGINGS, stack);
                case CHEST -> player.setEquipment(EquipmentSlot.CHESTPLATE, stack);
                case HEAD -> player.setEquipment(EquipmentSlot.HELMET, stack);
                case (byte) OFFHAND -> player.setEquipment(EquipmentSlot.OFF_HAND, stack);
                default -> {
                    int slot = tag.getByte("Slot");
                    if (slot >= 0 && slot < STORAGE_SLOTS) {
                        player.getInventory().setItemStack(slot, stack);
                    }
                }
            }
        }
        if (data.keySet().contains("SelectedItemSlot")) {
            player.setHeldItemSlot((byte) Math.max(0, Math.min(8, data.getInt("SelectedItemSlot"))));
        }
        if (data.keySet().contains("Health")) {
            player.setHealth(data.getFloat("Health"));
        }
        if (data.keySet().contains("foodLevel")) {
            player.setFood(data.getInt("foodLevel"));
            player.setFoodSaturation(data.getFloat("foodSaturationLevel"));
        }
        player.setLevel(data.getInt("XpLevel"));
        player.setExp(data.getFloat("XpP"));
        if (restoreGameMode && data.keySet().contains("playerGameType")) {
            GameMode[] modes = GameMode.values();
            int mode = data.getInt("playerGameType");
            if (mode >= 0 && mode < modes.length) {
                player.setGameMode(modes[mode]);
            }
        }
    }

    private ListBinaryTag inventory(Player player) {
        ListBinaryTag.Builder<CompoundBinaryTag> items = ListBinaryTag.builder(BinaryTagTypes.COMPOUND);
        var registries = MinecraftServer.getRegistries();
        for (int slot = 0; slot < STORAGE_SLOTS; slot++) {
            add(items, player.getInventory().getItemStack(slot), slot, registries);
        }
        add(items, player.getEquipment(EquipmentSlot.BOOTS), FEET, registries);
        add(items, player.getEquipment(EquipmentSlot.LEGGINGS), LEGS, registries);
        add(items, player.getEquipment(EquipmentSlot.CHESTPLATE), CHEST, registries);
        add(items, player.getEquipment(EquipmentSlot.HELMET), HEAD, registries);
        add(items, player.getEquipment(EquipmentSlot.OFF_HAND), OFFHAND, registries);
        return items.build();
    }

    private static void add(ListBinaryTag.Builder<CompoundBinaryTag> items, ItemStack stack, int slot, net.minestom.server.registry.Registries registries) {
        if (stack.isAir()) {
            return;
        }
        items.add(stack.toItemNBT(registries).putByte("Slot", (byte) slot));
    }

    private void write(UUID uuid, CompoundBinaryTag data) {
        try {
            Files.createDirectories(directory);
            Path target = file(uuid);
            Path partial = directory.resolve(uuid + ".dat.tmp");
            BinaryTagIO.writer().write(data, partial, BinaryTagIO.Compression.GZIP);
            if (Files.exists(target)) {
                Files.copy(target, directory.resolve(uuid + ".dat_old"), StandardCopyOption.REPLACE_EXISTING);
            }
            Files.move(partial, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private Path file(UUID uuid) {
        return directory.resolve(uuid + ".dat");
    }
}

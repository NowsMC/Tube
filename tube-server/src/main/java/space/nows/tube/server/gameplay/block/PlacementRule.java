package space.nows.tube.server.gameplay.block;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import net.minestom.server.registry.RegistryTag;
import net.minestom.server.registry.TagKey;

import java.util.HashSet;
import java.util.Set;

final class PlacementRule extends BlockPlacementRule {

    enum Support {
        NONE, FLOOR, ANY_FLOOR, SOIL, FARMLAND, SAND_OR_SOIL, SUGAR_CANE, CACTUS, WALL
    }

    private final String name;
    private final Set<String> facings;
    private final boolean axis;
    private final boolean stairHalf;
    private final boolean slab;
    private final boolean attachFace;
    private final boolean rotation;
    private final boolean hanging;
    private final boolean waterlogged;
    private final boolean connects;
    private final boolean wallConnects;
    private final boolean chest;
    private final boolean stairs;
    private final Block wallVariant;
    private final Support support;
    private final boolean multiPart;

    PlacementRule(Block block) {
        super(block);
        this.name = block.key().value();
        Set<String> facingValues = values(block, "facing");
        this.facings = facingValues.isEmpty() ? null : facingValues;
        this.axis = values(block, "axis").contains("y") && values(block, "axis").size() == 3;
        Set<String> halves = values(block, "half");
        this.stairHalf = halves.contains("top") && halves.contains("bottom");
        this.slab = values(block, "type").contains("double");
        this.attachFace = values(block, "face").contains("wall");
        this.rotation = values(block, "rotation").size() == 16;
        this.hanging = block.properties().containsKey("hanging");
        this.waterlogged = block.properties().containsKey("waterlogged");
        Set<String> north = values(block, "north");
        this.connects = north.contains("true") && block.properties().containsKey("east") && !name.contains("mushroom") && !name.equals("fire")
                && !name.equals("vine") && !name.equals("glow_lichen") && !name.equals("sculk_vein") && !name.equals("chorus_plant")
                && !name.equals("resin_clump") && !name.equals("tripwire");
        this.wallConnects = north.contains("low");
        this.chest = name.equals("chest") || name.equals("trapped_chest");
        this.stairs = name.endsWith("_stairs");
        this.wallVariant = wallVariant(name);
        this.multiPart = name.endsWith("_bed") || halves.contains("upper");
        this.support = support(block, name);
    }

    static boolean applies(Block block) {
        Set<String> keys = block.properties().keySet();
        return keys.contains("facing") || keys.contains("axis") || keys.contains("half") || keys.contains("type") || keys.contains("face")
                || keys.contains("rotation") || keys.contains("hanging") || keys.contains("persistent") || keys.contains("waterlogged") || keys.contains("north")
                || wallVariant(block.key().value()) != null || support(block, block.key().value()) != Support.NONE;
    }

    private static Set<String> values(Block block, String property) {
        Set<String> values = new HashSet<>();
        if (!block.properties().containsKey(property)) {
            return values;
        }
        for (Block state : block.possibleStates()) {
            values.add(state.getProperty(property));
        }
        return values;
    }

    private static Block wallVariant(String name) {
        if (name.contains("wall") || name.contains("hanging")) {
            return null;
        }
        int split = name.lastIndexOf('_');
        String candidate = split < 0 ? "wall_" + name : name.substring(0, split) + "_wall" + name.substring(split);
        return Block.fromKey("minecraft:" + candidate);
    }

    private static Support support(Block block, String name) {
        if (name.equals("sugar_cane")) {
            return Support.SUGAR_CANE;
        }
        if (name.equals("cactus")) {
            return Support.CACTUS;
        }
        if (name.equals("dead_bush")) {
            return Support.SAND_OR_SOIL;
        }
        if (name.equals("wheat") || name.equals("carrots") || name.equals("potatoes") || name.equals("beetroots")
                || name.endsWith("_stem") || name.equals("torchflower_crop") || name.equals("pitcher_crop")) {
            return Support.FARMLAND;
        }
        if (tagged(block, "#minecraft:saplings") || tagged(block, "#minecraft:small_flowers") || name.equals("short_grass") || name.equals("fern")
                || name.equals("tall_grass") || name.equals("large_fern") || name.equals("sunflower") || name.equals("lilac")
                || name.equals("rose_bush") || name.equals("peony") || name.equals("sweet_berry_bush") || name.equals("bush")) {
            return Support.SOIL;
        }
        if (name.equals("ladder") || name.equals("tripwire_hook") || name.contains("wall_torch") || name.endsWith("_wall_sign")
                || name.endsWith("_wall_banner")) {
            return Support.WALL;
        }
        if (name.endsWith("torch") || tagged(block, "#minecraft:rails") || name.equals("redstone_wire") || name.equals("repeater")
                || name.equals("comparator") || name.endsWith("_pressure_plate") || name.endsWith("_door") || name.equals("snow")
                || name.endsWith("_sign") && !name.contains("hanging")) {
            return Support.FLOOR;
        }
        if (name.endsWith("_carpet") || name.equals("moss_carpet") || name.equals("flower_pot") || name.startsWith("potted_")) {
            return Support.ANY_FLOOR;
        }
        if (block.properties().containsKey("face") && block.properties().containsKey("powered")) {
            return Support.WALL;
        }
        return Support.NONE;
    }

    static boolean tagged(Block block, String tag) {
        RegistryTag<Block> registryTag = Block.staticRegistry().getTag(TagKey.ofHash(tag));
        return registryTag != null && registryTag.contains(block);
    }

    @Override
    public Block blockPlace(PlacementState state) {
        Block result = state.block();
        BlockFace clickedFace = state.blockFace();
        Pos player = state.playerPosition();
        if (clickedFace == null || player == null) {
            return result;
        }
        Block.Getter world = state.instance();
        Point position = state.placePosition();
        Facing face = Facing.of(clickedFace);
        Facing look = Facing.horizontal(player.yaw());
        double cursorY = state.cursorPosition() == null ? 0.5 : state.cursorPosition().y() - Math.floor(state.cursorPosition().y());

        if (wallVariant != null && face.horizontal()) {
            Block wall = wallVariant.withProperty("facing", face.id);
            if (wall.properties().containsKey("waterlogged")) {
                wall = wall.withProperty("waterlogged", Boolean.toString(isWaterSource(world.getBlock(position))));
            }
            return attached(world, position, wall) ? wall : null;
        }
        if (wallVariant != null && face == Facing.DOWN && support != Support.NONE) {
            return null;
        }

        if (facings != null && !attachFace) {
            result = result.withProperty("facing", facing(face, look, Facing.looking(player)).id);
        }
        if (axis) {
            result = result.withProperty("axis", face.axis());
        }
        if (stairHalf) {
            boolean top = face == Facing.DOWN || face.horizontal() && cursorY > 0.5;
            result = result.withProperty("half", top ? "top" : "bottom");
        }
        if (slab) {
            if (world.getBlock(position).compare(state.block())) {
                result = result.withProperty("type", "double").withProperty("waterlogged", "false");
                return result;
            }
            boolean top = face == Facing.DOWN || face.horizontal() && cursorY > 0.5;
            result = result.withProperty("type", top ? "top" : "bottom");
        }
        if (attachFace) {
            if (face == Facing.UP) {
                result = result.withProperty("face", "floor").withProperty("facing", look.id);
            } else if (face == Facing.DOWN) {
                result = result.withProperty("face", "ceiling").withProperty("facing", look.id);
            } else {
                result = result.withProperty("face", "wall").withProperty("facing", face.id);
            }
        }
        if (rotation) {
            int value = Math.floorMod((int) Math.floor((player.yaw() + 180) * 16 / 360 + 0.5), 16);
            result = result.withProperty("rotation", Integer.toString(value));
        }
        if (hanging) {
            result = result.withProperty("hanging", Boolean.toString(face == Facing.DOWN));
        }
        if (waterlogged) {
            result = result.withProperty("waterlogged", Boolean.toString(isWaterSource(world.getBlock(position))));
        }
        if (result.properties().containsKey("persistent")) {
            result = result.withProperty("persistent", "true");
        }
        if (chest && !state.isPlayerShifting()) {
            Facing facing = Facing.of(result.getProperty("facing"));
            if (single(world.getBlock(facing.clockwise().offset(position)), facing)) {
                result = result.withProperty("type", "left");
            } else if (single(world.getBlock(facing.counterClockwise().offset(position)), facing)) {
                result = result.withProperty("type", "right");
            }
        }
        result = neighbours(world, position, result);
        return supported(world, position, result) ? result : null;
    }

    private Facing facing(Facing face, Facing look, Facing looking) {
        if (name.endsWith("_trapdoor")) {
            return face.horizontal() ? face : look.opposite();
        }
        if (name.equals("ladder") || name.equals("tripwire_hook") || name.equals("cocoa")) {
            Facing attached = face.horizontal() ? face : look.opposite();
            return name.equals("cocoa") ? attached.opposite() : attached;
        }
        if (facings.contains("up")) {
            if (name.equals("hopper")) {
                return face.horizontal() ? face.opposite() : Facing.DOWN;
            }
            if (name.equals("observer")) {
                return looking;
            }
            if (name.equals("end_rod") || name.equals("lightning_rod") || name.contains("amethyst") || name.endsWith("shulker_box")
                    || name.endsWith("_rod")) {
                return face;
            }
            return facings.contains(looking.opposite().id) ? looking.opposite() : look.opposite();
        }
        if (stairs || name.endsWith("_door") || name.endsWith("_fence_gate") || name.endsWith("_bed")) {
            return look;
        }
        if (name.endsWith("anvil")) {
            return look.clockwise();
        }
        return look.opposite();
    }

    @Override
    public Block blockUpdate(UpdateState state) {
        Block.Getter world = state.instance();
        Point position = state.blockPosition();
        Block current = state.currentBlock();
        Block updated = neighbours(world, position, current);
        if (chest) {
            updated = chestUpdate(world, position, updated);
        }
        if (!supported(world, position, updated)) {
            if (!multiPart && world instanceof Instance instance) {
                BlockDropper.drop(instance, position, current);
            }
            return Block.AIR;
        }
        return updated;
    }

    @Override
    public boolean isSelfReplaceable(Replacement replacement) {
        if (!slab || replacement.material().block() == null || !replacement.material().block().compare(block)) {
            return false;
        }
        String type = replacement.block().getProperty("type");
        if (type.equals("double")) {
            return false;
        }
        if (replacement.isOffset()) {
            return true;
        }
        double cursorY = replacement.cursorPosition().y() - Math.floor(replacement.cursorPosition().y());
        Facing face = Facing.of(replacement.blockFace());
        return type.equals("bottom") ? face == Facing.UP || face.horizontal() && cursorY > 0.5
                : face == Facing.DOWN || face.horizontal() && cursorY < 0.5;
    }

    private Block neighbours(Block.Getter world, Point position, Block block) {
        if (connects || wallConnects) {
            boolean[] linked = new boolean[4];
            for (int i = 0; i < 4; i++) {
                Facing direction = Facing.HORIZONTAL[i];
                linked[i] = connectsTo(world.getBlock(direction.offset(position)), direction);
                block = block.withProperty(direction.id, wallConnects ? (linked[i] ? "low" : "none") : Boolean.toString(linked[i]));
            }
            if (wallConnects && block.properties().containsKey("up")) {
                boolean straight = linked[0] && linked[2] && !linked[1] && !linked[3] || linked[1] && linked[3] && !linked[0] && !linked[2];
                block = block.withProperty("up", Boolean.toString(!straight || !world.getBlock(position.add(0, 1, 0)).air()));
            }
        }
        if (stairs) {
            block = block.withProperty("shape", stairShape(world, position, block));
        }
        return block;
    }

    private boolean connectsTo(Block neighbour, Facing direction) {
        String other = neighbour.key().value();
        if (neighbour.air()) {
            return false;
        }
        boolean fullFace = neighbour.registry().collisionShape().isFaceFull(direction.opposite().face()) && !other.endsWith("leaves");
        if (name.endsWith("_fence")) {
            return fullFace || other.endsWith("_fence") && other.contains("nether") == name.contains("nether") || other.endsWith("_fence_gate");
        }
        if (wallConnects) {
            return fullFace || other.endsWith("_wall") || other.endsWith("_fence_gate") || other.endsWith("_pane") || other.equals("iron_bars");
        }
        return fullFace || other.endsWith("_pane") || other.equals("glass_pane") || other.equals("iron_bars") || other.endsWith("_wall");
    }

    private static String stairShape(Block.Getter world, Point position, Block block) {
        Facing facing = Facing.of(block.getProperty("facing"));
        String half = block.getProperty("half");
        Block back = world.getBlock(facing.offset(position));
        if (isStairs(back) && half.equals(back.getProperty("half"))) {
            Facing backFacing = Facing.of(back.getProperty("facing"));
            if (!backFacing.axis().equals(facing.axis()) && canTakeShape(world, position, block, backFacing.opposite())) {
                return backFacing == facing.counterClockwise() ? "outer_left" : "outer_right";
            }
        }
        Block front = world.getBlock(facing.opposite().offset(position));
        if (isStairs(front) && half.equals(front.getProperty("half"))) {
            Facing frontFacing = Facing.of(front.getProperty("facing"));
            if (!frontFacing.axis().equals(facing.axis()) && canTakeShape(world, position, block, frontFacing)) {
                return frontFacing == facing.counterClockwise() ? "inner_left" : "inner_right";
            }
        }
        return "straight";
    }

    private static boolean canTakeShape(Block.Getter world, Point position, Block block, Facing direction) {
        Block other = world.getBlock(direction.offset(position));
        return !isStairs(other) || !other.getProperty("facing").equals(block.getProperty("facing")) || !other.getProperty("half").equals(block.getProperty("half"));
    }

    private static boolean isStairs(Block block) {
        return block.key().value().endsWith("_stairs");
    }

    private boolean single(Block neighbour, Facing facing) {
        return neighbour.compare(block) && "single".equals(neighbour.getProperty("type")) && facing.id.equals(neighbour.getProperty("facing"));
    }

    private Block chestUpdate(Block.Getter world, Point position, Block current) {
        Facing facing = Facing.of(current.getProperty("facing"));
        String type = current.getProperty("type");
        if (type.equals("single")) {
            Block right = world.getBlock(facing.clockwise().offset(position));
            if (right.compare(block) && facing.id.equals(right.getProperty("facing")) && "right".equals(right.getProperty("type"))) {
                return current.withProperty("type", "left");
            }
            Block left = world.getBlock(facing.counterClockwise().offset(position));
            if (left.compare(block) && facing.id.equals(left.getProperty("facing")) && "left".equals(left.getProperty("type"))) {
                return current.withProperty("type", "right");
            }
            return current;
        }
        Facing partnerSide = type.equals("left") ? facing.clockwise() : facing.counterClockwise();
        Block partner = world.getBlock(partnerSide.offset(position));
        String expected = type.equals("left") ? "right" : "left";
        boolean paired = partner.compare(block) && facing.id.equals(partner.getProperty("facing")) && expected.equals(partner.getProperty("type"));
        return paired ? current : current.withProperty("type", "single");
    }

    private boolean supported(Block.Getter world, Point position, Block block) {
        String half = block.getProperty("half");
        if ("upper".equals(half)) {
            Block below = world.getBlock(position.add(0, -1, 0));
            return below.compare(block) && "lower".equals(below.getProperty("half"));
        }
        if (name.endsWith("_bed")) {
            Facing facing = Facing.of(block.getProperty("facing"));
            boolean head = "head".equals(block.getProperty("part"));
            Block other = world.getBlock((head ? facing.opposite() : facing).offset(position));
            return other.compare(block) && !block.getProperty("part").equals(other.getProperty("part"));
        }
        Block below = world.getBlock(position.add(0, -1, 0));
        return switch (support) {
            case NONE -> true;
            case FLOOR -> below.registry().collisionShape().isFaceFull(BlockFace.TOP) || below.key().value().endsWith("_slab") && !"bottom".equals(below.getProperty("type"));
            case ANY_FLOOR -> !below.air();
            case SOIL -> tagged(below, "#minecraft:supports_vegetation");
            case FARMLAND -> below.compare(Block.FARMLAND);
            case SAND_OR_SOIL -> tagged(below, "#minecraft:supports_dry_vegetation");
            case SUGAR_CANE -> below.compare(Block.SUGAR_CANE) || tagged(below, "#minecraft:supports_sugar_cane") && waterBeside(world, position.add(0, -1, 0));
            case CACTUS -> (below.compare(Block.CACTUS) || tagged(below, "#minecraft:supports_cactus")) && clearSides(world, position);
            case WALL -> attached(world, position, block);
        };
    }

    private static boolean attached(Block.Getter world, Point position, Block block) {
        Facing towards;
        String face = block.getProperty("face");
        if ("floor".equals(face)) {
            towards = Facing.DOWN;
        } else if ("ceiling".equals(face)) {
            towards = Facing.UP;
        } else {
            Facing facing = Facing.of(block.getProperty("facing"));
            if (facing == null) {
                return true;
            }
            towards = facing.opposite();
        }
        Block wall = world.getBlock(towards.offset(position));
        return wall.registry().collisionShape().isFaceFull(towards.opposite().face());
    }

    private static boolean waterBeside(Block.Getter world, Point position) {
        for (Facing direction : Facing.HORIZONTAL) {
            Block side = world.getBlock(direction.offset(position));
            if (side.compare(Block.WATER) || side.compare(Block.FROSTED_ICE) || "true".equals(side.getProperty("waterlogged"))) {
                return true;
            }
        }
        return false;
    }

    private static boolean clearSides(Block.Getter world, Point position) {
        for (Facing direction : Facing.HORIZONTAL) {
            Block side = world.getBlock(direction.offset(position));
            if (side.solid() || side.liquid()) {
                return false;
            }
        }
        return true;
    }

    private static boolean isWaterSource(Block block) {
        return block.compare(Block.WATER) && "0".equals(block.getProperty("level"));
    }
}

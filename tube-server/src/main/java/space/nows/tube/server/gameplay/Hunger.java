package space.nows.tube.server.gameplay;

import net.minestom.server.MinecraftServer;
import net.minestom.server.component.DataComponents;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.item.PlayerBeginItemUseEvent;
import net.minestom.server.event.item.PlayerFinishItemUseEvent;
import net.minestom.server.event.player.PlayerMoveEvent;
import net.minestom.server.tag.Tag;
import net.minestom.server.timer.TaskSchedule;
import net.minestom.server.world.Difficulty;

final class Hunger {

    private static final Tag<State> STATE = Tag.Transient("tube:hunger");
    private static final int MAX_FOOD = 20;

    static final class State {
        float exhaustion;
        int timer;
        boolean wasOnGround = true;
    }

    private Hunger() {
    }

    static void install(GlobalEventHandler handler) {
        handler.addListener(PlayerMoveEvent.class, event -> {
            Player player = event.getPlayer();
            if (!Falling.affected(player)) {
                return;
            }
            State state = state(player);
            var from = player.getPosition();
            var to = event.getNewPosition();
            if (player.isSprinting()) {
                state.exhaustion += (float) (0.1 * Math.hypot(to.x() - from.x(), to.z() - from.z()));
            }
            if (state.wasOnGround && !event.isOnGround() && to.y() > from.y()) {
                state.exhaustion += player.isSprinting() ? 0.2f : 0.05f;
            }
            state.wasOnGround = event.isOnGround();
        });
        handler.addListener(PlayerBeginItemUseEvent.class, event -> {
            var food = event.getItemStack().get(DataComponents.FOOD);
            if (food != null && !food.canAlwaysEat() && event.getPlayer().getFood() >= MAX_FOOD && Falling.affected(event.getPlayer())) {
                event.setCancelled(true);
            }
        });
        handler.addListener(PlayerFinishItemUseEvent.class, Hunger::eat);
        MinecraftServer.getSchedulerManager().buildTask(Hunger::tickAll).repeat(TaskSchedule.tick(1)).schedule();
    }

    static void exhaust(Player player, float amount) {
        if (Falling.affected(player)) {
            state(player).exhaustion += amount;
        }
    }

    static State state(Player player) {
        State state = player.getTag(STATE);
        if (state == null) {
            state = new State();
            player.setTag(STATE, state);
        }
        return state;
    }

    private static void eat(PlayerFinishItemUseEvent event) {
        Player player = event.getPlayer();
        var item = event.getItemStack();
        var food = item.get(DataComponents.FOOD);
        if (food == null) {
            return;
        }
        int level = Math.min(MAX_FOOD, player.getFood() + food.nutrition());
        player.setFood(level);
        player.setFoodSaturation(Math.min(level, player.getFoodSaturation() + food.nutrition() * food.saturationModifier() * 2));
        if (player.getGameMode() != GameMode.CREATIVE) {
            player.setItemInHand(event.getHand(), item.withAmount(item.amount() - 1));
        }
    }

    private static void tickAll() {
        Difficulty difficulty = MinecraftServer.getDifficulty();
        for (Player player : MinecraftServer.getConnectionManager().getOnlinePlayers()) {
            if (Falling.affected(player) && !player.isDead()) {
                tick(player, state(player), difficulty);
            }
        }
    }

    static void tick(Player player, State state, Difficulty difficulty) {
        double maxHealth = player.getAttributeValue(Attribute.MAX_HEALTH);
        state.timer++;
        if (difficulty == Difficulty.PEACEFUL) {
            if (state.timer % 20 == 0) {
                if (player.getHealth() < maxHealth) {
                    player.setHealth((float) Math.min(maxHealth, player.getHealth() + 1));
                }
                if (player.getFood() < MAX_FOOD) {
                    player.setFood(player.getFood() + 1);
                }
            }
            return;
        }
        while (state.exhaustion >= 4) {
            state.exhaustion -= 4;
            if (player.getFoodSaturation() > 0) {
                player.setFoodSaturation(Math.max(0, player.getFoodSaturation() - 1));
            } else {
                player.setFood(Math.max(0, player.getFood() - 1));
            }
        }
        int food = player.getFood();
        if (player.getHealth() < maxHealth && food >= MAX_FOOD && player.getFoodSaturation() > 0) {
            if (state.timer >= 10) {
                float amount = Math.min(player.getFoodSaturation(), 6);
                player.setHealth((float) Math.min(maxHealth, player.getHealth() + amount / 6));
                state.exhaustion += amount;
                state.timer = 0;
            }
        } else if (player.getHealth() < maxHealth && food >= 18) {
            if (state.timer >= 80) {
                player.setHealth((float) Math.min(maxHealth, player.getHealth() + 1));
                state.exhaustion += 6;
                state.timer = 0;
            }
        } else if (food <= 0) {
            if (state.timer >= 80) {
                float health = player.getHealth();
                if (health > 10 || difficulty == Difficulty.HARD || health > 1 && difficulty == Difficulty.NORMAL) {
                    player.damage(DamageType.STARVE, 1);
                }
                state.timer = 0;
            }
        } else {
            state.timer = 0;
        }
    }
}

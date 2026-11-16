package space.nows.tube.bukkit.world;

import org.bukkit.Location;
import org.bukkit.WorldBorder;

final class BukkitWorldBorder implements WorldBorder {

    private final BukkitWorld world;
    private double damageBuffer = 5;
    private double damageAmount = 0.2;

    BukkitWorldBorder(BukkitWorld world) {
        this.world = world;
    }

    private space.nows.tube.api.world.WorldBorder border() {
        return world.tube().border();
    }

    @Override
    public void reset() {
        world.tube().setBorder(space.nows.tube.api.world.WorldBorder.DEFAULT);
    }

    @Override
    public double getSize() {
        return border().diameter();
    }

    @Override
    public void setSize(double size) {
        world.tube().setBorder(border().withDiameter(size));
    }

    @Override
    public void setSize(double size, long seconds) {
        world.tube().setBorder(border().withDiameter(size), seconds);
    }

    @Override
    public Location getCenter() {
        return new Location(world, border().centerX(), 0, border().centerZ());
    }

    @Override
    public void setCenter(double x, double z) {
        world.tube().setBorder(border().withCenter(x, z));
    }

    @Override
    public double getDamageBuffer() {
        return damageBuffer;
    }

    @Override
    public void setDamageBuffer(double blocks) {
        this.damageBuffer = blocks;
    }

    @Override
    public double getDamageAmount() {
        return damageAmount;
    }

    @Override
    public void setDamageAmount(double damage) {
        this.damageAmount = damage;
    }

    @Override
    public int getWarningTime() {
        return border().warningTime();
    }

    @Override
    public void setWarningTime(int seconds) {
        world.tube().setBorder(border().withWarningTime(seconds));
    }

    @Override
    public int getWarningDistance() {
        return border().warningDistance();
    }

    @Override
    public void setWarningDistance(int distance) {
        world.tube().setBorder(border().withWarningDistance(distance));
    }

    @Override
    public boolean isInside(Location location) {
        return location.getWorld() != null && location.getWorld().equals(world) && border().contains(location.getX(), location.getZ());
    }
}

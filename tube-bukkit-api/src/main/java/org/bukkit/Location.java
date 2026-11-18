package org.bukkit;

import org.bukkit.block.Block;
import org.bukkit.util.Vector;

import java.util.Objects;

public class Location implements Cloneable {

    private World world;
    private double x;
    private double y;
    private double z;
    private float yaw;
    private float pitch;

    public Location(World world, double x, double y, double z) {
        this(world, x, y, z, 0f, 0f);
    }

    public Location(World world, double x, double y, double z, float yaw, float pitch) {
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public World getWorld() {
        return world;
    }

    public void setWorld(World world) {
        this.world = world;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public void setX(double x) {
        this.x = x;
    }

    public void setY(double y) {
        this.y = y;
    }

    public void setZ(double z) {
        this.z = z;
    }

    public float getYaw() {
        return yaw;
    }

    public float getPitch() {
        return pitch;
    }

    public void setYaw(float yaw) {
        this.yaw = yaw;
    }

    public void setPitch(float pitch) {
        this.pitch = pitch;
    }

    public int getBlockX() {
        return (int) Math.floor(x);
    }

    public int getBlockY() {
        return (int) Math.floor(y);
    }

    public int getBlockZ() {
        return (int) Math.floor(z);
    }

    public Block getBlock() {
        return world.getBlockAt(this);
    }

    public Vector toVector() {
        return new Vector(x, y, z);
    }

    public Vector getDirection() {
        double rotX = Math.toRadians(yaw);
        double rotY = Math.toRadians(pitch);
        double xz = Math.cos(rotY);
        return new Vector(-xz * Math.sin(rotX), -Math.sin(rotY), xz * Math.cos(rotX));
    }

    public Location add(Vector vector) {
        return add(vector.getX(), vector.getY(), vector.getZ());
    }

    public Location add(double dx, double dy, double dz) {
        x += dx;
        y += dy;
        z += dz;
        return this;
    }

    public Location subtract(double dx, double dy, double dz) {
        return add(-dx, -dy, -dz);
    }

    public Location subtract(Location other) {
        return subtract(other.x, other.y, other.z);
    }

    public double distanceSquared(Location other) {
        double dx = x - other.x;
        double dy = y - other.y;
        double dz = z - other.z;
        return dx * dx + dy * dy + dz * dz;
    }

    public double distance(Location other) {
        return Math.sqrt(distanceSquared(other));
    }

    @Override
    public Location clone() {
        try {
            return (Location) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(e);
        }
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof Location that && Objects.equals(world, that.world)
                && Double.compare(x, that.x) == 0 && Double.compare(y, that.y) == 0 && Double.compare(z, that.z) == 0
                && Float.compare(yaw, that.yaw) == 0 && Float.compare(pitch, that.pitch) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(world, x, y, z, yaw, pitch);
    }

    @Override
    public String toString() {
        return "Location{world=" + world + ",x=" + x + ",y=" + y + ",z=" + z + ",pitch=" + pitch + ",yaw=" + yaw + "}";
    }
}

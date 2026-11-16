package org.bukkit.util;

import org.bukkit.Location;
import org.bukkit.World;

public class Vector implements Cloneable {

    protected double x;
    protected double y;
    protected double z;

    public Vector() {
    }

    public Vector(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vector(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vector(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
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

    public int getBlockX() {
        return (int) Math.floor(x);
    }

    public int getBlockY() {
        return (int) Math.floor(y);
    }

    public int getBlockZ() {
        return (int) Math.floor(z);
    }

    public Vector setX(double x) {
        this.x = x;
        return this;
    }

    public Vector setY(double y) {
        this.y = y;
        return this;
    }

    public Vector setZ(double z) {
        this.z = z;
        return this;
    }

    public Vector add(Vector other) {
        x += other.x;
        y += other.y;
        z += other.z;
        return this;
    }

    public Vector subtract(Vector other) {
        x -= other.x;
        y -= other.y;
        z -= other.z;
        return this;
    }

    public Vector multiply(int factor) {
        return multiply((double) factor);
    }

    public Vector multiply(double factor) {
        x *= factor;
        y *= factor;
        z *= factor;
        return this;
    }

    public double dot(Vector other) {
        return x * other.x + y * other.y + z * other.z;
    }

    public Vector crossProduct(Vector other) {
        double newX = y * other.z - other.y * z;
        double newY = z * other.x - other.z * x;
        double newZ = x * other.y - other.x * y;
        x = newX;
        y = newY;
        z = newZ;
        return this;
    }

    public double length() {
        return Math.sqrt(x * x + y * y + z * z);
    }

    public double distance(Vector other) {
        return Math.sqrt(distanceSquared(other));
    }

    public double distanceSquared(Vector other) {
        double dx = x - other.x;
        double dy = y - other.y;
        double dz = z - other.z;
        return dx * dx + dy * dy + dz * dz;
    }

    public Vector rotateAroundY(double angle) {
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        double newX = cos * x + sin * z;
        double newZ = -sin * x + cos * z;
        x = newX;
        z = newZ;
        return this;
    }

    public Location toLocation(World world) {
        return new Location(world, x, y, z);
    }

    @Override
    public Vector clone() {
        try {
            return (Vector) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(e);
        }
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof Vector that && Double.compare(x, that.x) == 0 && Double.compare(y, that.y) == 0 && Double.compare(z, that.z) == 0;
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(x, y, z);
    }

    @Override
    public String toString() {
        return x + "," + y + "," + z;
    }
}

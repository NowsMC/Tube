package org.bukkit.profile;

public interface PlayerProfile extends java.lang.Cloneable, org.bukkit.configuration.serialization.ConfigurationSerializable {

    org.bukkit.profile.PlayerTextures getTextures();

    java.util.concurrent.CompletableFuture<org.bukkit.profile.PlayerProfile> update();
}

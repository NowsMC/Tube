package org.bukkit.profile;

public interface PlayerTextures {

    java.net.URL getSkin();

    void setSkin(java.net.URL p0);

    public enum SkinModel {
        CLASSIC,
        SLIM
    }
}

package org.bukkit.advancement;

public interface AdvancementProgress {

    boolean awardCriteria(java.lang.String p0);

    boolean revokeCriteria(java.lang.String p0);

    java.util.Collection<java.lang.String> getAwardedCriteria();
}

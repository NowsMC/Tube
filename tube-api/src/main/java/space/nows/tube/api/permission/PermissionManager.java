package space.nows.tube.api.permission;

import java.util.Map;
import java.util.UUID;

public interface PermissionManager {

    boolean isOp(UUID uuid);

    void setOp(UUID uuid, String name, boolean op);

    boolean has(UUID uuid, String node);

    void grant(UUID uuid, String node);

    void revoke(UUID uuid, String node);

    void define(String node, PermissionDefault fallback, Map<String, Boolean> children);

    void undefine(String node);
}

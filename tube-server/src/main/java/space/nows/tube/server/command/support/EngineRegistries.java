package space.nows.tube.server.command.support;

import space.nows.tube.api.registry.Registries;

import java.util.List;

public final class EngineRegistries implements Registries {

    @Override
    public List<String> items() {
        return RegistryNames.items();
    }

    @Override
    public List<String> blocks() {
        return RegistryNames.blocks();
    }

    @Override
    public List<String> entities() {
        return RegistryNames.entities();
    }

    @Override
    public List<String> effects() {
        return RegistryNames.effects();
    }

    @Override
    public List<String> sounds() {
        return RegistryNames.sounds();
    }

    @Override
    public List<String> particles() {
        return RegistryNames.particles();
    }
}

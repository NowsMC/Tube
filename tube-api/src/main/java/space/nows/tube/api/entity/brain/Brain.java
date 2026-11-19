package space.nows.tube.api.entity.brain;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public interface Brain {

    void add(int priority, Behavior behavior);

    void add(Activity activity, int priority, Behavior behavior);

    void addActivity(Activity activity, Predicate<Brain> when);

    Activity activity();

    void remove(Behavior behavior);

    boolean running(Behavior behavior);

    List<Behavior> behaviors();

    void setEnabled(Control control, boolean enabled);

    <T> Optional<T> recall(MemoryKey<T> key);

    <T> void remember(MemoryKey<T> key, T value);

    <T> void remember(MemoryKey<T> key, T value, int ticks);

    void forget(MemoryKey<?> key);

    void addSense(Sense sense);
}

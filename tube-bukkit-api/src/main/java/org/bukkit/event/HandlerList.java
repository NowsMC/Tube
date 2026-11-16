package org.bukkit.event;

import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredListener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

public class HandlerList {

    private static final List<HandlerList> ALL_LISTS = new ArrayList<>();

    private volatile RegisteredListener[] handlers;
    private final EnumMap<EventPriority, ArrayList<RegisteredListener>> slots = new EnumMap<>(EventPriority.class);

    public HandlerList() {
        for (EventPriority priority : EventPriority.values()) {
            slots.put(priority, new ArrayList<>());
        }
        synchronized (ALL_LISTS) {
            ALL_LISTS.add(this);
        }
    }

    public static void bakeAll() {
        synchronized (ALL_LISTS) {
            for (HandlerList list : ALL_LISTS) {
                list.bake();
            }
        }
    }

    public static void unregisterAll() {
        synchronized (ALL_LISTS) {
            for (HandlerList list : ALL_LISTS) {
                synchronized (list) {
                    list.slots.values().forEach(ArrayList::clear);
                    list.handlers = null;
                }
            }
        }
    }

    public static void unregisterAll(Plugin plugin) {
        synchronized (ALL_LISTS) {
            ALL_LISTS.forEach(list -> list.unregister(plugin));
        }
    }

    public static void unregisterAll(Listener listener) {
        synchronized (ALL_LISTS) {
            ALL_LISTS.forEach(list -> list.unregister(listener));
        }
    }

    public static ArrayList<HandlerList> getHandlerLists() {
        synchronized (ALL_LISTS) {
            return new ArrayList<>(ALL_LISTS);
        }
    }

    public synchronized void register(RegisteredListener listener) {
        if (slots.get(listener.getPriority()).contains(listener)) {
            throw new IllegalStateException("This listener is already registered to priority " + listener.getPriority());
        }
        handlers = null;
        slots.get(listener.getPriority()).add(listener);
    }

    public void registerAll(Collection<RegisteredListener> listeners) {
        listeners.forEach(this::register);
    }

    public synchronized void unregister(RegisteredListener listener) {
        if (slots.get(listener.getPriority()).remove(listener)) {
            handlers = null;
        }
    }

    public synchronized void unregister(Plugin plugin) {
        boolean changed = false;
        for (List<RegisteredListener> list : slots.values()) {
            for (ListIterator<RegisteredListener> iterator = list.listIterator(); iterator.hasNext(); ) {
                if (iterator.next().getPlugin().equals(plugin)) {
                    iterator.remove();
                    changed = true;
                }
            }
        }
        if (changed) {
            handlers = null;
        }
    }

    public synchronized void unregister(Listener listener) {
        boolean changed = false;
        for (List<RegisteredListener> list : slots.values()) {
            for (ListIterator<RegisteredListener> iterator = list.listIterator(); iterator.hasNext(); ) {
                if (iterator.next().getListener().equals(listener)) {
                    iterator.remove();
                    changed = true;
                }
            }
        }
        if (changed) {
            handlers = null;
        }
    }

    public synchronized void bake() {
        if (handlers != null) {
            return;
        }
        List<RegisteredListener> entries = new ArrayList<>();
        for (Map.Entry<EventPriority, ArrayList<RegisteredListener>> entry : slots.entrySet()) {
            entries.addAll(entry.getValue());
        }
        handlers = entries.toArray(new RegisteredListener[0]);
    }

    public RegisteredListener[] getRegisteredListeners() {
        RegisteredListener[] baked = handlers;
        if (baked == null) {
            bake();
            baked = handlers;
        }
        return baked;
    }
}

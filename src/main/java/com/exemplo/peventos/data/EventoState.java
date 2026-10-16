package com.exemplo.peventos.data;

import com.exemplo.peventos.PEventos;
import com.exemplo.peventos.model.EventoOption;

import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.EnumMap;
import java.util.Map;
import java.util.logging.Level;

public final class EventoState {

    private final PEventos plugin;
    private final File file;
    private final YamlConfiguration data;
    private final Map<EventoOption, Boolean> values = new EnumMap<>(EventoOption.class);

    public EventoState(PEventos plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "data.yml");
        this.data = YamlConfiguration.loadConfiguration(file);
        for (EventoOption option : EventoOption.values()) {
            values.put(option, data.getBoolean("estado." + option.getKey(), option.getDefaultValue()));
        }
    }

    public boolean isEnabled(EventoOption option) {
        return values.get(option);
    }

    public boolean toggle(EventoOption option) {
        boolean newValue = !isEnabled(option);
        values.put(option, newValue);
        data.set("estado." + option.getKey(), newValue);
        save();
        return newValue;
    }

    public void resetAll() {
        for (EventoOption option : EventoOption.values()) {
            values.put(option, option.getDefaultValue());
            data.set("estado." + option.getKey(), option.getDefaultValue());
        }
        save();
    }

    public Location getSpawn() {
        Location location = data.getLocation("spawn");
        return (location != null && location.getWorld() != null) ? location : null;
    }

    public void setSpawn(Location location) {
        data.set("spawn", location);
        save();
    }

    private void save() {
        try {
            plugin.getDataFolder().mkdirs();
            data.save(file);
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Não foi possível salvar o data.yml", e);
        }
    }
}

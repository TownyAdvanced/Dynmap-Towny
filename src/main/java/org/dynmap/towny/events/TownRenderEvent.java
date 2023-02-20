package org.dynmap.towny.events;

import com.palmergames.bukkit.towny.object.Town;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.dynmap.markers.AreaMarker;
import org.jetbrains.annotations.NotNull;

/**
 * Called when Dynmap-Towny has made a town which will be rendered.
 */
public class TownRenderEvent extends Event {
    private static HandlerList handlers = new HandlerList();
    private final Town town;
    private final AreaMarker areaMarker;

    public TownRenderEvent(Town town, AreaMarker areaMarker) {
        super(!Bukkit.getServer().isPrimaryThread());
        this.town = town;
        this.areaMarker = areaMarker;
    }

    public Town getTown() {
        return town;
    }

    public AreaMarker getAreaMarker() {
        return areaMarker;
    }

    @NotNull
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}

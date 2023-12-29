package org.dynmap.towny.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.PluginManager;
import org.dynmap.DynmapWebChatEvent;
import org.dynmap.towny.DynmapTownyPlugin;
import org.dynmap.towny.settings.Settings;

public class DynmapTownyListener implements Listener {
	final DynmapTownyPlugin plugin;
	public DynmapTownyListener(PluginManager pm, DynmapTownyPlugin plugin) {
		this.plugin = plugin;
		pm.registerEvents(this, plugin);
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerLogin(PlayerLoginEvent event) {
		if (!Settings.sendLoginMessage() || !Settings.usingTownyChat() || event.getResult() != PlayerLoginEvent.Result.ALLOWED)
			return;

		plugin.getDynmapAPI().postPlayerJoinQuitToWeb(event.getPlayer(), true);
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerQuit(PlayerQuitEvent event) {
		if (!Settings.sendQuitMessage() || !Settings.usingTownyChat())
			return;

		plugin.getDynmapAPI().postPlayerJoinQuitToWeb(event.getPlayer(), false);
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onWebchatEvent(DynmapWebChatEvent event) {
		if (!Settings.usingTownyChat() || Settings.getChatFormat().isEmpty() || event.isCancelled() || event.isProcessed())
			return;

		event.setProcessed();
		String msg = Settings.getChatFormat()
				.replace("&color;", "\u00A7")
				.replace("%playername%", event.getName())
				.replace("%message%", event.getMessage());
		plugin.getServer().broadcastMessage(msg);
	}

	@EventHandler(priority = EventPriority.NORMAL)
	private void onDynMapReload(PluginEnableEvent event) {
		if (!event.getPlugin().getName().equals("dynmap"))
			return;

		PluginManager pm = plugin.getServer().getPluginManager();
		if (pm.isPluginEnabled("Dynmap-Towny")) {
			pm.disablePlugin(plugin);
			pm.enablePlugin(plugin);
		}
	}
}

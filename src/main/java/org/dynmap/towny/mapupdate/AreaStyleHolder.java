package org.dynmap.towny.mapupdate;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.dynmap.towny.DynmapTownyPlugin;

public class AreaStyleHolder {

	private static AreaStyle defstyle = new AreaStyle();
	private static Map<String, AreaStyle> cusstyle = new HashMap<String, AreaStyle>();
	private static Map<String, AreaStyle> nationstyle = new HashMap<String, AreaStyle>();

	public static void initialize() {
		DynmapTownyPlugin plugin = DynmapTownyPlugin.getPlugin();
		FileConfiguration cfg = plugin.getConfig();
		ConfigurationSection sect = cfg.getConfigurationSection("custstyle");
		if (sect != null) {
			Set<String> ids = sect.getKeys(false);

			for (String id : ids) {
				cusstyle.put(id, new AreaStyle("custstyle." + id, plugin.getDynmapAPI().getMarkerAPI()));
			}
		}
		sect = cfg.getConfigurationSection("nationstyle");
		if (sect != null) {
			Set<String> ids = sect.getKeys(false);

			for (String id : ids) {
				nationstyle.put(id, new AreaStyle("nationstyle." + id, plugin.getDynmapAPI().getMarkerAPI()));
			}
		}
	}

	public static AreaStyle getDefaultStyle() {
		return defstyle;
	}

	public static Map<String, AreaStyle> getCustomStyles() {
		return cusstyle;
	}

	public static Map<String, AreaStyle> getNationStyles() {
		return nationstyle;
	}
	
}

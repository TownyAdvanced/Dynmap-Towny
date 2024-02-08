package org.dynmap.towny.settings;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.bukkit.configuration.ConfigurationSection;
import org.dynmap.markers.MarkerIcon;
import org.dynmap.towny.DynmapTownyPlugin;

import com.palmergames.bukkit.config.CommentedConfiguration;
import com.palmergames.bukkit.towny.exceptions.initialization.TownyInitException;
import com.palmergames.util.FileMgmt;

public class Settings {
	private static final String NATIONSTYLE_ROOT = "nationstyle";
	private static final String CUSTSTYLE_ROOT = "custstyle";
	private static CommentedConfiguration config, newConfig;
	private static Path configPath = DynmapTownyPlugin.getPlugin().getDataFolder().toPath().resolve("config.yml");
	private static boolean usingTownyChat;
	private static MarkerIcon outpostIcon = null;
	private static MarkerIcon homeIcon = null;
	private static MarkerIcon capitalIcon = null;
	private static MarkerIcon ruinIcon = null;
	private static boolean visByTownSet;
	private static boolean visByNationSet;
	private static boolean visByNation;
	private static boolean visByTown;
	private static Set<String> visibleRegions = null;
	private static Set<String> hiddenRegions = null;

	public static void loadConfig() {
		if (FileMgmt.checkOrCreateFile(configPath.toString())) {

			// read the config.yml into memory
			config = new CommentedConfiguration(configPath);
			if (!config.load())
				throw new TownyInitException("Failed to load config.yml.", TownyInitException.TownyError.MAIN_CONFIG);

			setDefaults(DynmapTownyPlugin.getPlugin().getVersion(), configPath);
			config.save();
		}
	}

	public static void addComment(String root, String... comments) {

		newConfig.addComment(root.toLowerCase(), comments);
	}

	private static void setNewProperty(String root, Object value) {

		if (value == null) {
			value = "";
		}
		newConfig.set(root.toLowerCase(), value.toString());
	}

	@SuppressWarnings("unused")
	private static void setProperty(String root, Object value) {

		config.set(root.toLowerCase(), value.toString());
	}
	
	/**
	 * Builds a new config reading old config data.
	 */
	private static void setDefaults(String version, Path configPath) {

		newConfig = new CommentedConfiguration(configPath);
		newConfig.load();

		for (ConfigNodes root : ConfigNodes.values()) {
			if (root.getComments().length > 0)
				addComment(root.getRoot(), root.getComments());
			if (root.getRoot() == ConfigNodes.VERSION.getRoot())
				setNewProperty(root.getRoot(), version);
			else
				setNewProperty(root.getRoot(), (config.get(root.getRoot().toLowerCase()) != null) ? config.get(root.getRoot().toLowerCase()) : root.getDefault());
		}

		trySetDefaultCustomAndNationStyles();
		config = newConfig;
		newConfig = null;
	}
	
	private static void trySetDefaultCustomAndNationStyles() {
		if(config.contains(CUSTSTYLE_ROOT)) // Custstyle already exists.
			newConfig.set(CUSTSTYLE_ROOT, config.get(CUSTSTYLE_ROOT));
		else { // Make a new custstyle.
			DynmapTownyPlugin.getPlugin().getLogger().info("Config: Creating default custom styles.");

			newConfig.createSection(CUSTSTYLE_ROOT);
			ConfigurationSection configurationSection = newConfig.getConfigurationSection(CUSTSTYLE_ROOT);
			configurationSection.set(".customregion1.strokecolor", "#00FF00");
			configurationSection.set(".customregion1.y", "64");
			configurationSection.set(".customregion2.strokecolor", "#007F00");
			configurationSection.set(".customregion2.y", "64");
			configurationSection.set(".customregion2.boost", "false");
		}

		if(config.contains(NATIONSTYLE_ROOT)) // Custstyle already exists.
			newConfig.set(NATIONSTYLE_ROOT, config.get(NATIONSTYLE_ROOT));
		else { // Make a new nationstyle.
			DynmapTownyPlugin.getPlugin().getLogger().info("Config: Creating default nation styles.");

			newConfig.createSection(NATIONSTYLE_ROOT);
			ConfigurationSection configurationSection = newConfig.getConfigurationSection(NATIONSTYLE_ROOT);
			configurationSection.set(".NationOfBlue.strokecolor", "#0000FF");
			configurationSection.set(".NationOfBlue.fillcolor", "#0000FF");
			configurationSection.set(".NationOfBlue.boost", "false");
			configurationSection.set("._none_.homeicon", "greenflag");
		}
	}

	public static String getString(String root, String def) {

		String data = config.getString(root.toLowerCase(), def);
		if (data == null) {
			sendError(root.toLowerCase() + " from config.yml");
			return "";
		}
		return data;
	}

	private static void sendError(String msg) {

		DynmapTownyPlugin.severe("Error could not read " + msg);
	}
	
	public static boolean getBoolean(ConfigNodes node) {

		return Boolean.parseBoolean(config.getString(node.getRoot().toLowerCase(), node.getDefault()));
	}

	public static double getDouble(ConfigNodes node) {

		try {
			return Double.parseDouble(config.getString(node.getRoot().toLowerCase(), node.getDefault()).trim());
		} catch (NumberFormatException e) {
			sendError(node.getRoot().toLowerCase() + " from config.yml");
			return 0.0;
		}
	}

	public static int getInt(ConfigNodes node) {

		try {
			return Integer.parseInt(config.getString(node.getRoot().toLowerCase(), node.getDefault()).trim());
		} catch (NumberFormatException e) {
			sendError(node.getRoot().toLowerCase() + " from config.yml");
			return 0;
		}
	}

	public static String getString(ConfigNodes node) {

		return config.getString(node.getRoot().toLowerCase(), node.getDefault());
	}

	public static List<String> getStrArr(ConfigNodes node) {

		String[] strArray = getString(node.getRoot().toLowerCase(Locale.ROOT), node.getDefault()).split(",");
		List<String> list = new ArrayList<>();
		
		for (String string : strArray)
			if (string != null && !string.isEmpty())
				list.add(string.trim());
		
		return list;
	}

	public static void setUsingTownyChat(boolean value) {
		usingTownyChat = value;
	}

	public static boolean usingTownyChat() {
		return usingTownyChat;
	}

	public static boolean sendLoginMessage() {
		return getBoolean(ConfigNodes.CHAT_SEND_LOGIN);
	}

	public static boolean sendQuitMessage() {
		return getBoolean(ConfigNodes.CHAT_SEND_QUIT);
	}

	public static String getChatFormat() {
		return getString(ConfigNodes.CHAT_FORMAT);
	}

	public static String getTownInfoWindow() {
		return getString(ConfigNodes.INFOWINDOW_TOWN_POPUP);
	}

	public static String noNationSlug() {
		return getString(ConfigNodes.INFOWINDOW_NO_NATION_SLUG);
	}

	public static MarkerIcon getOutpostIcon() {
		if (outpostIcon == null)
			outpostIcon = DynmapTownyPlugin.getPlugin().getDynmapAPI().getMarkerAPI().getMarkerIcon(getString(ConfigNodes.REGIONSTYLE_OUTPOST_ICON));
		return outpostIcon;
	}

	public static MarkerIcon getHomeIcon() {
		if (homeIcon == null)
			homeIcon = DynmapTownyPlugin.getPlugin().getDynmapAPI().getMarkerAPI().getMarkerIcon(getString(ConfigNodes.REGIONSTYLE_HOME_ICON));
		return homeIcon;
	}

	public static MarkerIcon getCapitalIcon() {
		if (capitalIcon == null)
			capitalIcon = DynmapTownyPlugin.getPlugin().getDynmapAPI().getMarkerAPI().getMarkerIcon(getString(ConfigNodes.REGIONSTYLE_CAPITAL_ICON));
		return capitalIcon;
	}

	public static MarkerIcon getRuinIcon() {
		if (ruinIcon == null)
			ruinIcon = DynmapTownyPlugin.getPlugin().getDynmapAPI().getMarkerAPI().getMarkerIcon(getString(ConfigNodes.REGIONSTYLE_RUIN_ICON));
		return ruinIcon;
	}

	public static String getStrokeColour() {
		return getString(ConfigNodes.REGIONSTYLE_STROKE_COLOUR);
	}

	public static double getStrokeOpacity() {
		return getDouble(ConfigNodes.REGIONSTYLE_STROKE_OPACITY);
	}

	public static int getStrokeWeight() {
		return getInt(ConfigNodes.REGIONSTYLE_STROKE_WEIGHT);
	}

	public static String getFillColour() {
		return getString(ConfigNodes.REGIONSTYLE_FILL_COLOUR);
	}

	public static String getShopsFillColour() {
		return getString(ConfigNodes.TOWNBLOCK_SHOP_FILL_COLOUR);
	}

	public static String getArenaFillColour() {
		return getString(ConfigNodes.TOWNBLOCK_SHOP_FILL_COLOUR);
	}

	public static String getEmbassyFillColour() {
		return getString(ConfigNodes.TOWNBLOCK_SHOP_FILL_COLOUR);
	}

	public static String getWildsFillColour() {
		return getString(ConfigNodes.TOWNBLOCK_SHOP_FILL_COLOUR);
	}

	public static double getFillOpacity() {
		return getDouble(ConfigNodes.REGIONSTYLE_FILL_OPACITY);
	}

	
	public static boolean getBoost() {
		return getBoolean(ConfigNodes.REGIONSTYLE_BOOST);
	}

	public static String getLayerName() {
		return getString(ConfigNodes.LAYER_NAME);
	}

	public static int getMinZoom() {
		return getInt(ConfigNodes.LAYER_MIN_ZOOM);
	}

	public static int getLayerPriority() {
		return getInt(ConfigNodes.LAYER_PRIORITY);
	}

	public static boolean getLayerHiddenByDefault() {
		return getBoolean(ConfigNodes.LAYER_HIDE_BY_DEFAULT);
	}

	public static boolean getPlayerVisibilityByTown() {
		if (visByTownSet)
			return visByTown;

		visByTownSet = true;
		if (getBoolean(ConfigNodes.VISIBILITY_BY_TOWN)) {
			try {
				if (!DynmapTownyPlugin.getPlugin().getDynmapAPI().testIfPlayerInfoProtected()) {
					visByTown = false;
					DynmapTownyPlugin.getPlugin().getLogger().info("Dynmap does not have player-info-protected enabled - visibility-by-nation will have no effect");
					return visByTown;
				}
			} catch (NoSuchMethodError x) {
				visByTown = false;
				DynmapTownyPlugin.getPlugin().getLogger().info("Dynmap does not support function needed for 'visibility-by-nation' - need to upgrade to 0.60 or later");
				return visByTown;
			}
			visByTown = true;
		}
		return visByTown;
	}

	public static boolean getPlayerVisibilityByNation() {
		if (visByNationSet)
			return visByNation;

		visByNationSet = true;
		if (getBoolean(ConfigNodes.VISIBILITY_BY_NATION)) {
			try {
				if (!DynmapTownyPlugin.getPlugin().getDynmapAPI().testIfPlayerInfoProtected()) {
					visByNation = false;
					DynmapTownyPlugin.getPlugin().getLogger().info("Dynmap does not have player-info-protected enabled - visibility-by-nation will have no effect");
					return visByNation;
				}
			} catch (NoSuchMethodError x) {
				visByNation = false;
				DynmapTownyPlugin.getPlugin().getLogger().info("Dynmap does not support function needed for 'visibility-by-nation' - need to upgrade to 0.60 or later");
				return visByNation;
			}
			visByNation = true;
		}
		return visByNation;
	}

	public static boolean usingDynamicNationColours() {
		return getBoolean(ConfigNodes.DYNAMIC_COLOURS_NATION);
	}

	public static boolean usingDynamicTownColours() {
		return getBoolean(ConfigNodes.DYNAMIC_COLOURS_TOWN);
	}

	public static int getUpdatePeriod() {
		return getInt(ConfigNodes.UPDATE_PERIOD);
	}

	public static boolean showingShops() {
		return getBoolean(ConfigNodes.TOWNBLOCK_SHOW_SHOPS);
	}

	public static boolean showingArenas() {
		return getBoolean(ConfigNodes.TOWNBLOCK_SHOW_ARENAS);
	}

	public static boolean showingEmbassies() {
		return getBoolean(ConfigNodes.TOWNBLOCK_SHOW_EMBASSIES);
	}

	public static boolean showingWilds() {
		return getBoolean(ConfigNodes.TOWNBLOCK_SHOW_WILDSPLOTS);
	}

	public static Set<String> getVisibleRegions() {
		if (visibleRegions == null)
			visibleRegions = new HashSet<>(getStrArr(ConfigNodes.VISIBLE_ROOT));
		return visibleRegions;
	}

	public static boolean visibleRegionsAreSet() {
		Set<String> visibleRegions = getVisibleRegions();
		// Pre-CommentedConfiguration configs will have their region set to [], which throws off the results.
		return visibleRegions.size() > 0 && !(visibleRegions.size() == 1 && visibleRegions.contains("[]"));
	}

	public static Set<String> getHiddenRegions() {
		if (hiddenRegions == null)
			hiddenRegions = new HashSet<>(getStrArr(ConfigNodes.HIDDEN_ROOT));
		return hiddenRegions;
	}

	public static boolean hiddenRegionsAreSet() {
		Set<String> hiddenRegions = getHiddenRegions();
		// Pre-CommentedConfiguration configs will have their region set to [], which throws off the results.
		return hiddenRegions.size() > 0 && !(hiddenRegions.size() == 1 && hiddenRegions.contains("[]"));
	}
}

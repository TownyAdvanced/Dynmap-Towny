package org.dynmap.towny.settings;

public enum ConfigNodes {
	
	VERSION_HEADER("version", "", ""),
	VERSION(
			"version.version",
			"",
			"# This is the current version.  Please do not edit."),
	UPDATE_ROOT("update","","",""),
	UPDATE_PERIOD("update.period",
			"300",
			"# Seconds between updating Towny information on the dynmap."),

	LAYER_ROOT("layer","","",""),
	LAYER_NAME("layer.name",
			"Towny",
			"","# The name of the Towny layer on dynmap."),
	LAYER_HIDE_BY_DEFAULT("layer.hidebydefault",
			"false",
			"","# Is the Towny layer hidden by default?"),
	LAYER_PRIORITY("layer.layerprio",
			"2",
			"", "# Ordering priority in layer menu (low goes before high - default is 0)"),
	LAYER_MIN_ZOOM("layer.minzoom",
			"0",
			"", "# (optional) set minimum zoom level before layer is visible (0 = defalt, always visible)"),

	VISIBILITY_ROOT("player_visibility","","","# Requires Player-Info-Is-Protected enabled in dynmap."),
	VISIBILITY_BY_TOWN("player_visibility.visibility-by-town",
			"true",
			"","# Allow all residents of a given town to see one another."),
	VISIBILITY_BY_NATION("player_visibility.visibility-by-nation",
			"true",
			"","# Allow all residents of a given nation to see one another."),

	TOWNBLOCK_COLOURS_ROOT("townblock_colours","","",""),
	TOWNBLOCK_SHOW_SHOPS("townblock_colours.shops.showShops",
			"false",
			"", "# Show shop plots with their own fill colour."),
	TOWNBLOCK_SHOP_FILL_COLOUR("townblock_colours.shops.fillColour",
			"#0000FF",
			"", "# The colour shops plots are filled with."),
	TOWNBLOCK_SHOW_ARENAS("townblock_colours.arenas.showArenas",
			"false",
			"", "# Show arena plots with their own fill colour."),
	TOWNBLOCK_ARENA_FILL_COLOUR("townblock_colours.arenas.fillColour",
			"#FF00FF",
			"", "# The colour arena plots are filled with."),
	TOWNBLOCK_SHOW_EMBASSIES("townblock_colours.embassies.showEmbassies",
			"false",
			"", "# Show embassy plots with their own fill colour."),
	TOWNBLOCK_EMBASSY_FILL_COLOUR("townblock_colours.embassies.fillColour",
			"#00FFFF",
			"", "# The colour embassy plots are filled with."),
	TOWNBLOCK_SHOW_WILDSPLOTS("townblock_colours.wilds.showWilds",
			"false",
			"", "# Show wilds plots with their own fill colour."),
	TOWNBLOCK_WILDS_FILL_COLOUR("townblock_colours.wilds.fillColour",
			"#00FF00",
			"", "# The colour wilds plots are filled with."),

	DYNAMIC_COLOURS_ROOT("dynamic_colours","","",""),
	DYNAMIC_COLOURS_TOWN("dynamic_colours.town_colours",
			"true",
			"","# Use dynamic nation colors, which are set in-game by individual nations (using /t set mapcolor <colour>)"),
	DYNAMIC_COLOURS_NATION("dynamic_colours.nation_colours",
			"true",
			"","# Use dynamic nation colors, which are set in-game by individual nations (using /n set mapcolor <colour>)"),

	INFOWINDOW_ROOT("infowindow","","",""),
	INFOWINDOW_TOWN_POPUP("infowindow.town_info_window",
			"<div class=\"infowindow\"><span style=\"font-size:120%;\">%regionname% (%nation%)</span><br /> Mayor <span style=\"font-weight:bold;\">%playerowners%</span><br /> Associates <span style=\"font-weight:bold;\">%playermanagers%</span><br/>Flags<br /><span style=\"font-weight:bold;\">%flags%</span></div>",
			"","# Format for town popup."),
	INFOWINDOW_NO_NATION_SLUG("infowindow.noNationSlug",
			"",
			"","# What is shown in the info window's %nation% when a town has no nation."),

	REGIONSTYLE_ROOT("regionstyle","","",""),
	REGIONSTYLE_STROKE_COLOUR("regionstyle.strokeColor", "#FF0000"),
	REGIONSTYLE_STROKE_OPACITY("regionstyle.strokeOpacity", "0.8"),
	REGIONSTYLE_STROKE_WEIGHT("regionstyle.strokeWeight", "3"),
	REGIONSTYLE_FILL_COLOUR("regionstyle.fillColor", "#FF0000"),
	REGIONSTYLE_FILL_OPACITY("regionstyle.fillOpacity", "0.35"),
	REGIONSTYLE_BOOST("regionstyle.boost", "false"),
	REGIONSTYLE_ICONS_ROOT("regionstyle.icons","","",
			"# Allowed icon names found here: https://github.com/webbukkit/dynmap/wiki/Using-Markers",
			"# If you want to not show outposts, put in a invalid marker name ie: 'outpostIcon: boot'."),
	REGIONSTYLE_HOME_ICON("regionstyle.icons.homeIcon",
			"blueflag"),
	REGIONSTYLE_CAPITAL_ICON("regionstyle.icons.capitalIcon",
			"king"),
	REGIONSTYLE_OUTPOST_ICON("regionstyle.icons.outpostIcon",
			"tower"),
	REGIONSTYLE_RUIN_ICON("regionstyle.icons.ruinIcon",
			"warning"),

	VISIBLE_ROOT("visibleregions","","",
			"# Optional setting to limit which regions to show, by name - if commented out, all regions are shown.",
			"# To show all regions on a given world, add 'world:<worldname>' to the list."),
	HIDDEN_ROOT("hiddenregions","","",
			"# Optional setting to hide specific regions, by name.",
			"# To hide all regions on a given world, add 'world:<worldname>' to the list."),

	CUSTOMSTYLE_ROOT("custstyle","","",""),
	NATIONSTYLE_ROOT("nationstyle","","",""),
	
	CHAT_ROOT("chat","","","# Chat settings for use with TownyChat."),
	CHAT_SEND_LOGIN("chat.sendlogin","true"),
	CHAT_SEND_QUIT("chat.sendquit","true"),
	CHAT_FORMAT("chat.format","&color;2[WEB] %playername%: &color;f%message%"),

	LANGUAGE("language",
			"english.yml",
			"# The language file you wish to use");
	
	private final String Root;
	private final String Default;
	private String[] comments;

	ConfigNodes(String root, String def, String... comments) {

		this.Root = root;
		this.Default = def;
		this.comments = comments;
	}

	/**
	 * Retrieves the root for a config option
	 *
	 * @return The root for a config option
	 */
	public String getRoot() {

		return Root;
	}

	/**
	 * Retrieves the default value for a config path
	 *
	 * @return The default value for a config path
	 */
	public String getDefault() {

		return Default;
	}

	/**
	 * Retrieves the comment for a config path
	 *
	 * @return The comments for a config path
	 */
	public String[] getComments() {

		if (comments != null) {
			return comments;
		}

		String[] comments = new String[1];
		comments[0] = "";
		return comments;
	}

}

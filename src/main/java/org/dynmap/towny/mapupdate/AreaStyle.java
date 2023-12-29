package org.dynmap.towny.mapupdate;

import org.bukkit.configuration.file.FileConfiguration;
import org.dynmap.markers.MarkerAPI;
import org.dynmap.markers.MarkerIcon;
import org.dynmap.towny.DynmapTownyPlugin;
import org.dynmap.towny.settings.Settings;

import com.palmergames.bukkit.towny.object.TownBlockType;

public class AreaStyle {
	int strokecolor;
	double strokeopacity;
	int strokeweight;
	int fillcolor;
	double fillopacity;
	int fillcolor_shops;
	int fillcolor_embassies;
	int fillcolor_arenas;
	int fillcolor_wilds;
	MarkerIcon homeicon;
	MarkerIcon capitalicon;
	MarkerIcon ruinicon;
	MarkerIcon outposticon;
	int yc;
	boolean boost;

	/**
	 * Used to create AreaStyles based off of what is in the config used on all
	 * non-custom and non-nation styles.
	 */
	public AreaStyle() {
		String sc = Settings.getStrokeColour();
		strokeopacity = Settings.getStrokeOpacity();
		strokeweight = Settings.getStrokeWeight();
		String fc = Settings.getFillColour();
		String fcs = Settings.getShopsFillColour();
		String fca = Settings.getArenaFillColour();
		String fce = Settings.getEmbassyFillColour();
		String fcw = Settings.getWildsFillColour();
		yc = 64;
		boost = Settings.getBoost();

		strokecolor = -1;
		fillcolor = -1;
		fillcolor_shops = -1;
		fillcolor_arenas = -1;
		fillcolor_embassies = -1;
		fillcolor_wilds = -1;
		try {
			if (sc != null)
				strokecolor = Integer.parseInt(sc.substring(1), 16);
			if (fc != null)
				fillcolor = Integer.parseInt(fc.substring(1), 16);
			if (fcs != null)
				fillcolor_shops = Integer.parseInt(fcs.substring(1), 16);
			if (fca != null)
				fillcolor_arenas = Integer.parseInt(fca.substring(1), 16);
			if (fce != null)
				fillcolor_embassies = Integer.parseInt(fce.substring(1), 16);
			if (fcw != null)
				fillcolor_wilds = Integer.parseInt(fcw.substring(1), 16);
		} catch (NumberFormatException nfx) {
		}

		fillopacity = Settings.getFillOpacity();
		homeicon = Settings.getHomeIcon();
		capitalicon = Settings.getCapitalIcon();
		outposticon = Settings.getOutpostIcon();
		ruinicon = Settings.getRuinIcon();
	}

	/**
	 * Used to contain non-default style AreaStyles, used for CustomeStyles and
	 * NationStyles configured in the config.
	 * 
	 * @param path      The path of the config containing nationstyle + id or
	 *                  custstyle + id.
	 * @param markerapi Instance of the MarkerAPI we are using.
	 */
	public AreaStyle(String path, MarkerAPI markerapi) {
		FileConfiguration cfg = DynmapTownyPlugin.getPlugin().getConfig();
		String sc = cfg.getString(path + ".strokecolor", null);
		strokeopacity = cfg.getDouble(path + ".strokeopacity", -1);
		strokeweight = cfg.getInt(path + ".strokeweight", -1);
		String fc = cfg.getString(path + ".fillcolor", null);
		String fcs = cfg.getString(path + ".fillcolorshops", null);
		String fca = cfg.getString(path + ".fillcolorarenas", null);
		String fce = cfg.getString(path + ".fillcolorembassies", null);
		String fcw = cfg.getString(path + ".fillColorwilds", null);
		yc = cfg.getInt(path + ".y", -1);
		boost = cfg.getBoolean(path + ".boost", false);

		strokecolor = -1;
		fillcolor = -1;
		fillcolor_shops = -1;
		fillcolor_arenas = -1;
		fillcolor_embassies = -1;
		fillcolor_wilds = -1;
		try {
			if (sc != null)
				strokecolor = Integer.parseInt(sc.substring(1), 16);
			if (fc != null)
				fillcolor = Integer.parseInt(fc.substring(1), 16);
			if (fcs != null)
				fillcolor_shops = Integer.parseInt(fcs.substring(1), 16);
			if (fca != null)
				fillcolor_arenas = Integer.parseInt(fca.substring(1), 16);
			if (fce != null)
				fillcolor_embassies = Integer.parseInt(fce.substring(1), 16);
			if (fcw != null)
				fillcolor_wilds = Integer.parseInt(fcw.substring(1), 16);
		} catch (NumberFormatException nfx) {
		}

		fillopacity = cfg.getDouble(path + ".fillopacity", -1);
		String homemarker = cfg.getString(path + ".homeicon", null);
		if (homemarker != null) {
			homeicon = markerapi.getMarkerIcon(homemarker);
			if (homeicon == null) {
				homeicon = markerapi.getMarkerIcon("blueicon");
			}
		}
		String capitalmarker = cfg.getString(path + ".capitalicon", null);
		if (capitalmarker != null) {
			capitalicon = markerapi.getMarkerIcon(capitalmarker);
			if (capitalicon == null) {
				capitalicon = markerapi.getMarkerIcon("king");
			}
		}
		ruinicon = markerapi.getMarkerIcon("warning");

		String outpostmarker = cfg.getString(path + ".outposticon", "tower");
		outposticon = markerapi.getMarkerIcon(outpostmarker);
		if (outposticon == null)
			outposticon = markerapi.getMarkerIcon("tower");
	}

	public int getStrokeColor(AreaStyle cust, AreaStyle nat) {
		if ((cust != null) && (cust.strokecolor >= 0))
			return cust.strokecolor;
		else if ((nat != null) && (nat.strokecolor >= 0))
			return nat.strokecolor;
		else if (strokecolor >= 0)
			return strokecolor;
		else
			return 0xFF0000;
	}

	public double getStrokeOpacity(AreaStyle cust, AreaStyle nat) {
		if ((cust != null) && (cust.strokeopacity >= 0))
			return cust.strokeopacity;
		else if ((nat != null) && (nat.strokeopacity >= 0))
			return nat.strokeopacity;
		else if (strokeopacity >= 0)
			return strokeopacity;
		else
			return 0.8;
	}

	public int getStrokeWeight(AreaStyle cust, AreaStyle nat) {
		if ((cust != null) && (cust.strokeweight >= 0))
			return cust.strokeweight;
		else if ((nat != null) && (nat.strokeweight >= 0))
			return nat.strokeweight;
		else if (strokeweight >= 0)
			return strokeweight;
		else
			return 3;
	}

	public int getFillColor(TownBlockType btype) {
		if (btype == TownBlockType.COMMERCIAL)
			return fillcolor_shops;
		if (btype == TownBlockType.ARENA)
			return fillcolor_arenas;
		if (btype == TownBlockType.EMBASSY)
			return fillcolor_embassies;
		if (btype == TownBlockType.WILDS)
			return fillcolor_wilds;
		return -1;
	}

	public int getFillColor(AreaStyle cust, AreaStyle nat, TownBlockType btype) {
		if (btype == TownBlockType.COMMERCIAL) {
			if ((cust != null) && (cust.fillcolor_shops >= 0))
				return cust.fillcolor_shops;
			else if ((nat != null) && (nat.fillcolor_shops >= 0))
				return nat.fillcolor_shops;
			else if (fillcolor_shops >= 0)
				return fillcolor_shops;
			else
				return 0xFF0000;
		} else if (btype == TownBlockType.ARENA) {
			if ((cust != null) && (cust.fillcolor_arenas >= 0))
				return cust.fillcolor_shops;
			else if ((nat != null) && (nat.fillcolor_arenas >= 0))
				return nat.fillcolor_arenas;
			else if (fillcolor_arenas >= 0)
				return fillcolor_arenas;
			else
				return 0xFF0000;
		} else if (btype == TownBlockType.EMBASSY) {
			if ((cust != null) && (cust.fillcolor_embassies >= 0))
				return cust.fillcolor_embassies;
			else if ((nat != null) && (nat.fillcolor_embassies >= 0))
				return nat.fillcolor_embassies;
			else if (fillcolor_embassies >= 0)
				return fillcolor_embassies;
			else
				return 0xFF0000;
		} else if (btype == TownBlockType.WILDS) {
			if ((cust != null) && (cust.fillcolor_wilds >= 0))
				return cust.fillcolor_wilds;
			else if ((nat != null) && (nat.fillcolor_wilds >= 0))
				return nat.fillcolor_wilds;
			else if (fillcolor_wilds >= 0)
				return fillcolor_wilds;
			else
				return 0xFF0000;
		}
		if ((cust != null) && (cust.fillcolor >= 0))
			return cust.fillcolor;
		else if ((nat != null) && (nat.fillcolor >= 0))
			return nat.fillcolor;
		else if (fillcolor >= 0)
			return fillcolor;
		else
			return 0xFF0000;
	}

	public double getFillOpacity(AreaStyle cust, AreaStyle nat) {
		if ((cust != null) && (cust.fillopacity >= 0))
			return cust.fillopacity;
		else if ((nat != null) && (nat.fillopacity >= 0))
			return nat.fillopacity;
		else if (fillopacity >= 0)
			return fillopacity;
		else
			return 0.35;
	}

	public MarkerIcon getHomeMarker(AreaStyle cust, AreaStyle nat) {
		if ((cust != null) && (cust.homeicon != null))
			return cust.homeicon;
		else if ((nat != null) && (nat.homeicon != null))
			return nat.homeicon;
		else
			return homeicon;
	}

	public MarkerIcon getCapitalMarker(AreaStyle cust, AreaStyle nat) {
		if ((cust != null) && (cust.capitalicon != null))
			return cust.capitalicon;
		else if ((nat != null) && (nat.capitalicon != null))
			return nat.capitalicon;
		else if (capitalicon != null)
			return capitalicon;
		else
			return getHomeMarker(cust, nat);
	}

	public MarkerIcon getRuinIcon() {
		return ruinicon;
	}

	public int getY(AreaStyle cust, AreaStyle nat) {
		if ((cust != null) && (cust.yc >= 0))
			return cust.yc;
		else if ((nat != null) && (nat.yc >= 0))
			return nat.yc;
		else if (yc >= 0)
			return yc;
		else
			return 64;
	}

	public boolean getBoost(AreaStyle cust, AreaStyle nat) {
		if ((cust != null) && cust.boost)
			return cust.boost;
		else if ((nat != null) && nat.boost)
			return nat.boost;
		else
			return boost;
	}
}

package org.dynmap.towny.mapupdate;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.dynmap.markers.AreaMarker;
import org.dynmap.markers.Marker;
import org.dynmap.markers.MarkerIcon;
import org.dynmap.markers.MarkerSet;
import org.dynmap.towny.DynmapTownyPlugin;
import org.dynmap.towny.events.BuildTownMarkerDescriptionEvent;
import org.dynmap.towny.events.TownRenderEvent;
import org.dynmap.towny.events.TownSetMarkerIconEvent;
import org.dynmap.towny.settings.Settings;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.TownySettings;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownBlock;
import com.palmergames.bukkit.towny.object.TownBlockType;
import com.palmergames.bukkit.towny.object.TownBlockTypeCache.CacheType;
import com.palmergames.bukkit.towny.object.TownyWorld;

public class UpdateTowns implements Runnable {

	private final int TOWNBLOCKSIZE = TownySettings.getTownBlockSize();
	private final DynmapTownyPlugin plugin = DynmapTownyPlugin.getPlugin();
	private final MarkerSet set = plugin.getMarkerSet();
	private Map<String, AreaMarker> existingAreaMarkers = plugin.getAreaMarkers();
	private Map<String, Marker> existingMarkers = plugin.getMarkers();

	private final AreaStyle defstyle = AreaStyleHolder.getDefaultStyle();
	private final Map<String, AreaStyle> cusstyle = AreaStyleHolder.getCustomStyles();
	private final Map<String, AreaStyle> nationstyle = AreaStyleHolder.getNationStyles();

	enum direction {XPLUS, ZPLUS, XMINUS, ZMINUS};

	@Override
	public void run() {
		Map<String, AreaMarker> newmap = new HashMap<String, AreaMarker>(); /* Build new map */
		Map<String, Marker> newmark = new HashMap<String, Marker>(); /* Build new map */

		/* Loop through towns */
		for (Town t : TownyAPI.getInstance().getTowns()) {
			try {
				handleTown(t, newmap, newmark, null);
				if (Settings.showingShops() && townHasTBsOfType(t, TownBlockType.COMMERCIAL)) {
					handleTown(t, newmap, newmark, TownBlockType.COMMERCIAL);
				}
				if (Settings.showingArenas() && townHasTBsOfType(t, TownBlockType.ARENA)) {
					handleTown(t, newmap, newmark, TownBlockType.ARENA);
				}
				if (Settings.showingEmbassies() && townHasTBsOfType(t, TownBlockType.EMBASSY)) {
					handleTown(t, newmap, newmark, TownBlockType.EMBASSY);
				}
				if (Settings.showingWilds() && townHasTBsOfType(t, TownBlockType.WILDS)) {
					handleTown(t, newmap, newmark, TownBlockType.WILDS);
				}
			} catch (Exception e) {
				plugin.getLogger().info(e.getMessage());
			}
		}
		/* Now, review old maps - anything left is removed */
		existingAreaMarkers.values().forEach(a -> a.deleteMarker());
		existingMarkers.values().forEach(m -> m.deleteMarker());

		/* And replace with new map */
		existingAreaMarkers = newmap;
		existingMarkers = newmark;
	}

	private boolean townHasTBsOfType(Town t, TownBlockType tbType) {
		return t.getTownBlockTypeCache().getNumTownBlocks(tbType, CacheType.ALL) > 0;
	}

	/* Handle specific town */
	private void handleTown(Town town, Map<String, AreaMarker> newWorldNameAreaMarkerMap, Map<String, Marker> newWorldNameMarkerMap, TownBlockType btype) throws Exception {
		String townName = town.getName();
		int poly_index = 0; /* Index of polygon for when a town has multiple shapes. */

		/* Get the Town's Townblocks or the Towns' Townblocks of one TownBlockType. */
		Collection<TownBlock> townBlocks = filterTownBlocks(town, btype);
		if (townBlocks.isEmpty())
			return;

		/* Build popup */
		BuildTownMarkerDescriptionEvent event = new BuildTownMarkerDescriptionEvent(town);
		Bukkit.getPluginManager().callEvent(event);
		String infoWindowPopup = event.getDescription();

    	HashMap<String, TileFlags> worldNameShapeMap = new HashMap<String, TileFlags>();
        LinkedList<TownBlock> townBlocksToDraw = new LinkedList<TownBlock>();
        TownyWorld currentWorld = null;
        TileFlags currentShape = null;
        boolean vis = false;
    	/* Loop through blocks: set flags on blockmaps for worlds */
    	for(TownBlock townBlock : townBlocks) {
    	    if(townBlock.getWorld() != currentWorld) { /* Not same world */
    	        String worldName = townBlock.getWorld().getName();
    	        vis = isVisible(townName, worldName);  /* See if visible */
    	        if(vis) {  /* Only accumulate for visible areas */
    	            currentShape = worldNameShapeMap.get(worldName);  /* Find existing */
    	            if(currentShape == null) {
    	                currentShape = new TileFlags();
    	                worldNameShapeMap.put(worldName, currentShape);   /* Add fresh one */
    	            }
    	        }
    	        currentWorld = townBlock.getWorld();
    	    }
    	    if(vis) {
    	        currentShape.setFlag(townBlock.getX(), townBlock.getZ(), true); /* Set flag for block */
    	        townBlocksToDraw.addLast(townBlock);
    	    }
    	}
        /* Loop through until we don't find more areas */
        while(townBlocksToDraw != null) {
            LinkedList<TownBlock> ourTownBlocks = null;
            LinkedList<TownBlock> townBlockLeftToDraw = null;
            TileFlags ourShape = null;
            int minx = Integer.MAX_VALUE;
            int minz = Integer.MAX_VALUE;
            for(TownBlock tb : townBlocksToDraw) {
                int tbX = tb.getX();
                int tbZ = tb.getZ();
                if(ourShape == null) {   /* If not started, switch to world for this block first */
                    if(tb.getWorld() != currentWorld) {
                        currentWorld = tb.getWorld();
                        currentShape = worldNameShapeMap.get(currentWorld.getName());
                    }
                }
                /* If we need to start shape, and this block is not part of one yet */
                if((ourShape == null) && currentShape.getFlag(tbX, tbZ)) {
                    ourShape = new TileFlags();  /* Create map for shape */
                    ourTownBlocks = new LinkedList<TownBlock>();
                    floodFillTarget(currentShape, ourShape, tbX, tbZ);   /* Copy shape */
                    ourTownBlocks.add(tb); /* Add it to our node list */
                    minx = tbX; minz = tbZ;
                }
                /* If shape found, and we're in it, add to our node list */
                else if((ourShape != null) && (tb.getWorld() == currentWorld) &&
                    (ourShape.getFlag(tbX, tbZ))) {
                    ourTownBlocks.add(tb);
                    if(tbX < minx) {
                        minx = tbX; minz = tbZ;
                    }
                    else if((tbX == minx) && (tbZ < minz)) {
                        minz = tbZ;
                    }
                }
                else {  /* Else, keep it in the list for the next polygon */
                    if(townBlockLeftToDraw == null)
                    	townBlockLeftToDraw = new LinkedList<TownBlock>();
                    townBlockLeftToDraw.add(tb);
                }
            }
            townBlocksToDraw = townBlockLeftToDraw; /* Replace list (null if no more to process) */
            if(ourShape != null) {
                poly_index = traceTownOutline(town, newWorldNameAreaMarkerMap, btype, poly_index, infoWindowPopup, currentWorld.getName(), ourShape, minx, minz);
            }
        }

		/* We're showing only specific TownBlockTypes on this pass, dont render townspawns and outposts.*/
		if (btype != null)
			return;

		drawTownMarkers(town, newWorldNameMarkerMap, townName, infoWindowPopup);
	}

	private Collection<TownBlock> filterTownBlocks(Town town, TownBlockType btype) {
		if (btype == null)
			return town.getTownBlocks();
		return town.getTownBlocks().stream()
				.filter(tb -> tb.getTypeName().equalsIgnoreCase(btype.getName()))
				.collect(Collectors.toList());
	}

	private int traceTownOutline(Town town, Map<String, AreaMarker> newWorldNameMarkerMap, TownBlockType btype, int poly_index,
			String infoWindowPopup, String worldName, TileFlags ourShape, int minx, int minz) throws Exception {

		double[] x;
		double[] z;
		/* Trace outline of blocks - start from minx, minz going to x+ */
		int init_x = minx;
		int init_z = minz;
		int cur_x = minx;
		int cur_z = minz;
		direction dir = direction.XPLUS;
		ArrayList<int[]> linelist = new ArrayList<int[]>();
		linelist.add(new int[] { init_x, init_z } ); // Add start point
		while((cur_x != init_x) || (cur_z != init_z) || (dir != direction.ZMINUS)) {
		    switch(dir) {
		        case XPLUS: /* Segment in X+ direction */
		            if(!ourShape.getFlag(cur_x+1, cur_z)) { /* Right turn? */
		                linelist.add(new int[] { cur_x+1, cur_z }); /* Finish line */
		                dir = direction.ZPLUS;  /* Change direction */
		            }
		            else if(!ourShape.getFlag(cur_x+1, cur_z-1)) {  /* Straight? */
		                cur_x++;
		            }
		            else {  /* Left turn */
		                linelist.add(new int[] { cur_x+1, cur_z }); /* Finish line */
		                dir = direction.ZMINUS;
		                cur_x++; cur_z--;
		            }
		            break;
		        case ZPLUS: /* Segment in Z+ direction */
		            if(!ourShape.getFlag(cur_x, cur_z+1)) { /* Right turn? */
		                linelist.add(new int[] { cur_x+1, cur_z+1 }); /* Finish line */
		                dir = direction.XMINUS;  /* Change direction */
		            }
		            else if(!ourShape.getFlag(cur_x+1, cur_z+1)) {  /* Straight? */
		                cur_z++;
		            }
		            else {  /* Left turn */
		                linelist.add(new int[] { cur_x+1, cur_z+1 }); /* Finish line */
		                dir = direction.XPLUS;
		                cur_x++; cur_z++;
		            }
		            break;
		        case XMINUS: /* Segment in X- direction */
		            if(!ourShape.getFlag(cur_x-1, cur_z)) { /* Right turn? */
		                linelist.add(new int[] { cur_x, cur_z+1 }); /* Finish line */
		                dir = direction.ZMINUS;  /* Change direction */
		            }
		            else if(!ourShape.getFlag(cur_x-1, cur_z+1)) {  /* Straight? */
		                cur_x--;
		            }
		            else {  /* Left turn */
		                linelist.add(new int[] { cur_x, cur_z+1 }); /* Finish line */
		                dir = direction.ZPLUS;
		                cur_x--; cur_z++;
		            }
		            break;
		        case ZMINUS: /* Segment in Z- direction */
		            if(!ourShape.getFlag(cur_x, cur_z-1)) { /* Right turn? */
		                linelist.add(new int[] { cur_x, cur_z }); /* Finish line */
		                dir = direction.XPLUS;  /* Change direction */
		            }
		            else if(!ourShape.getFlag(cur_x-1, cur_z-1)) {  /* Straight? */
		                cur_z--;
		            }
		            else {  /* Left turn */
		                linelist.add(new int[] { cur_x, cur_z }); /* Finish line */
		                dir = direction.XMINUS;
		                cur_x--; cur_z--;
		            }
		            break;
		    }
		}
		/* Build information for specific area */
		String polyid = town.getName() + "__" + poly_index;
		if(btype != null) {
			polyid += "_" + btype.getName();
		}
		int sz = linelist.size();
		x = new double[sz];
		z = new double[sz];
		for(int i = 0; i < sz; i++) {
		    int[] line = linelist.get(i);
		    x[i] = (double)line[0] * (double)TOWNBLOCKSIZE;
		    z[i] = (double)line[1] * (double)TOWNBLOCKSIZE;
		}
		/* Find existing one */
		AreaMarker areaMarker = existingAreaMarkers.remove(polyid); /* Existing area? */
		if(areaMarker == null) {
		    areaMarker = set.createAreaMarker(polyid, town.getName(), false, worldName, x, z, false);
		    if(areaMarker == null) {
		    	areaMarker = set.findAreaMarker(polyid);
		    	if (areaMarker == null) {
		            throw new Exception("Error adding area marker " + polyid);
		    	}
		    }
		}
		else {
		    areaMarker.setCornerLocations(x, z); /* Replace corner locations */
		    areaMarker.setLabel(town.getName());   /* Update label */
		}
		/* Set popup */
		areaMarker.setDescription(infoWindowPopup);
		/* Set line and fill properties */
		addStyle(town, areaMarker, btype);

		/* Fire an event allowing other plugins to alter the AreaMarker */
		TownRenderEvent renderEvent = new TownRenderEvent(town, areaMarker); 
		Bukkit.getPluginManager().callEvent(renderEvent);
		areaMarker = renderEvent.getAreaMarker();

		/* Add to map */
		newWorldNameMarkerMap.put(polyid, areaMarker);
		poly_index++;
		return poly_index;
	}

	/**
	 * Find all contiguous blocks, set in target and clear in source
	 */
	private static int floodFillTarget(TileFlags src, TileFlags dest, int x, int y) {
		int cnt = 0;
		ArrayDeque<int[]> stack = new ArrayDeque<int[]>();
		stack.push(new int[] { x, y });

		while (stack.isEmpty() == false) {
			int[] nxt = stack.pop();
			x = nxt[0];
			y = nxt[1];
			if (src.getFlag(x, y)) { /* Set in src */
				src.setFlag(x, y, false); /* Clear source */
				dest.setFlag(x, y, true); /* Set in destination */
				cnt++;
				if (src.getFlag(x + 1, y))
					stack.push(new int[] { x + 1, y });
				if (src.getFlag(x - 1, y))
					stack.push(new int[] { x - 1, y });
				if (src.getFlag(x, y + 1))
					stack.push(new int[] { x, y + 1 });
				if (src.getFlag(x, y - 1))
					stack.push(new int[] { x, y - 1 });
			}
		}
		return cnt;
	}

	private static boolean isVisible(String id, String worldname) {

		if (Settings.visibleRegionsAreSet() &&
			(Settings.getVisibleRegions().contains("world:" + worldname) == false || Settings.getVisibleRegions().contains(id) == false))
				return false;

		if (Settings.hiddenRegionsAreSet() &&
			(Settings.getHiddenRegions().contains(id) || Settings.getHiddenRegions().contains("world:" + worldname)))
				return false;
		return true;
	}

    private void addStyle(Town town, AreaMarker m, TownBlockType btype) {
        AreaStyle as = cusstyle.get(town.getName());	/* Look up custom style for town, if any */
        AreaStyle ns = nationstyle.get(getNationNameOrNone(town));	/* Look up nation style, if any */
        
        if(btype == null) {
            m.setLineStyle(defstyle.getStrokeWeight(as, ns), defstyle.getStrokeOpacity(as, ns), defstyle.getStrokeColor(as, ns));
        }
        else {
            m.setLineStyle(1, 0, 0);
        }
        m.setFillStyle(defstyle.getFillOpacity(as, ns), defstyle.getFillColor(as, ns, btype));
        double y = defstyle.getY(as, ns);
        m.setRangeY(y, y);
        m.setBoostFlag(defstyle.getBoost(as, ns));

		// We're dealing with something that has a custom AreaStyle applied via the
		// custstyle or nationstyle in the config.yml, do not apply dynamic colours.
		if (as != null || (ns != null && ns.fillcolor > -1 && ns.strokecolor > -1))
			return;

        //Read dynamic colors from town/nation objects
        if(Settings.usingDynamicTownColours() || Settings.usingDynamicNationColours()) {
            try {
                String colorHexCode; 
                Integer townFillColorInteger = null;
                Integer townBorderColorInteger = null;

                //CALCULATE FILL COLOUR
                if (Settings.usingDynamicTownColours()) {
                    //Here we know the server is using town colors. This takes top priority for fill
                    colorHexCode = town.getMapColorHexCode();              
                    if(!colorHexCode.isEmpty()) {
                        //If town has a color, use it
                        townFillColorInteger = Integer.parseInt(colorHexCode, 16);
                        townBorderColorInteger = townFillColorInteger;
                    }                
                } else {
                    //Here we know the server is using nation colors
                    colorHexCode = town.getNationMapColorHexCode();              
                    if(colorHexCode != null && !colorHexCode.isEmpty()) {
                        //If nation has a color, use it
                        townFillColorInteger = Integer.parseInt(colorHexCode, 16);                
                    }                            
                }

                //CALCULATE BORDER COLOR
                if (Settings.usingDynamicNationColours()) {
                    //Here we know the server is using nation colors. This takes top priority for border
                    colorHexCode = town.getNationMapColorHexCode();              
                    if(colorHexCode != null && !colorHexCode.isEmpty()) {
                        //If nation has a color, use it
                        townBorderColorInteger = Integer.parseInt(colorHexCode, 16);                
                    }                                
                } else {
                    //Here we know the server is using town colors
                    colorHexCode = town.getMapColorHexCode();              
                    if(!colorHexCode.isEmpty()) {
                        //If town has a color, use it
                        townBorderColorInteger = Integer.parseInt(colorHexCode, 16);                
                    }                
                }

                //SET FILL COLOR
                if(townFillColorInteger != null) {
                    //Set fill style
                    double fillOpacity = m.getFillOpacity();
                    //Allow special fills for some townblock types
                    int townblockcolor = defstyle.getFillColor(btype);
                    m.setFillStyle(fillOpacity, townblockcolor >= 0 ? townblockcolor : townFillColorInteger);
                }

                //SET BORDER COLOR
                if(townBorderColorInteger != null) {
                    //Set stroke style
                    double strokeOpacity = m.getLineOpacity();
                    int strokeWeight = m.getLineWeight();
                    m.setLineStyle(strokeWeight, strokeOpacity, townBorderColorInteger);
                }   

            } catch (Exception ex) {}
        }
    }


	/*
	 * Town Marker Drawing Methods
	 */

	private void drawTownMarkers(Town town, Map<String, Marker> newWorldNameMarkerMap, String townName, String desc) {
		/* Now, add marker for home block */
		TownBlock homeBlock = town.getHomeBlockOrNull();
		
		if (homeBlock != null && isVisible(townName, homeBlock.getWorld().getName())) {
			MarkerIcon townHomeBlockIcon = getMarkerIcon(town);

			/* Fire an event allowing other plugins to alter the MarkerIcon */
			TownSetMarkerIconEvent iconEvent = new TownSetMarkerIconEvent(town, townHomeBlockIcon);
			Bukkit.getPluginManager().callEvent(iconEvent);
			townHomeBlockIcon = iconEvent.getIcon();

			if (townHomeBlockIcon != null)
				drawHomeBlockSpawn(newWorldNameMarkerMap, townName, desc, homeBlock, townHomeBlockIcon);
		}

		if (town.hasOutpostSpawn() && Settings.getOutpostIcon() != null)
			drawOutpostIcons(town, newWorldNameMarkerMap, desc);
	}

	private MarkerIcon getMarkerIcon(Town town) {
		if (town.isRuined())
			return defstyle.getRuinIcon();;

		AreaStyle as = cusstyle.get(town.getName());
		AreaStyle ns = nationstyle.get(getNationNameOrNone(town));

		return town.isCapital() ? defstyle.getCapitalMarker(as, ns) : defstyle.getHomeMarker(as, ns);
	}

	private String getNationNameOrNone(Town town) {
		return town.hasNation() ? town.getNationOrNull().getName() : "_none_";
	}

	private void drawHomeBlockSpawn(Map<String, Marker> newWorldNameMarkerMap, String townName, String desc, TownBlock townBlock, MarkerIcon ico) {
		String markid = townName + "__home";
		Marker home = existingMarkers.remove(markid);
		double xx = TOWNBLOCKSIZE * townBlock.getX() + (TOWNBLOCKSIZE / 2);
		double zz = TOWNBLOCKSIZE * townBlock.getZ() + (TOWNBLOCKSIZE / 2);
		if(home == null) {
			home = set.createMarker(markid, townName, townBlock.getWorld().getName(), xx, 64, zz, ico, false);
			if (home == null)
				return;
		} else {
			home.setLocation(townBlock.getWorld().getName(), xx, 64, zz);
			home.setLabel(townName); /* Update label */
			home.setMarkerIcon(ico);
		}
		/* Set popup */
		home.setDescription(desc);

		newWorldNameMarkerMap.put(markid, home);
	}

	private void drawOutpostIcons(Town town, Map<String, Marker> newWorldNameMarkerMap, String desc) {
		MarkerIcon outpostIco = Settings.getOutpostIcon();
		int i = 0;
		for (Location loc : town.getAllOutpostSpawns()) {
			i++;
			TownBlock townBlock = TownyAPI.getInstance().getTownBlock(loc);
			if (townBlock == null || !isVisible(town.getName(), townBlock.getWorld().getName()))
				continue;

			double xx = TOWNBLOCKSIZE * townBlock.getX() + (TOWNBLOCKSIZE / 2);
			double zz = TOWNBLOCKSIZE * townBlock.getZ() + (TOWNBLOCKSIZE / 2);
			String outpostName = town.getName() + "_Outpost_" + i;
			String outpostMarkerID = outpostName;
			Marker outpostMarker = existingMarkers.remove(outpostMarkerID);
			if (outpostMarker == null) {
				outpostMarker = set.createMarker(outpostMarkerID, outpostName, townBlock.getWorld().getName(), xx, 64, zz, outpostIco, true);
				if (outpostMarker == null)
					continue;
			} else {
				outpostMarker.setLocation(townBlock.getWorld().getName(), xx, 64, zz);
				outpostMarker.setLabel(outpostName);
				outpostMarker.setMarkerIcon(outpostIco);
			}
			outpostMarker.setDescription(desc);
			newWorldNameMarkerMap.put(outpostMarkerID, outpostMarker);
		}
	}

}

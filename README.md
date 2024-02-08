# Dynmap-Towny

Dynmap-Towny provides a simple way to add visibility of Towny towns and nations on Dynmap's maps. The plugin depends on the presence of both Dynmap and Towny Advanced, and interacts directly with the Towny API. Updates to zones are automatically processed (on a settable period - default is once per 5 minutes (300 seconds)). By default, the plugin will be active after simply installing it (by unzipping the distribution into the plugins/ directory and restarting the server).

Towns of any shape are supported, and a proper outline border is computed and displayed that will encompass all the contiguous blocks of a given town (**limitation - 'holes' in the middle of a town may still be shaded to look like part of the town, this is a limitation of Dynmap itself.**). Outposts, including outposts on other worlds, are supported. Clicking on the town will display a popup with a configurable set of data on the town.

Dynmap-Towny will also show configurable icons for the home block of each town, including distinctive icons when the town is a capital of its country.

Display style, including the color and opacity of the outlines and fill, as well as icons used for home markers, can be tailored. This can be done at the global default level, the per-nation level, or the per town level. The Y coordinate (altitude) of the town outlines can also be set (default is 64 - standard sea level) - typically this would be done using 'custstyle', to set the value for each town needing to be adjusted individually.

Visibility of towns can be controlled via the 'visibleregions' and 'hiddenregions' settings. Besides listing the names of the towns to be made visible or hidden, entries with the format 'world:<worldname>' can be used to make all towns on a given world visible or hidden.

Also, the display of the town outlines can be restricted to a minimum zoom-in level, via the 'minzoom' setting. When non-zero, this setting causes the town outlines to only be displayed at or beyond the given zoom-in level.

## Plugin Configuration
After the first load, there will be a config.yml file in the plugins/Dynmap-Towny directory. Details of the default configuration, and all the provided settings, can be found [here](https://github.com/TownyAdvanced/Dynmap-Towny/wiki)

## Acknowledgements
This is a fork of [Dynmap-Towny](https://github.com/webbukkit/Dynmap-Towny).

LlmDl forked this from Hank Jordan who forked it from the original repo.


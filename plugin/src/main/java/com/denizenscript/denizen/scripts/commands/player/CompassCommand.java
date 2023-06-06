package com.denizenscript.denizen.scripts.commands.player;

import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.objects.Argument;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class CompassCommand extends AbstractCommand {

    public CompassCommand() {
        setName("compass");
        setSyntax("compass [<location>/reset] (for:<player>|...)");
        setRequiredArguments(1, 2);
        isProcedural = false;
    }

    // <--[command]
    // @Name Compass
    // @Syntax compass [<location>/reset] (for:<player>|...)
    // @Required 1
    // @Maximum 2
    // @Short Redirects the player's compass to target the given location.
    // @Group player
    //
    // @Description
    // Redirects the compass of the player, who is attached to the script queue.
    //
    // This is not the compass item, but the command is controlling the pointer the item should direct at.
    // This means that all item compasses will point the same direction but differently for each player.
    //
    // To affect an individual compass item, use <@link mechanism ItemTag.lodestone_location>
    //
    // The y-axis is not used but its fine to be included in the location argument.
    //
    // Reset argument will turn the direction to default (spawn or bed)
    //
    // @Tags
    // <PlayerTag.compass_target>
    //
    // @Usage
    // Use to reset the compass direction to its default.
    // - compass reset
    //
    // @Usage
    // Use to point with a compass to the player's current location.
    // - compass <player.location>
    //
    // @Usage
    // Use to point with a compass to the world's spawn location.
    // - compass <player.world.spawn_location>
    // -->

    @Override
    public void addCustomTabCompletions(TabCompletionsBuilder tab) {
        tab.addNotesOfType(LocationTag.class);
    }

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {
        for (Argument arg : scriptEntry) {
            if (!scriptEntry.hasObject("players")
                    && arg.matchesPrefix("for", "players")) {
                scriptEntry.addObject("players", arg.asType(ListTag.class).filter(PlayerTag.class, scriptEntry));
            }
            else if (!scriptEntry.hasObject("location")
                    && arg.matchesArgumentType(LocationTag.class)) {
                scriptEntry.addObject("location", arg.asType(LocationTag.class));
            }
            else if (!scriptEntry.hasObject("reset")
                    && arg.matches("reset")) {
                scriptEntry.addObject("reset", new ElementTag(true));
            }
            else {
                arg.reportUnhandled();
            }
        }
        if (!scriptEntry.hasObject("location") && !scriptEntry.hasObject("reset")) {
            throw new InvalidArgumentsException("Missing location argument!");
        }
        else if (!scriptEntry.hasObject("players")) {
            if (!Utilities.entryHasPlayer(scriptEntry)) {
                throw new InvalidArgumentsException("This command must have a player attached!");
            }
            else {
                scriptEntry.addObject("players",
                        Collections.singletonList(Utilities.getEntryPlayer(scriptEntry)));
            }
        }

        scriptEntry.defaultObject("reset", new ElementTag(false));
    }

    @Override
    public void execute(ScriptEntry scriptEntry) {
        LocationTag location = scriptEntry.getObjectTag("location");
        ElementTag reset = scriptEntry.getElement("reset");
        List<PlayerTag> players = (List<PlayerTag>) scriptEntry.getObject("players");
        if (scriptEntry.dbCallShouldDebug()) {
            Debug.report(scriptEntry, getName(), db("Player(s)", players), location, reset);
        }
        if (reset.asBoolean()) {
            for (PlayerTag player : players) {
                Player playerEntity = player.getPlayerEntity();
                Location bed = playerEntity.getBedSpawnLocation();
                playerEntity.setCompassTarget(bed != null ? bed : player.getWorld().getSpawnLocation());
            }
        }
        else {
            for (PlayerTag player : players) {
                player.getPlayerEntity().setCompassTarget(location);
            }
        }
    }
}

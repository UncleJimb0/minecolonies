package com.minecolonies.commands;

import com.minecolonies.colony.CitizenData;
import com.minecolonies.colony.Colony;
import com.minecolonies.colony.ColonyManager;
import com.minecolonies.colony.IColony;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.*;

/**
 * List all colonies.
 */
public class ListCitizens extends AbstractSingleCommand
{

    private static final String ID_TEXT                 = "§2ID: §f";
    private static final String NAME_TEXT               = "§2 Name: §f";
    private static final String COORDINATES_TEXT        = "§2Coordinates: §f";
    private static final String LIST_COMMAND_SUGGESTED  = "/mc citizens list ";
    private static final int    CITIZENS_ON_PAGE        = 9;
    private static final String NO_COLONY_FOUND_MESSAGE = "No colony found for id: %d.";

    /**
     * Initialize this SubCommand with it's parents.
     *
     * @param parents an array of all the parents.
     */
    public ListCitizens(@NotNull final String... parents)
    {
        super(parents);
    }

    @NotNull
    @Override
    public String getCommandUsage(@NotNull final ICommandSender sender)
    {
        return super.getCommandUsage(sender) + "";
    }

    @Override
    public void execute(@NotNull final MinecraftServer server, @NotNull final ICommandSender sender, @NotNull final String... args) throws CommandException
    {
        int colonyId = getIthArgument(args, 0, getColonyId(sender));
        final Colony colony = ColonyManager.getColony(colonyId);

        if (colony == null)
        {
            sender.addChatMessage(new TextComponentString(String.format(NO_COLONY_FOUND_MESSAGE, colonyId)));
            return;
        }

        final List<CitizenData> citizens = new ArrayList<>(colony.getCitizens().values());
        final int citizenCount = citizens.size();

        // check to see if we have to add one page to show the half page
        int page = getIthArgument(args, 0, 1);
        final int halfPage = (citizenCount % CITIZENS_ON_PAGE == 0) ? 0 : 1;
        final int pageCount = ((citizenCount) / CITIZENS_ON_PAGE) + halfPage;

        if (page < 1 || page > pageCount)
        {
            page = 1;
        }

        final int pageStartIndex = CITIZENS_ON_PAGE * (page - 1);
        final int pageStopIndex = Math.min(CITIZENS_ON_PAGE * page, citizenCount);


        List<CitizenData> citizensPage;

        if (pageStartIndex < 0 || pageStartIndex >= citizenCount)
        {
            citizensPage = new ArrayList<>();
        }
        else
        {
            citizensPage = citizens.subList(pageStartIndex, pageStopIndex);
        }

        final ITextComponent headerLine = new TextComponentString("§2   ------------------ page " + page + " of " + pageCount + " ------------------");
        sender.addChatMessage(headerLine);

        for (final CitizenData citizen : citizensPage)
        {
            sender.addChatMessage(new TextComponentString(ID_TEXT + citizen.getId() + NAME_TEXT + citizen.getName()));

            if (citizen.getCitizenEntity() != null)
            {
                final BlockPos position = citizen.getCitizenEntity().getPosition();
                sender.addChatMessage(new TextComponentString(COORDINATES_TEXT + String.format("§4x=§f%s §4y=§f%s §4z=§f%s", position.getX(), position.getY(), position.getZ())));
            }
        }
        drawPageSwitcher(sender, colonyId, page, citizenCount, halfPage);
    }

    /**
     * Get the ith argument (An Integer).
     * @param i the argument from the list you want.
     * @param args the list of arguments.
     * @param def the default value.
     * @return the argument.
     */
    private int getIthArgument(String[] args, int i, int def)
    {
        try
        {
            return Integer.parseInt(args[i]);
        }
        catch (NumberFormatException e)
        {
            return def;
        }
    }

    /**
     * Returns the colony of the owner or if not available colony 1.
     * @param sender the sender of the command.
     * @return the colonyId.
     */
    private int getColonyId( @NotNull final ICommandSender sender)
    {
        final IColony tempColony = ColonyManager.getIColonyByOwner(sender.getEntityWorld(), sender.getCommandSenderEntity().getUniqueID());
        if(tempColony != null)
        {
            final Colony colony = ColonyManager.getColony(sender.getEntityWorld(), tempColony.getCenter());
            if(colony != null)
            {
                return colony.getID();
            }
        }

        return 1;
    }

    /**
     * Draws the page switcher at the bottom
     * @param sender the sender.
     * @param colonyId the colonyid.
     * @param page the page number.
     * @param count number of citizens.
     * @param halfPage the halfPage.
     */
    private static void drawPageSwitcher(@NotNull final ICommandSender sender, int colonyId, int page, int count, int halfPage)
    {
        final int prevPage = Math.max(0, page - 1);
        final int nextPage = Math.min(page + 1, (count / CITIZENS_ON_PAGE) + halfPage);

        final ITextComponent prevButton = new TextComponentString(" <- prev").setStyle(new Style().setBold(true).setColor(TextFormatting.GOLD).setClickEvent(
                new ClickEvent(ClickEvent.Action.RUN_COMMAND, LIST_COMMAND_SUGGESTED + colonyId + prevPage)
        ));
        final ITextComponent nextButton = new TextComponentString("next -> ").setStyle(new Style().setBold(true).setColor(TextFormatting.GOLD).setClickEvent(
                new ClickEvent(ClickEvent.Action.RUN_COMMAND, LIST_COMMAND_SUGGESTED + colonyId + " " + nextPage)
        ));

        final ITextComponent beginLine = new TextComponentString("§2 ----------------");
        final ITextComponent endLine = new TextComponentString("§2---------------- ");
        sender.addChatMessage(beginLine.appendSibling(prevButton).appendSibling(new TextComponentString("§2 | ")).appendSibling(nextButton).appendSibling(endLine));
    }


    @NotNull
    @Override
    public List<String> getTabCompletionOptions(
                                                 @NotNull final MinecraftServer server,
                                                 @NotNull final ICommandSender sender,
                                                 @NotNull final String[] args,
                                                 @Nullable final BlockPos pos)
    {
        return new ArrayList<>();
    }

    @Override
    public boolean isUsernameIndex(@NotNull final String[] args, final int index)
    {
        return false;
    }
}
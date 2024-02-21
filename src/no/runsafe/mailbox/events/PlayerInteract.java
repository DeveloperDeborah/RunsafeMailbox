package no.runsafe.mailbox.events;

import no.runsafe.framework.api.IConfiguration;
import no.runsafe.framework.api.IUniverse;
import no.runsafe.framework.api.block.IBlock;
import no.runsafe.framework.api.event.player.IPlayerInteractEvent;
import no.runsafe.framework.api.event.player.IPlayerRightClickBlock;
import no.runsafe.framework.api.event.plugin.IConfigurationChanged;
import no.runsafe.framework.api.player.IPlayer;
import no.runsafe.framework.minecraft.Item;
import no.runsafe.framework.minecraft.event.player.RunsafePlayerInteractEvent;
import no.runsafe.framework.minecraft.item.meta.RunsafeMeta;
import no.runsafe.mailbox.MailHandler;
import no.runsafe.mailbox.MailboxBlocks;

public class PlayerInteract implements IPlayerInteractEvent, IPlayerRightClickBlock, IConfigurationChanged
{
	public PlayerInteract(MailHandler mailHandler, MailboxBlocks blocks)
	{
		this.mailHandler = mailHandler;
		this.blocks = blocks;
	}

	@Override
	public void OnConfigurationChanged(IConfiguration configuration)
	{
		this.mailUniverse = configuration.getConfigValueAsString("mailUniverse");
	}

	@Override
	public void OnPlayerInteractEvent(RunsafePlayerInteractEvent event)
	{
		IUniverse universe = event.getPlayer().getUniverse();
		if (universe == null || !mailUniverse.equals(universe.getName()))
			return;

		if (event.hasItem())
		{
			RunsafeMeta item = event.getItemStack();
			if (item.is(Item.Decoration.Chest) && item.hasItemMeta())
			{
				String displayName = item.getDisplayName();
				if (displayName != null && displayName.startsWith("Mail Package #"))
				{
					IPlayer player = event.getPlayer();
					String[] split = displayName.split("#");
					this.mailHandler.openPackage(player, Integer.parseInt(split[1]));
					player.getInventory().remove(item);
					event.cancel();
				}
			}
		}
	}

	@Override
	public boolean OnPlayerRightClick(IPlayer player, RunsafeMeta usingItem, IBlock targetBlock)
	{
		if (targetBlock.is(Item.Decoration.Chest) && blocks.isMailboxBlock(targetBlock.getLocation()))
		{
			mailHandler.openMailbox(player);
			return false;
		}
		return true;
	}

	private final MailHandler mailHandler;
	private final MailboxBlocks blocks;
	private String mailUniverse;
}

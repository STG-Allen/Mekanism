package mekanism.common.item;

import java.util.HashSet;
import java.util.Set;

import mekanism.api.Coord4D;
import mekanism.api.EnumColor;
import mekanism.api.IHeatTransfer;
import mekanism.api.MekanismAPI;
import mekanism.api.transmitters.DynamicNetwork;
import mekanism.api.transmitters.IGridTransmitter;
import mekanism.api.transmitters.TransmitterNetworkRegistry;
import mekanism.common.capabilities.Capabilities;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

public class ItemNetworkReader extends ItemEnergized
{
	public static double ENERGY_PER_USE = 400;

	public ItemNetworkReader()
	{
		super(60000);
	}

	@Override
	public boolean onItemUseFirst(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ)
	{
		if(!world.isRemote)
		{
			TileEntity tileEntity = world.getTileEntity(pos);
			boolean drain = !player.capabilities.isCreativeMode;

			if(getEnergy(stack) >= ENERGY_PER_USE && tileEntity != null)
			{
				if(tileEntity.hasCapability(Capabilities.GRID_TRANSMITTER_CAPABILITY, side.getOpposite()))
				{
					if(drain) setEnergy(stack, getEnergy(stack)-ENERGY_PER_USE);
	
					IGridTransmitter transmitter = tileEntity.getCapability(Capabilities.GRID_TRANSMITTER_CAPABILITY, side.getOpposite());
	
					player.addChatMessage(new ChatComponentText(EnumColor.GREY + "------------- " + EnumColor.DARK_BLUE + "[Mekanism]" + EnumColor.GREY + " -------------"));
					player.addChatMessage(new ChatComponentText(EnumColor.GREY + " *Transmitters: " + EnumColor.DARK_GREY + transmitter.getTransmitterNetworkSize()));
					player.addChatMessage(new ChatComponentText(EnumColor.GREY + " *Acceptors: " + EnumColor.DARK_GREY + transmitter.getTransmitterNetworkAcceptorSize()));
					player.addChatMessage(new ChatComponentText(EnumColor.GREY + " *Needed: " + EnumColor.DARK_GREY + transmitter.getTransmitterNetworkNeeded()));
					player.addChatMessage(new ChatComponentText(EnumColor.GREY + " *Buffer: " + EnumColor.DARK_GREY + transmitter.getTransmitterNetworkBuffer()));
					player.addChatMessage(new ChatComponentText(EnumColor.GREY + " *Throughput: " + EnumColor.DARK_GREY + transmitter.getTransmitterNetworkFlow()));
					player.addChatMessage(new ChatComponentText(EnumColor.GREY + " *Capacity: " + EnumColor.DARK_GREY + transmitter.getTransmitterNetworkCapacity()));
					if(transmitter instanceof IHeatTransfer)
						player.addChatMessage(new ChatComponentText(EnumColor.GREY + " *Temperature: " + EnumColor.DARK_GREY + ((IHeatTransfer)transmitter).getTemp() + "K above ambient"));
					player.addChatMessage(new ChatComponentText(EnumColor.GREY + "------------- " + EnumColor.DARK_BLUE + "[=======]" + EnumColor.GREY + " -------------"));
					
					return true;
				}
				else if(tileEntity instanceof IHeatTransfer)
				{
					if(drain) setEnergy(stack, getEnergy(stack)-ENERGY_PER_USE);

					player.addChatMessage(new ChatComponentText(EnumColor.GREY + "------------- " + EnumColor.DARK_BLUE + "[Mekanism]" + EnumColor.GREY + " -------------"));
					player.addChatMessage(new ChatComponentText(EnumColor.GREY + " *Temperature: " + EnumColor.DARK_GREY + ((IHeatTransfer)tileEntity).getTemp() + "K above ambient"));
					player.addChatMessage(new ChatComponentText(EnumColor.GREY + "------------- " + EnumColor.DARK_BLUE + "[=======]" + EnumColor.GREY + " -------------"));

					return true;
				}
				else
				{
					if(drain) setEnergy(stack, getEnergy(stack)-ENERGY_PER_USE);
					
					Set<DynamicNetwork> iteratedNetworks = new HashSet<>();
					
					for(EnumFacing iterSide : EnumFacing.VALUES)
					{
						Coord4D coord = Coord4D.get(tileEntity).offset(iterSide);

						TileEntity tile = coord.getTileEntity(world);
						
						if(tile != null && tile.hasCapability(Capabilities.GRID_TRANSMITTER_CAPABILITY, iterSide.getOpposite()))
						{
							IGridTransmitter transmitter = tile.getCapability(Capabilities.GRID_TRANSMITTER_CAPABILITY, iterSide.getOpposite());
							
							if(transmitter.getTransmitterNetwork().possibleAcceptors.containsKey(coord.offset(iterSide.getOpposite())) && !iteratedNetworks.contains(transmitter.getTransmitterNetwork()))
							{
								player.addChatMessage(new ChatComponentText(EnumColor.GREY + "------------- " + EnumColor.DARK_BLUE + "[" + transmitter.getTransmissionType().getName() + "]" + EnumColor.GREY + " -------------"));
								player.addChatMessage(new ChatComponentText(EnumColor.GREY + " *Connected sides: " + EnumColor.DARK_GREY + transmitter.getTransmitterNetwork().acceptorDirections.get(coord.offset(iterSide.getOpposite()))));
								player.addChatMessage(new ChatComponentText(EnumColor.GREY + "------------- " + EnumColor.DARK_BLUE + "[=======]" + EnumColor.GREY + " -------------"));
								
								iteratedNetworks.add(transmitter.getTransmitterNetwork());
							}
						}
					}
					
					return true;
				}
			}

			if(player.isSneaking() && MekanismAPI.debug)
			{
				String[] strings = TransmitterNetworkRegistry.getInstance().toStrings();
				player.addChatMessage(new ChatComponentText(EnumColor.GREY + "---------- " + EnumColor.DARK_BLUE + "[Mekanism Debug]" + EnumColor.GREY + " ----------"));

				for(String s : strings)
				{
					player.addChatMessage(new ChatComponentText(EnumColor.DARK_GREY + s));
				}

				player.addChatMessage(new ChatComponentText(EnumColor.GREY + "------------- " + EnumColor.DARK_BLUE + "[=======]" + EnumColor.GREY + " -------------"));
			}
		}

		return false;
	}

	@Override
	public boolean canSend(ItemStack itemstack)
	{
		return false;
	}
}

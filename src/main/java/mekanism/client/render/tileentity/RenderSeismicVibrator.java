package mekanism.client.render.tileentity;

import mekanism.client.model.ModelSeismicVibrator;
import mekanism.common.tile.TileEntitySeismicVibrator;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class RenderSeismicVibrator extends TileEntitySpecialRenderer<TileEntitySeismicVibrator>
{
	private ModelSeismicVibrator model = new ModelSeismicVibrator();

	@Override
	public void renderTileEntityAt(TileEntitySeismicVibrator tileEntity, double x, double y, double z, float partialTick, int destroyStage)
	{
		GlStateManager.pushMatrix();
		GlStateManager.translate((float)x + 0.5F, (float)y + 1.5F, (float)z + 1.5F);

		bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "SeismicVibrator" /*+ (tileEntity.isActive ? "On" : "")*/ + ".png"));

		switch(tileEntity.facing.ordinal()) /*TODO: switch the enum*/
		{
			case 2: GlStateManager.rotate(0, 0.0F, 1.0F, 0.0F); break;
			case 3: GlStateManager.rotate(180, 0.0F, 1.0F, 0.0F); break;
			case 4: GlStateManager.rotate(90, 0.0F, 1.0F, 0.0F); break;
			case 5: GlStateManager.rotate(270, 0.0F, 1.0F, 0.0F); break;
		}
		
		float actualRate = (float)Math.sin((tileEntity.clientPiston + (tileEntity.isActive ? partialTick : 0))/5F);

		GlStateManager.rotate(180F, 0.0F, 0.0F, 1.0F);
		model.renderWithPiston(Math.max(0, actualRate), 0.0625F);
		GlStateManager.popMatrix();
	}
}

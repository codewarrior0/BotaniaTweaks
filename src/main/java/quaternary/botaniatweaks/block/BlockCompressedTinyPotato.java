package quaternary.botaniatweaks.block;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.*;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import quaternary.botaniatweaks.BotaniaTweaks;
import quaternary.botaniatweaks.tile.TileCompressedTinyPotato;
import quaternary.botaniatweaks.util.MathUtil;
import vazkii.botania.common.core.BotaniaCreativeTab;

import javax.annotation.Nullable;
import java.util.List;

public class BlockCompressedTinyPotato extends Block {
	public final int compressionLevel;
	final int potatoCount;
	final AxisAlignedBB aabb;
	
	public static final PropertyEnum<EnumFacing> FACING = PropertyEnum.create("facing", EnumFacing.class, EnumFacing.HORIZONTALS);
	
	public static double compressionLevelToRadius(int compressionLevel) {
		return MathUtil.rangeRemap(compressionLevel, 0, 8, 2/16d, .5d);
	}
	
	public BlockCompressedTinyPotato(int compressionLevel) {
		super(Material.CLOTH);
		
		if(compressionLevel <= 0 || compressionLevel >= 9) throw new IllegalArgumentException("Only 8 potato compression levels are allowed");
		
		this.compressionLevel = compressionLevel;
		potatoCount = (int) Math.pow(9, compressionLevel);
		
		double radius = compressionLevelToRadius(compressionLevel);
		double height = radius * 3;
		
		aabb = new AxisAlignedBB(.5 - radius, 0, .5 - radius, .5 + radius, height, .5 + radius);
		
		setRegistryName(new ResourceLocation(BotaniaTweaks.MODID, "compressed_tiny_potato_" + compressionLevel));
		setUnlocalizedName(BotaniaTweaks.MODID + ".compressedpotato." + compressionLevel);
		
		setHardness(0.25f * (1 + compressionLevel));
		
		setCreativeTab(BotaniaCreativeTab.INSTANCE);
		
		setDefaultState(getDefaultState().withProperty(FACING, EnumFacing.NORTH));
	}
	
	@Override
	public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
		String nicePotatoCount = String.format("%,d", potatoCount);
		tooltip.add(I18n.translateToLocalFormatted("botania_tweaks.compressedpotato.tooltip", nicePotatoCount));
	}
	
	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
		TileEntity tile = world.getTileEntity(pos);
		
		if(tile instanceof TileCompressedTinyPotato) {
			((TileCompressedTinyPotato)tile).interact(player, hand, player.getHeldItem(hand), side);
			
			double x = pos.getX() + MathUtil.rangeRemap(world.rand.nextDouble(), 0, 1, aabb.minX, aabb.maxX);
			double y = pos.getY() + aabb.maxY + (double) 3 / 16;
			double z = pos.getZ() + MathUtil.rangeRemap(world.rand.nextDouble(), 0, 1, aabb.minZ, aabb.maxZ);
			world.spawnParticle(EnumParticleTypes.HEART, x, y, z, 0, 0, 0);
		}
		
		return true;
	}
	
	@Override
	public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
		if(stack.hasDisplayName()) {
			TileEntity te = world.getTileEntity(pos);
			if(te instanceof TileCompressedTinyPotato) {
				((TileCompressedTinyPotato) te).name = stack.getDisplayName();
			}
		}
	}
	
	//Tile
	@Override
	public boolean hasTileEntity(IBlockState state) {
		return true;
	}
	
	@Nullable
	@Override
	public TileEntity createTileEntity(World world, IBlockState state) {
		return new TileCompressedTinyPotato(compressionLevel);
	}
	
	//Render
	@Override
	public EnumBlockRenderType getRenderType(IBlockState state) {
		return EnumBlockRenderType.ENTITYBLOCK_ANIMATED;
	}
	
	@Override
	public boolean isOpaqueCube(IBlockState state) {
		return false;
	}
	
	@Override
	public boolean isFullCube(IBlockState state) {
		return false;
	}
	
	//State
	@Override
	public IBlockState getStateFromMeta(int meta) {
		return getDefaultState().withProperty(FACING, EnumFacing.getHorizontal(meta));
	}
	
	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(FACING).getHorizontalIndex();
	}
	
	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, FACING);
	}
	
	@Override
	public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
		return getDefaultState().withProperty(FACING, placer.getHorizontalFacing().getOpposite());
	}
	
	//Etc
	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
		return aabb;
	}
	
	@Override
	public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn, IBlockState state, BlockPos pos, EnumFacing face) {
		if(compressionLevel != 8) return BlockFaceShape.UNDEFINED;
		else return face == EnumFacing.UP ? BlockFaceShape.UNDEFINED : BlockFaceShape.SOLID;
	}
}
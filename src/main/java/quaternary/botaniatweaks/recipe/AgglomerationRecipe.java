package quaternary.botaniatweaks.recipe;

import com.google.common.collect.ImmutableList;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.oredict.OreDictionary;

import javax.annotation.Nullable;
import java.util.List;

public class AgglomerationRecipe {
	final ImmutableList<ItemStack> recipeStacks;
	final ImmutableList<String> recipeOreKeys;
	final ItemStack recipeOutput;
	final int manaCost;
	final int color1;
	final int color2;
	final IBlockState multiblockCenter;
	final IBlockState multiblockEdge;
	final IBlockState multiblockCorner;
	@Nullable public final IBlockState multiblockCenterReplace;
	@Nullable public final IBlockState multiblockEdgeReplace;
	@Nullable public final IBlockState multiblockCornerReplace;
	
	final int totalInputs;
	
	private void verifyInputs(ImmutableList<Object> inputs) {
		if(inputs.isEmpty()) throw new IllegalArgumentException("Can't make empty agglomeration recipe");
		
		for(Object o : inputs) {
			if(o instanceof ItemStack || o instanceof String) continue;
			throw new IllegalArgumentException("illegal recipe input " + o);
		}
	}
	
	public AgglomerationRecipe(ImmutableList<Object> recipeInputs, ItemStack recipeOutput, int manaCost, int color1, int color2, IBlockState multiblockCenter, IBlockState multiblockEdge, IBlockState multiblockCorner, @Nullable IBlockState multiblockCenterReplace, @Nullable IBlockState multiblockEdgeReplace, @Nullable IBlockState multiblockCornerReplace) {
		verifyInputs(recipeInputs);
		
		ImmutableList.Builder<ItemStack> stackInputBuilder = new ImmutableList.Builder<>();
		ImmutableList.Builder<String> keyInputBuilder = new ImmutableList.Builder<>();
		
		for(Object o : recipeInputs) {
			if(o instanceof ItemStack) stackInputBuilder.add((ItemStack)o);
			else keyInputBuilder.add((String) o);
		}
		
		this.recipeStacks = stackInputBuilder.build();
		this.recipeOreKeys = keyInputBuilder.build();
		this.totalInputs = recipeStacks.size() + recipeOreKeys.size();
		
		this.recipeOutput = recipeOutput;
		this.manaCost = manaCost;
		this.color1 = color1;
		this.color2 = color2;
		
		this.multiblockCenter = multiblockCenter;
		this.multiblockEdge = multiblockEdge;
		this.multiblockCorner = multiblockCorner;
		
		this.multiblockCenterReplace = multiblockCenterReplace;
		this.multiblockEdgeReplace = multiblockEdgeReplace;
		this.multiblockCornerReplace = multiblockCornerReplace;
	}
	
	/////
	
	public boolean matches(World w, BlockPos platePos, List<ItemStack> inputs) {
		return multiblockMatches(w, platePos) && itemsMatch(inputs);
	}
	
	public boolean itemsMatch(List<ItemStack> inputs) {
		int matches = 0;
		
		//Check stack inputs
		for(ItemStack recipeStack : recipeStacks) {
			for(ItemStack inputStack : inputs) {
				if(ItemHandlerHelper.canItemStacksStack(recipeStack, inputStack)) {
					matches++;
					break; //try the next input stack
				}
			}
		}
		
		if(matches == totalInputs) return true;
		
		//Check recipe inputs that are ore dictionary keys
		for(String oreKey : recipeOreKeys) {
			NonNullList<ItemStack> ores = OreDictionary.getOres(oreKey);
			
			for(ItemStack inputStack : inputs) {
				if(inputStack.getCount() != 1) continue;
				
				for(ItemStack ore : ores) {
					if(ItemHandlerHelper.canItemStacksStack(ore, inputStack)) {
						matches++;
						break; //try the next input stack
					}
				}
			}
		}
		
		return matches == totalInputs;
	}
	
	public boolean multiblockMatches(World w, BlockPos platePos) {
		BlockPos multiblockPos = platePos.down();
		if(!w.getBlockState(multiblockPos).equals(multiblockCenter)) return false;
		for(EnumFacing nesw : EnumFacing.HORIZONTALS) {
			BlockPos horizOffset = multiblockPos.offset(nesw);
			if(!w.getBlockState(horizOffset).equals(multiblockEdge)) return false;
			
			BlockPos cornerOffset = horizOffset.offset(nesw.rotateY());
			if(!w.getBlockState(cornerOffset).equals(multiblockCorner)) return false;
		}
		
		return true;
	}
	
	//// Yeet
	
	
	public ImmutableList<ItemStack> getRecipeStacks() {
		return recipeStacks;
	}
	
	public ImmutableList<String> getRecipeOreKeys() {
		return recipeOreKeys;
	}
	
	public int getManaCost() {
		return manaCost;
	}
	
	public ItemStack getRecipeOutputCopy() {
		return recipeOutput.copy();
	}
	
	////
	
	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof AgglomerationRecipe)) return false;
		
		return false; //TODO
	}
}

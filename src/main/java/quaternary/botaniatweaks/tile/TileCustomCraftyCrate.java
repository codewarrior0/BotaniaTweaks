package quaternary.botaniatweaks.tile;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import vazkii.botania.api.mana.IManaReceiver;
import vazkii.botania.common.block.tile.TileCraftCrate;
import vazkii.botania.common.item.ModItems;

import javax.annotation.Nonnull;

public class TileCustomCraftyCrate extends TileCraftCrate implements IManaReceiver {
	/////// mana stuff
	int mana = 0;
	int MANA_PER_ITEM = 100;
	int MAX_MANA = MANA_PER_ITEM * 20;
	
	//Watch out for the name clash, TileCraftCrate and IManaReceiver both have an isFull(V)Z
	@Override
	public boolean isFull() {
		return mana >= MAX_MANA;
	}
	
	@Override
	public void recieveMana(int m) {
		mana += m;
	}
	
	@Override
	public boolean canRecieveManaFromBursts() {
		return !isFull();
	}
	
	@Override
	public int getCurrentMana() {
		return mana;
	}
	
	////////////////////////// crate stuff
	//Basically there's 2 tweaks and then 1000 lines of copypasted class since stuff was either private or referred
	
	int mySignal = 0; //Copypaste of private field "signal"
	
	@Override
	//Copypaste of method "update()V" to point towards the new isFull and craft methods
	public void update() {
		if (world.isRemote)
			return;
		
		if(canEject() && isCrateFull() && doCraft(true))
			doEjectAll();
		
		int newSignal = 0;
		for(; newSignal < 9; newSignal++) // dis for loop be derpy
			if(!isLocked(newSignal) && itemHandler.getStackInSlot(newSignal).isEmpty())
				break;
		
		if(newSignal != mySignal) {
			mySignal = newSignal;
			world.updateComparatorOutputLevel(pos, world.getBlockState(pos).getBlock());
		}
	}
	
	//Override-but-it's-private method "craft(Z)Z", changes noted
	protected boolean doCraft(boolean fullCheck) {
		if(fullCheck && !isCrateFull())
			return false;
		
		InventoryCrafting craft = new InventoryCrafting(new Container() {
			@Override
			public boolean canInteractWith(@Nonnull EntityPlayer player) {
				return false;
			}
		}, 3, 3);
		
		//Tweak: test mana
		int recipeItems = 0;
		
		for(int i = 0; i < 9; i++) {
			ItemStack stack = itemHandler.getStackInSlot(i);
			
			if(stack.isEmpty() || isLocked(i) || stack.getItem() == ModItems.manaResource && stack.getItemDamage() == 11)
				continue;
			
			recipeItems++; //Cont. tweak: test mana
			craft.setInventorySlotContents(i, stack.copy());
		}
		
		//Cont. tweak: test mana
		if(recipeItems * MANA_PER_ITEM > mana) {
			return false;
		}
		
		for(IRecipe recipe : ForgeRegistries.RECIPES)
			if(recipe.matches(craft, world)) {
				itemHandler.setStackInSlot(9, recipe.getCraftingResult(craft));
				
				for(int i = 0; i < 9; i++) {
					ItemStack stack = itemHandler.getStackInSlot(i);
					if(stack.isEmpty())
						continue;
					
					ItemStack container = stack.getItem().getContainerItem(stack);
					itemHandler.setStackInSlot(i, container);
				}
				return true;
			}
		
		return false;
	}
	
	//Override-but-it's-private private method "ejectAll()V"
	protected void doEjectAll() {
		//Tweak: use mana
		mana = 0;
		
		//"super.ejectAll();"
		for(int i = 0; i < getSizeInventory(); ++i) {
			ItemStack stack = itemHandler.getStackInSlot(i);
			if(!stack.isEmpty())
				eject(stack, false);
			itemHandler.setStackInSlot(i, ItemStack.EMPTY);
		}
		markDirty();
	}
	
	//copypaste of method "isFull()Z" which name-clashes with the IManaReceiver "isFull()Z"
	boolean isCrateFull() {
		for(int i = 0; i < 9; i++)
			if(!isLocked(i) && itemHandler.getStackInSlot(i).isEmpty())
				return false;
		
		return true;
	}
	
	//Override getSignal to return my copy of the private variable >.>
	@Override
	public int getSignal() {
		return mySignal;
	}
	
	//Override onWanded to point to the new craft and ejectAll
	@Override
	public boolean onWanded(World world, EntityPlayer player, ItemStack stack) {
		if(!world.isRemote && canEject()) {
			doCraft(false);
			doEjectAll();
		}
		return true;
	}
	
	//Save mana to NBT
	@Override
	public void writePacketNBT(NBTTagCompound nbt) {
		nbt.setInteger("Mana", mana);
		super.writePacketNBT(nbt);
	}
	
	@Override
	public void readPacketNBT(NBTTagCompound nbt) {
		super.readPacketNBT(nbt);
		mana = nbt.getInteger("Mana");
	}
}

package xyz.wagyourtail.jsmacros.client.api.classes.inventory;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.StonecutterScreen;

import xyz.wagyourtail.jsmacros.client.api.helpers.item.ItemStackHelper;

import java.util.List;

/**
 * @author Etheradon
 * @since 1.8.4
 */
@SuppressWarnings("unused")
public class StoneCutterInventory extends Inventory<StonecutterScreen> {

    public StoneCutterInventory(StonecutterScreen inventory) {
        super(inventory);
    }

    /**
     * @return the selected recipe index.
     *
     * @since 1.8.4
     */
    public int getSelectedRecipeIndex() {
        return inventory.getScreenHandler().getSelectedRecipe();
    }

    /**
     * @return the output item for the selected recipe.
     *
     * @since 1.8.4
     */
    public ItemStackHelper getOutput() {
        return getSlot(1);
    }

    /**
     * @param idx the index to select
     * @since 1.8.4
     */
    public void selectRecipe(int idx) {
        if (idx >= 0 && idx < inventory.getScreenHandler().getAvailableRecipeCount()) {
            inventory.getScreenHandler().onButtonClick(mc.player, idx);
            MinecraftClient.getInstance().interactionManager.clickButton(getCurrentSyncId(), idx);
        }
    }

    /**
     * @return the amount of available recipes.
     *
     * @since 1.8.4
     */
    public int getAvailableRecipeCount() {
        return inventory.getScreenHandler().getAvailableRecipeCount();
    }

    /**
     * @return a list of all available recipe results in the form of item stacks.
     *
     * @since 1.8.4
     */
    public List<ItemStackHelper> getRecipes() {
        return inventory.getScreenHandler().getAvailableRecipes().stream().map(recipe -> new ItemStackHelper(recipe.getOutput())).toList();
    }

    /**
     * @return {@code true} if there is a selected recipe, {@code false} otherwise.
     *
     * @since 1.8.4
     */
    public boolean canCraft() {
        return inventory.getScreenHandler().canCraft();
    }

    @Override
    public String toString() {
        return String.format("StoneCutterInventory:{}");
    }

}

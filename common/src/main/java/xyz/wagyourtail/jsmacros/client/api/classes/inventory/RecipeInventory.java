package xyz.wagyourtail.jsmacros.client.api.classes.inventory;

import net.minecraft.client.gui.screen.ingame.AbstractFurnaceScreen;
import net.minecraft.client.gui.screen.ingame.CraftingScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.screen.recipebook.RecipeBookResults;
import net.minecraft.client.gui.screen.recipebook.RecipeBookWidget;
import net.minecraft.client.gui.screen.recipebook.RecipeResultCollection;
import net.minecraft.recipe.Recipe;
import net.minecraft.screen.AbstractRecipeScreenHandler;

import org.jetbrains.annotations.Nullable;
import xyz.wagyourtail.jsmacros.client.access.IRecipeBookResults;
import xyz.wagyourtail.jsmacros.client.access.IRecipeBookWidget;
import xyz.wagyourtail.jsmacros.client.api.helpers.ItemStackHelper;
import xyz.wagyourtail.jsmacros.client.api.helpers.RecipeHelper;
import xyz.wagyourtail.jsmacros.core.Core;

import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Etheradon
 * @since 1.8.4
 */
@SuppressWarnings("unused")
public abstract class RecipeInventory<T extends HandledScreen<? extends AbstractRecipeScreenHandler<?>>> extends Inventory<T> {

    private final AbstractRecipeScreenHandler<?> handler;

    protected RecipeInventory(T inventory) {
        super(inventory);
        this.handler = inventory.getScreenHandler();
    }

    /**
     * @return the output item.
     *
     * @since 1.8.4
     */
    public ItemStackHelper getOutput() {
        return getSlot(handler.getCraftingResultSlotIndex());
    }

    /**
     * @return the maximum input size for all recipes in this inventory.
     *
     * @since 1.8.4
     */
    public int getInputSize() {
        return getCraftingHeight() * getCraftingWidth();
    }

    /**
     * @param x the x position of the input slot, starting at 0, left to right. Must be less than
     *          {@link #getCraftingWidth()}
     * @param z the z position of the input slot, starting at 0, top to bottom. Must be less than
     *          {@link #getCraftingHeight()}
     * @return the input item at the given position.
     *
     * @since 1.8.4
     */
    public abstract ItemStackHelper getInput(int x, int z);

    /**
     * @return the input items of the crafting grid, in a 2d array.
     *
     * @since 1.8.4
     */
    public ItemStackHelper[][] getInput() {
        ItemStackHelper[][] input = new ItemStackHelper[getCraftingWidth()][getCraftingHeight()];
        for (int x = 0; x < getCraftingWidth(); x++) {
            for (int z = 0; z < getCraftingHeight(); z++) {
                input[x][z] = getInput(x, z);
            }
        }
        return input;
    }

    /**
     * @return the width of the crafting grid.
     *
     * @since 1.8.4
     */
    public int getCraftingWidth() {
        return handler.getCraftingWidth();
    }

    /**
     * @return the height of the crafting grid.
     *
     * @since 1.8.4
     */
    public int getCraftingHeight() {
        return handler.getCraftingHeight();
    }

    /**
     * @return the amount of slots used for crafting.
     *
     * @since 1.8.4
     */
    public int getCraftingSlotCount() {
        return handler.getCraftingSlotCount();
    }

    /**
     * @return the recipe category of recipes that can be crafted in this inventory.
     *
     * @since 1.8.4
     */
    public String getCategory() {
        return switch (handler.getCategory()) {
            case CRAFTING -> "CRAFTING";
            case FURNACE -> "FURNACE";
            case BLAST_FURNACE -> "BLAST_FURNACE";
            case SMOKER -> "SMOKER";
        };
    }

    /**
     * @return
     *
     * @throws InterruptedException
     * @since 1.3.1
     */
    public List<RecipeHelper> getCraftableRecipes() throws InterruptedException {
        return getRecipes(true);
    }

    /**
     * @param craftable whether only to list craftable recipes
     * @return a list of recipes that can be crafted in this inventory.
     *
     * @throws InterruptedException
     * @since 1.8.4
     */
    public List<RecipeHelper> getRecipes(boolean craftable) throws InterruptedException {
        Stream<Recipe<?>> recipes;
        RecipeBookResults res;
        IRecipeBookWidget recipeBookWidget = getRecipeBookWidget();
        if (recipeBookWidget == null) {
            return null;
        }
        if (Core.getInstance().profile.checkJoinedThreadStack()) {
            if (mc.currentScreen != inventory) {
                ((RecipeBookWidget) recipeBookWidget).initialize(0, 0, mc, true, (AbstractRecipeScreenHandler<?>) handler);
            }
            if (!((RecipeBookWidget) recipeBookWidget).isOpen()) {
                ((RecipeBookWidget) recipeBookWidget).reset();
            }
            try {
                recipeBookWidget.jsmacros_refreshResultList();
            } catch (Throwable t) {
                t.printStackTrace();
                throw new RuntimeException("refreshing the recipe list threw an error", t);
            }
        } else {
            Semaphore lock = new Semaphore(0);
            Throwable[] t = new Throwable[1];
            mc.execute(() -> {
                try {
                    if (mc.currentScreen != inventory) {
                        ((RecipeBookWidget) recipeBookWidget).initialize(0, 0, mc, true, (AbstractRecipeScreenHandler<?>) handler);
                    }
                    if (!((RecipeBookWidget) recipeBookWidget).isOpen()) {
                        ((RecipeBookWidget) recipeBookWidget).reset();
                    }
                    recipeBookWidget.jsmacros_refreshResultList();
                } catch (Throwable e) {
                    t[0] = e;
                } finally {
                    lock.release();
                }
            });
            lock.acquire();
            if (t[0] != null) {
                t[0].printStackTrace();
                throw new RuntimeException("refreshing the recipe list threw an error", t[0]);
            }
        }
        res = recipeBookWidget.jsmacros_getResults();
        List<RecipeResultCollection> result = ((IRecipeBookResults) res).jsmacros_getResultCollections();
        recipes = result.stream().flatMap(e -> e.getRecipes(craftable).stream());
        return recipes.map(e -> new RecipeHelper(e, syncId)).collect(Collectors.toList());
    }

    @Nullable
    private IRecipeBookWidget getRecipeBookWidget() {
        IRecipeBookWidget recipeBookWidget;
        if (inventory instanceof CraftingScreen) {
            recipeBookWidget = (IRecipeBookWidget) ((CraftingScreen) inventory).getRecipeBookWidget();
        } else if (inventory instanceof InventoryScreen) {
            recipeBookWidget = (IRecipeBookWidget) ((InventoryScreen) inventory).getRecipeBookWidget();
        } else if (inventory instanceof AbstractFurnaceScreen) {
            recipeBookWidget = (IRecipeBookWidget) ((AbstractFurnaceScreen<?>) inventory).getRecipeBookWidget();
        } else {
            return null;
        }
        return recipeBookWidget;
    }

    /**
     * @return {@code true} if the recipe book is visible, {@code false} otherwise.
     *
     * @since 1.8.4
     */
    public boolean isRecipeBookOpened() {
        IRecipeBookWidget recipeBookWidget = getRecipeBookWidget();
        if (recipeBookWidget == null) {
            return false;
        }
        return ((RecipeBookWidget) recipeBookWidget).isOpen();
    }

    /**
     * @since 1.8.4
     */
    public void toggleRecipeBook() {
        IRecipeBookWidget recipeBookWidget = getRecipeBookWidget();
        if (recipeBookWidget == null) {
            return;
        }
        ((RecipeBookWidget) recipeBookWidget).toggleOpen();
    }

    /**
     * @param open whether to open or close the recipe book
     * @since 1.8.4
     */
    public void setRecipeBook(boolean open) {
        IRecipeBookWidget recipeBookWidget = getRecipeBookWidget();
        if (recipeBookWidget != null) {
            RecipeBookWidget rbw = (RecipeBookWidget) recipeBookWidget;
            if (rbw.isOpen() != open) {
                rbw.toggleOpen();
            }
        }
    }

}

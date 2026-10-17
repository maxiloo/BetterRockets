package max.test.betterrockets.custom.item;

import max.betterrockets.ModComponents;
import max.betterrockets.ModItems;
import max.betterrockets.item.custom.RechargeableFireworkItem;
import net.minecraft.SharedConstants;
import net.minecraft.core.component.DataComponentInitializers;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.registries.VanillaRegistries;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.Fireworks;
import net.minecraft.world.level.Level;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class RechargeableFireworkItemTest {

    private RechargeableFireworkItem rechargeableFireworkItem;
    private ItemStack fireworkStack;
    private ItemStack rechargeableFireworkStack;
    private Player player;
    private Slot slot;
    private Level world;

    private static boolean componentsBound = false;

    /**
     * Since Minecraft 26.1, item components are only bound when a world/datapacks load.
     * In unit tests there is no world, so we bind them manually (once) – otherwise
     * {@code new ItemStack(...)} throws "Components not bound yet".
     */
    private static void bindItemComponents() {
        if (componentsBound) return;
        // Make sure our own items/components are registered BEFORE binding
        ModComponents.ROCKETS_LOADED.toString();
        ModItems.RECHARGEABLE_FIREWORK.toString();

        BuiltInRegistries.DATA_COMPONENT_INITIALIZERS
                .build(VanillaRegistries.createLookup())
                .forEach(DataComponentInitializers.PendingComponents::apply);
        componentsBound = true;
    }

    @BeforeEach
    void setUp() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        bindItemComponents();

        rechargeableFireworkItem = (RechargeableFireworkItem) spy(ModItems.RECHARGEABLE_FIREWORK);
        fireworkStack = new ItemStack(Items.FIREWORK_ROCKET, 64);
        rechargeableFireworkStack = new ItemStack(rechargeableFireworkItem);
        player = mock(Player.class);
        slot = mock(Slot.class);
        world = mock(Level.class);

        doNothing().when(rechargeableFireworkItem).playInsertSound(isA(Player.class));
        doNothing().when(rechargeableFireworkItem).playEmptySound(isA(Level.class), isA(Player.class));
        doNothing().when(rechargeableFireworkItem).spawnFireworkEntity(any(Level.class), any(Player.class), any(ItemStack.class));

        when(world.isClientSide()).thenReturn(false);
    }

    @Test
    @DisplayName("Test successful rocket bundle reload")
    void testOnClicked_successful() {
        // When
        boolean result = rechargeableFireworkItem.overrideOtherStackedOnMe(rechargeableFireworkStack, fireworkStack, slot, ClickAction.PRIMARY, player, null);

        // Then
        assertTrue(result, "overrideOtherStackedOnMe should return true when loading fireworks");
        assertEquals(0, fireworkStack.getCount(), "the firework stack count should be zero");
        assertEquals(64, rechargeableFireworkStack.getOrDefault(ModComponents.ROCKETS_LOADED, 0), "the rocket bundle should be set to 64");

        // Verify
        verify(rechargeableFireworkItem).playInsertSound(any(Player.class));
    }

    @Test
    @DisplayName("Test successful rocket bundle reload with flight duration 3")
    void testOnClicked_fireworkType() {
        // Given
        fireworkStack.set(DataComponents.FIREWORKS, new Fireworks(3, new ArrayList<>()));

        // When
        boolean result = rechargeableFireworkItem.overrideOtherStackedOnMe(rechargeableFireworkStack, fireworkStack, slot, ClickAction.PRIMARY, player, null);

        // Then
        assertTrue(result, "overrideOtherStackedOnMe should return true when clicking on the stack with a the correct firework type");
        assertEquals(3, rechargeableFireworkItem.getFireworkType(rechargeableFireworkStack), "the firework type should be 3");
        assertEquals(64, rechargeableFireworkStack.getOrDefault(ModComponents.ROCKETS_LOADED, 0), "the rocket bundle should increase to 64");
    }

    @Test
    @DisplayName("Test rocket bundle reload fail due to wrong firework type")
    void testOnClicked_falseFireworkType() {
        // Given
        fireworkStack.set(DataComponents.FIREWORKS, new Fireworks(1, new ArrayList<>()));
        rechargeableFireworkItem.overrideOtherStackedOnMe(rechargeableFireworkStack, fireworkStack, slot, ClickAction.PRIMARY, player, null);
        fireworkStack.set(DataComponents.FIREWORKS, new Fireworks(3, new ArrayList<>())); // Different flight duration

        // When
        boolean result = rechargeableFireworkItem.overrideOtherStackedOnMe(rechargeableFireworkStack, fireworkStack, slot, ClickAction.SECONDARY, player, null);

        // Then
        assertFalse(result, "overrideOtherStackedOnMe should return false when clicking on the stack with a different rocket duration");
        assertEquals(1, rechargeableFireworkItem.getFireworkType(rechargeableFireworkStack), "the firework type should still be 1");
        assertEquals(64, rechargeableFireworkStack.getOrDefault(ModComponents.ROCKETS_LOADED, 0), "the rocket bundle should still be 64");
    }

    @Test
    @DisplayName("Test rocket bundle reload fail due to wrong click")
    void testOnClicked_falseClick() {
        // When
        boolean result = rechargeableFireworkItem.overrideOtherStackedOnMe(rechargeableFireworkStack, fireworkStack, slot, ClickAction.SECONDARY, player, null);

        // Then
        assertEquals(64, fireworkStack.getCount(), "the firework stack count should still be 64");
        assertEquals(0, rechargeableFireworkStack.getOrDefault(ModComponents.ROCKETS_LOADED, 0), "the rocket bundle should still be 0");

        // Verify
        verify(rechargeableFireworkItem, never()).playInsertSound(any(Player.class));
    }

    @Test
    @DisplayName("Test rocket bundle reload fail due to false item")
    void testOnClicked_falseItem() {
        // Given
        ItemStack appleStack = new ItemStack(Items.APPLE, 64);

        // When
        boolean result = rechargeableFireworkItem.overrideOtherStackedOnMe(rechargeableFireworkStack, appleStack, slot, ClickAction.PRIMARY, player, null);

        // Then
        assertFalse(result, "overrideOtherStackedOnMe should return false when clicking on the stack with the wrong item");
        assertEquals(64, appleStack.getCount(), "the apple stack count should still be 64");
        assertEquals(0, rechargeableFireworkStack.getOrDefault(ModComponents.ROCKETS_LOADED, 0), "the rocket bundle should still be 0");

        // Verify
        verify(rechargeableFireworkItem, never()).playInsertSound(any(Player.class));
    }

    @Test
    @DisplayName("Test rocket bundle reload fail due to rocket bundle being already at max capacity")
    void testOnClicked_alreadyFull() {
        // Given
        rechargeableFireworkItem.setLoadedFireworks(rechargeableFireworkStack, 512);

        // When
        boolean result = rechargeableFireworkItem.overrideOtherStackedOnMe(rechargeableFireworkStack, fireworkStack, slot, ClickAction.PRIMARY, player, null);

        // Then
        assertFalse(result, "overrideOtherStackedOnMe should return false when clicking on rocket bundle at max. capacity");
        assertEquals(64, fireworkStack.getCount(), "the firework stack count should still be 64");
        assertEquals(512, rechargeableFireworkStack.getOrDefault(ModComponents.ROCKETS_LOADED, 0), "the rocket bundle should still be 512 (max)");

        // Verify
        verify(rechargeableFireworkItem, never()).playInsertSound(any(Player.class));
    }

    @Test
    @DisplayName("Test rocket bundle reload succeed and reach max capacity")
    void testOnClicked_overflow() {
        // Given
        rechargeableFireworkItem.setLoadedFireworks(rechargeableFireworkStack, 500);

        // When
        boolean result = rechargeableFireworkItem.overrideOtherStackedOnMe(rechargeableFireworkStack, fireworkStack, slot, ClickAction.PRIMARY, player, null);

        // Then
        assertTrue(result, "overrideOtherStackedOnMe should return true when left-clicking on rocket bundle below max. capacity");
        assertEquals(52, fireworkStack.getCount(), "the firework stack count should be reduced to 52");
        assertEquals(512, rechargeableFireworkStack.getOrDefault(ModComponents.ROCKETS_LOADED, 0), "the rocket bundle should reach 512 (max)");

        // Verify
        verify(rechargeableFireworkItem).playInsertSound(any(Player.class));
    }

    @Test
    @DisplayName("Test item bar for unused rocket bundle")
    void testIsItemBarVisible_unused() {
        // When
        boolean result = rechargeableFireworkStack.isBarVisible();

        //Then
        assertFalse(result, "unused rocket bundle stack should not show item bar");
    }

    @Test
    @DisplayName("Test item bar for full rocket bundle")
    void testIsItemBarVisible_fullCapacity() {
        // Given
        rechargeableFireworkItem.setLoadedFireworks(rechargeableFireworkStack, 512);

        // When
        boolean result = rechargeableFireworkStack.isBarVisible();

        //Then
        assertFalse(result, "full rocket bundle stack should not show item bar");
    }

    @Test
    @DisplayName("Test item bar for consumed rocket bundle")
    void testIsItemBarVisible_consumed() {
        // Given
        rechargeableFireworkItem.setLoadedFireworks(rechargeableFireworkStack, 11);

        // When
        boolean result = rechargeableFireworkStack.isBarVisible();

        //Then
        assertTrue(result, "consumed rocket bundle stack should not show item bar");
    }

    @Test
    @DisplayName("Test use rocket bundle successfully")
    void testUse_successful() {
        // Mock behavior
        when(player.getItemInHand(InteractionHand.MAIN_HAND)).thenReturn(rechargeableFireworkStack);
        when(player.isFallFlying()).thenReturn(true);

        // When
        rechargeableFireworkItem.setLoadedFireworks(rechargeableFireworkStack, 11);
        InteractionResult actionResult = rechargeableFireworkItem.use(world, player, InteractionHand.MAIN_HAND);

        // Then
        assertEquals(InteractionResult.SUCCESS, actionResult, "successful use of rocket bundle should return successful ActionResult");
        assertEquals(10, rechargeableFireworkItem.getLoadedFireworks(rechargeableFireworkStack), "consumption should reduce number of loaded fireworks");

        // Verify
        verify(rechargeableFireworkItem, never()).playEmptySound(any(Level.class), any(Player.class));
        verify(rechargeableFireworkItem).spawnFireworkEntity(any(Level.class), any(Player.class), any(ItemStack.class));
    }

    @Test
    @DisplayName("Test use rocket bundle fail because it is empty")
    void testUse_empty() {
        // Mock behavior
        when(player.getItemInHand(InteractionHand.MAIN_HAND)).thenReturn(rechargeableFireworkStack);
        when(player.isFallFlying()).thenReturn(true);

        // When
        rechargeableFireworkItem.setLoadedFireworks(rechargeableFireworkStack, 0);
        InteractionResult  actionResult = rechargeableFireworkItem.use(world, player, InteractionHand.MAIN_HAND);

        // Then
        assertEquals(InteractionResult.FAIL, actionResult, "unsuccessful use of rocket bundle should return fail ActionResult");
        assertEquals(0, rechargeableFireworkItem.getLoadedFireworks(rechargeableFireworkStack), "consumption should not reduce number of loaded fireworks");

        // Verify
        verify(rechargeableFireworkItem).playEmptySound(any(Level.class), any(Player.class));
        verify(rechargeableFireworkItem, never()).spawnFireworkEntity(any(Level.class), any(Player.class), any(ItemStack.class));
    }

    @Test
    @DisplayName("Test use rocket bundle fail because player is not flying")
    void testUse_notFlying() {
        // Mock behavior
        when(player.getItemInHand(InteractionHand.MAIN_HAND)).thenReturn(rechargeableFireworkStack);
        when(player.isFallFlying()).thenReturn(false);

        // When
        rechargeableFireworkItem.setLoadedFireworks(rechargeableFireworkStack, 20);
        InteractionResult actionResult = rechargeableFireworkItem.use(world, player, InteractionHand.MAIN_HAND);

        // Then
        assertEquals(InteractionResult.FAIL, actionResult, "unsuccessful use of rocket bundle should return fail ActionResult");
        assertEquals(20, rechargeableFireworkItem.getLoadedFireworks(rechargeableFireworkStack), "consumption should not reduce number of loaded fireworks");

        // Verify
        verify(rechargeableFireworkItem, never()).spawnFireworkEntity(any(Level.class), any(Player.class), any(ItemStack.class));
    }

}

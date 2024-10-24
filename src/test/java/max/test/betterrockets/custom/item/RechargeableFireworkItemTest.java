package max.test.betterrockets.custom.item;

import max.betterrockets.ModComponents;
import max.betterrockets.ModItems;
import max.betterrockets.item.custom.RechargeableFireworkItem;
import net.minecraft.Bootstrap;
import net.minecraft.SharedConstants;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.FireworksComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ClickType;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
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
    private PlayerEntity player;
    private Slot slot;
    private World world;

    @BeforeEach
    void setUp() {
        SharedConstants.createGameVersion();
        Bootstrap.initialize();

        rechargeableFireworkItem = (RechargeableFireworkItem) spy(ModItems.RECHARGEABLE_FIREWORK);
        fireworkStack = new ItemStack(Items.FIREWORK_ROCKET, 64);
        rechargeableFireworkStack = new ItemStack(rechargeableFireworkItem);
        player = mock(PlayerEntity.class);
        slot = mock(Slot.class);
        world = mock(World.class);

        doNothing().when(rechargeableFireworkItem).playInsertSound(isA(PlayerEntity.class));
        doNothing().when(rechargeableFireworkItem).playEmptySound(isA(World.class), isA(PlayerEntity.class));
        doNothing().when(rechargeableFireworkItem).spawnFireworkEntity(any(World.class), any(PlayerEntity.class), any(ItemStack.class));

        when(world.isClient()).thenReturn(false);
    }

    @Test
    @DisplayName("Test successful rocket bundle reload")
    void testOnClicked_successful() {
        // When
        boolean result = rechargeableFireworkItem.onClicked(rechargeableFireworkStack, fireworkStack, slot, ClickType.RIGHT, player, null);

        // Then
        assertTrue(result, "onClicked should return true when loading fireworks");
        assertEquals(0, fireworkStack.getCount(), "the firework stack count should be zero");
        assertEquals(64, rechargeableFireworkStack.getOrDefault(ModComponents.ROCKETS_LOADED, 0), "the rocket bundle should be set to 64");

        // Verify
        verify(rechargeableFireworkItem).playInsertSound(any(PlayerEntity.class));
    }

    @Test
    @DisplayName("Test successful rocket bundle reload with flight duration 3")
    void testOnClicked_fireworkType() {
        // Given
        fireworkStack.set(DataComponentTypes.FIREWORKS, new FireworksComponent(3, new ArrayList<>()));

        // When
        boolean result = rechargeableFireworkItem.onClicked(rechargeableFireworkStack, fireworkStack, slot, ClickType.RIGHT, player, null);

        // Then
        assertTrue(result, "onClicked should return true when clicking on the stack with a the correct firework type");
        assertEquals(3, rechargeableFireworkItem.getFireworkType(rechargeableFireworkStack), "the firework type should be 3");
        assertEquals(64, rechargeableFireworkStack.getOrDefault(ModComponents.ROCKETS_LOADED, 0), "the rocket bundle should increase to 64");
    }

    @Test
    @DisplayName("Test rocket bundle reload fail due to wrong firework type")
    void testOnClicked_falseFireworkType() {
        // Given
        fireworkStack.set(DataComponentTypes.FIREWORKS, new FireworksComponent(1, new ArrayList<>()));
        rechargeableFireworkItem.onClicked(rechargeableFireworkStack, fireworkStack, slot, ClickType.RIGHT, player, null);
        fireworkStack.set(DataComponentTypes.FIREWORKS, new FireworksComponent(3, new ArrayList<>())); // Different flight duration

        // When
        boolean result = rechargeableFireworkItem.onClicked(rechargeableFireworkStack, fireworkStack, slot, ClickType.RIGHT, player, null);

        // Then
        assertFalse(result, "onClicked should return false when clicking on the stack with a different rocket duration");
        assertEquals(1, rechargeableFireworkItem.getFireworkType(rechargeableFireworkStack), "the firework type should still be 1");
        assertEquals(64, rechargeableFireworkStack.getOrDefault(ModComponents.ROCKETS_LOADED, 0), "the rocket bundle should still be 64");
    }

    @Test
    @DisplayName("Test rocket bundle reload fail due to wrong click")
    void testOnClicked_falseClick() {
        // When
        boolean result = rechargeableFireworkItem.onClicked(rechargeableFireworkStack, fireworkStack, slot, ClickType.LEFT, player, null);

        // Then
        assertFalse(result, "onClicked should return false when clicking on the stack with ClickType.LEFT");
        assertEquals(64, fireworkStack.getCount(), "the firework stack count should still be 64");
        assertEquals(0, rechargeableFireworkStack.getOrDefault(ModComponents.ROCKETS_LOADED, 0), "the rocket bundle should still be 0");

        // Verify
        verify(rechargeableFireworkItem, never()).playInsertSound(any(PlayerEntity.class));
    }

    @Test
    @DisplayName("Test rocket bundle reload fail due to false item")
    void testOnClicked_falseItem() {
        // Given
        ItemStack appleStack = new ItemStack(Items.APPLE, 64);

        // When
        boolean result = rechargeableFireworkItem.onClicked(rechargeableFireworkStack, appleStack, slot, ClickType.RIGHT, player, null);

        // Then
        assertFalse(result, "onClicked should return false when clicking on the stack with the wrong item");
        assertEquals(64, appleStack.getCount(), "the apple stack count should still be 64");
        assertEquals(0, rechargeableFireworkStack.getOrDefault(ModComponents.ROCKETS_LOADED, 0), "the rocket bundle should still be 0");

        // Verify
        verify(rechargeableFireworkItem, never()).playInsertSound(any(PlayerEntity.class));
    }

    @Test
    @DisplayName("Test rocket bundle reload fail due to rocket bundle being already at max capacity")
    void testOnClicked_alreadyFull() {
        // Given
        rechargeableFireworkItem.setLoadedFireworks(rechargeableFireworkStack, 512);

        // When
        boolean result = rechargeableFireworkItem.onClicked(rechargeableFireworkStack, fireworkStack, slot, ClickType.RIGHT, player, null);

        // Then
        assertFalse(result, "onClicked should return false when clicking on rocket bundle at max. capacity");
        assertEquals(64, fireworkStack.getCount(), "the firework stack count should still be 64");
        assertEquals(512, rechargeableFireworkStack.getOrDefault(ModComponents.ROCKETS_LOADED, 0), "the rocket bundle should still be 512 (max)");

        // Verify
        verify(rechargeableFireworkItem, never()).playInsertSound(any(PlayerEntity.class));
    }

    @Test
    @DisplayName("Test rocket bundle reload succeed and reach max capacity")
    void testOnClicked_overflow() {
        // Given
        rechargeableFireworkItem.setLoadedFireworks(rechargeableFireworkStack, 500);

        // When
        boolean result = rechargeableFireworkItem.onClicked(rechargeableFireworkStack, fireworkStack, slot, ClickType.RIGHT, player, null);

        // Then
        assertTrue(result, "onClicked should return true when clicking on rocket bundle below max. capacity");
        assertEquals(52, fireworkStack.getCount(), "the firework stack count should be reduced to 52");
        assertEquals(512, rechargeableFireworkStack.getOrDefault(ModComponents.ROCKETS_LOADED, 0), "the rocket bundle should reach 512 (max)");

        // Verify
        verify(rechargeableFireworkItem).playInsertSound(any(PlayerEntity.class));
    }

    @Test
    @DisplayName("Test item bar for unused rocket bundle")
    void testIsItemBarVisible_unused() {
        // When
        boolean result = rechargeableFireworkStack.isItemBarVisible();

        //Then
        assertFalse(result, "unused rocket bundle stack should not show item bar");
    }

    @Test
    @DisplayName("Test item bar for full rocket bundle")
    void testIsItemBarVisible_fullCapacity() {
        // Given
        rechargeableFireworkItem.setLoadedFireworks(rechargeableFireworkStack, 512);

        // When
        boolean result = rechargeableFireworkStack.isItemBarVisible();

        //Then
        assertFalse(result, "full rocket bundle stack should not show item bar");
    }

    @Test
    @DisplayName("Test item bar for consumed rocket bundle")
    void testIsItemBarVisible_consumed() {
        // Given
        rechargeableFireworkItem.setLoadedFireworks(rechargeableFireworkStack, 11);

        // When
        boolean result = rechargeableFireworkStack.isItemBarVisible();

        //Then
        assertTrue(result, "consumed rocket bundle stack should not show item bar");
    }

    @Test
    @DisplayName("Test use rocket bundle successfully")
    void testUse_successful() {
        // Mock behavior
        when(player.getStackInHand(Hand.MAIN_HAND)).thenReturn(rechargeableFireworkStack);
        when(player.isGliding()).thenReturn(true);

        // When
        rechargeableFireworkItem.setLoadedFireworks(rechargeableFireworkStack, 11);
        ActionResult actionResult = rechargeableFireworkStack.use(world, player, Hand.MAIN_HAND);

        // Then
        assertEquals("Success[swingSource=CLIENT, itemContext=ItemContext[wasItemInteraction=true, heldItemTransformedTo=1 [unregistered]]]", actionResult.toString(), "successful use of rocket bundle should return successful ActionResult");
        assertEquals(10, rechargeableFireworkItem.getLoadedFireworks(rechargeableFireworkStack), "consumption should reduce number of loaded fireworks");

        // Verify
        verify(rechargeableFireworkItem, never()).playEmptySound(any(World.class), any(PlayerEntity.class));
        verify(rechargeableFireworkItem).spawnFireworkEntity(any(World.class), any(PlayerEntity.class), any(ItemStack.class));
    }

    @Test
    @DisplayName("Test use rocket bundle fail because it is empty")
    void testUse_empty() {
        // Mock behavior
        when(player.getStackInHand(Hand.MAIN_HAND)).thenReturn(rechargeableFireworkStack);
        when(player.isGliding()).thenReturn(true);

        // When
        rechargeableFireworkItem.setLoadedFireworks(rechargeableFireworkStack, 0);
        ActionResult  actionResult = rechargeableFireworkStack.use(world, player, Hand.MAIN_HAND);

        // Then
        assertEquals(ActionResult.FAIL, actionResult, "unsuccessful use of rocket bundle should return fail ActionResult");
        assertEquals(0, rechargeableFireworkItem.getLoadedFireworks(rechargeableFireworkStack), "consumption should not reduce number of loaded fireworks");

        // Verify
        verify(rechargeableFireworkItem).playEmptySound(any(World.class), any(PlayerEntity.class));
        verify(rechargeableFireworkItem, never()).spawnFireworkEntity(any(World.class), any(PlayerEntity.class), any(ItemStack.class));
    }

    @Test
    @DisplayName("Test use rocket bundle fail because player is not flying")
    void testUse_notFlying() {
        // Mock behavior
        when(player.getStackInHand(Hand.MAIN_HAND)).thenReturn(rechargeableFireworkStack);
        when(player.isGliding()).thenReturn(false);

        // When
        rechargeableFireworkItem.setLoadedFireworks(rechargeableFireworkStack, 20);
        ActionResult actionResult = rechargeableFireworkStack.use(world, player, Hand.MAIN_HAND);

        // Then
        assertEquals(ActionResult.FAIL, actionResult, "unsuccessful use of rocket bundle should return fail ActionResult");
        assertEquals(20, rechargeableFireworkItem.getLoadedFireworks(rechargeableFireworkStack), "consumption should not reduce number of loaded fireworks");

        // Verify
        verify(rechargeableFireworkItem, never()).spawnFireworkEntity(any(World.class), any(PlayerEntity.class), any(ItemStack.class));
    }

}

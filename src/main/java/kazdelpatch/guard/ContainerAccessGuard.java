package kazdelpatch.guard;

import cpw.mods.fml.common.eventhandler.Event;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerOpenContainerEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class ContainerAccessGuard {

    private static final Logger LOGGER = LogManager.getLogger("KazdelPatch");
    private static final int MAX_REFLECTION_DEPTH = 6;
    private static final Map<Class<?>, Field[]> RELEVANT_FIELD_CACHE =
        new ConcurrentHashMap<Class<?>, Field[]>();

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onPlayerOpenContainer(PlayerOpenContainerEvent event) {
        if (event == null || event.entityPlayer == null || event.entityPlayer.worldObj == null) {
            return;
        }
        if (event.entityPlayer.worldObj.isRemote) {
            return;
        }
        if (event.getResult() != Event.Result.ALLOW) {
            return;
        }
        if (event.canInteractWith) {
            return;
        }

        Container container = event.entityPlayer.openContainer;
        if (container == null || !hasInvalidBackend(container)) {
            return;
        }

        event.setResult(Event.Result.DENY);
        event.entityPlayer.closeScreen();
        LOGGER.warn(
            "[KazdelPatch] Closed force-allowed container {} for player {} because backend inventory became invalid.",
            container.getClass().getName(),
            event.entityPlayer.getCommandSenderName()
        );
    }

    private static boolean hasInvalidBackend(Container container) {
        if (container.inventorySlots == null || container.inventorySlots.isEmpty()) {
            return false;
        }

        Set<IInventory> inventories = newIdentitySet();
        for (Object slotObj : container.inventorySlots) {
            if (!(slotObj instanceof Slot)) {
                continue;
            }
            IInventory inventory = ((Slot) slotObj).inventory;
            if (inventory == null || inventory instanceof InventoryPlayer) {
                continue;
            }
            inventories.add(inventory);
        }

        if (inventories.isEmpty()) {
            return false;
        }

        for (IInventory inventory : inventories) {
            if (isInventoryBackendInvalid(inventory)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isInventoryBackendInvalid(IInventory inventory) {
        Set<TileEntity> tileEntities = newIdentitySet();
        collectTileEntities(inventory, newIdentitySet(), tileEntities, 0);
        if (tileEntities.isEmpty()) {
            return false;
        }
        for (TileEntity tileEntity : tileEntities) {
            if (!isTileEntityStillBound(tileEntity)) {
                return true;
            }
        }
        return false;
    }

    private static void collectTileEntities(Object current, Set<Object> visited, Set<TileEntity> out, int depth) {
        if (current == null || depth > MAX_REFLECTION_DEPTH || !visited.add(current)) {
            return;
        }
        if (current instanceof TileEntity) {
            out.add((TileEntity) current);
            return;
        }

        for (Field field : getRelevantFields(current.getClass())) {
            Object value = readFieldValue(field, current);
            if (value == null) {
                continue;
            }
            if (value instanceof TileEntity) {
                out.add((TileEntity) value);
            } else {
                collectTileEntities(value, visited, out, depth + 1);
            }
        }
    }

    private static Object readFieldValue(Field field, Object owner) {
        try {
            return field.get(owner);
        } catch (Throwable ignored) {
            return null;
        }
    }

    private static boolean isTileEntityStillBound(TileEntity tileEntity) {
        if (tileEntity == null || tileEntity.isInvalid()) {
            return false;
        }

        World world = tileEntity.getWorldObj();
        if (world == null) {
            return false;
        }
        if (!world.blockExists(tileEntity.xCoord, tileEntity.yCoord, tileEntity.zCoord)) {
            return false;
        }

        TileEntity live = world.getTileEntity(tileEntity.xCoord, tileEntity.yCoord, tileEntity.zCoord);
        return live == tileEntity && !live.isInvalid();
    }

    private static Field[] getRelevantFields(Class<?> rootType) {
        Field[] cached = RELEVANT_FIELD_CACHE.get(rootType);
        if (cached != null) {
            return cached;
        }

        ArrayList<Field> fields = new ArrayList<Field>();
        Class<?> type = rootType;
        while (type != null && type != Object.class) {
            for (Field field : type.getDeclaredFields()) {
                if (Modifier.isStatic(field.getModifiers())) {
                    continue;
                }
                Class<?> fieldType = field.getType();
                if (!IInventory.class.isAssignableFrom(fieldType) && !TileEntity.class.isAssignableFrom(fieldType)) {
                    continue;
                }
                try {
                    field.setAccessible(true);
                    fields.add(field);
                } catch (Throwable ignored) {
                }
            }
            type = type.getSuperclass();
        }

        Field[] built = fields.toArray(new Field[fields.size()]);
        Field[] previous = RELEVANT_FIELD_CACHE.putIfAbsent(rootType, built);
        return previous != null ? previous : built;
    }

    private static <T> Set<T> newIdentitySet() {
        return Collections.newSetFromMap(new IdentityHashMap<T, Boolean>());
    }
}

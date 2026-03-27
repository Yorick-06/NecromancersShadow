package cz.yorick.data;

import cz.yorick.NecromancersShadow;
import it.unimi.dsi.fastutil.ints.*;
import java.util.Map;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.Mob;

public class ServerShadowManager implements MutableShadowAccess {
    private final ServerPlayer owner;
    private final Int2ObjectMap<Mob> spawned = new Int2ObjectArrayMap<>();

    public ServerShadowManager(ServerPlayer owner) {
        this.owner = owner;
    }

    private boolean shouldHandleResources() {
        return !this.owner.isCreative();
    }

    private boolean toggleShadowsInternal() {
        if(this.spawned.isEmpty()) {
            return trySpawnShadows();
        }

        despawnShadows();
        return true;
    }

    private boolean trySpawnShadows() {
        int spawned = 0;
        for (Map.Entry<Integer, ShadowData> shadowDataEntry : DataAttachments.getShadowStorage(this.owner)) {
            if(trySpawnShadow(shadowDataEntry.getKey(), shadowDataEntry.getValue())) {
                spawned++;
            }
        }

        return spawned > 0;
    }

    private boolean trySpawnShadow(int slot, ShadowData shadow) {
        //do not spawn if already spawned
        if(this.spawned.containsKey(slot)) {
            return false;
        }

        if(shouldHandleResources()) {
            if(shadow.cost() > DataAttachments.getSoulEnergy(this.owner)) {
                return false;
            }

            DataAttachments.removeSoulEnergy(this.owner, shadow.cost());
        }

        BlockPos spawnPos = this.owner.blockPosition().offset(-2 + this.owner.getRandom().nextInt(5), 1, -2 + this.owner.getRandom().nextInt(5));
        shadow.entityType().spawn(this.owner.level(), spawned -> {
            if(spawned instanceof Mob mobEntity) {
                shadow.applyTo(mobEntity, this.owner);
                this.spawned.put(slot, mobEntity);
            }
        }, spawnPos, EntitySpawnReason.TRIGGERED, false, false);
        return true;
    }

    public void despawnShadows() {
        if(this.spawned.isEmpty()) {
            return;
        }

        mutateStorage(mutableStorage -> {
            //use toIntArray to receive a copy, this prevents concurrent modification
            for (int slot : this.spawned.keySet().toIntArray()) {
                tryDespawnShadow(mutableStorage, slot);
            }
        });
    }

    private void tryDespawnShadow(MutableShadowStorage mutableStorage, int slot) {
        Mob spawnedEntity = this.spawned.get(slot);
        //not spawned -> do nothing
        if(spawnedEntity == null) {
            return;
        }

        //update data on the item stack representation
        ShadowData data = ShadowData.fromEntity(spawnedEntity);

        //if the shadow died, do not return the soul energy - store this before removing the entity
        if(spawnedEntity.isAlive() && shouldHandleResources()) {
            DataAttachments.addSoulEnergy(this.owner, data.cost());
        }

        spawnedEntity.remove(Entity.RemovalReason.DISCARDED);
        this.spawned.remove(slot);
        mutableStorage.setShadow(slot, data);
    }

    private void mutateStorage(Consumer<MutableShadowStorage> mutator) {
        DataAttachments.mutateShadowStorage(this.owner, mutator);
    }

    public void convertShadow(Mob oldEntity, Mob newEntity) {
        int slot = findSlot(oldEntity);
        if(slot <= -1) {
            NecromancersShadow.LOGGER.warn("findSlot returned -1 when called from convertShadow, should never happen");
            return;
        }

        this.spawned.put(slot, newEntity);
    }

    private int findSlot(Mob entity) {
        //maybe a better way to do this?
        for (Int2ObjectMap.Entry<Mob> mobEntityEntry : this.spawned.int2ObjectEntrySet()) {
            if(mobEntityEntry.getValue() == entity) {
                return mobEntityEntry.getIntKey();
            }
        }

        return -1;
    }

    //override to handle swapping spawned references and
    //bypass shadows despawning from set()
    @Override
    public void swapShadows(int from, int to) {
        //swap data - bypass local despawn from set
        mutateStorage(mutableStorage -> {
            ShadowData toShadowData = mutableStorage.getShadow(to);
            mutableStorage.setShadow(to, mutableStorage.getShadow(from));
            mutableStorage.setShadow(from, toShadowData);
        });

        //swap references for the spawned entities - handle nulls manually
        Mob fromEntity = this.spawned.remove(from);
        Mob toEntity = this.spawned.remove(to);
        if(fromEntity != null) {
            this.spawned.put(to, fromEntity);
        }

        if(toEntity != null) {
            this.spawned.put(from, toEntity);
        }
    }

    private ImmutableShadowStorage storage() {
        return DataAttachments.getShadowStorage(this.owner);
    }

    @Override
    public ShadowData getShadow(int slot) {
        return storage().getShadow(slot);
    }

    @Override
    public void setShadow(int slot, ShadowData shadowData) {
        mutateStorage(mutableStorage -> {
            //try to despawn the current shadow in the slot
            tryDespawnShadow(mutableStorage, slot);
            mutableStorage.setShadow(slot, shadowData);
        });
    }

    @Override
    public int lastOccupiedSlot() {
        return storage().lastOccupiedSlot();
    }

    @Override
    public boolean isEmpty() {
        return storage().isEmpty();
    }

    public void toggleShadow(int slot) {
        if(this.spawned.containsKey(slot)) {
            mutateStorage(mutableStorage -> tryDespawnShadow(mutableStorage, slot));
            return;
        }

        ShadowData shadow = DataAttachments.getShadowStorage(this.owner).getShadow(slot);
        if(shadow == null) {
            return;
        }

        trySpawnShadow(slot, shadow);
    }

    public static boolean toggleShadows(ServerPlayer player) {
        return DataAttachments.getShadowManager(player).toggleShadowsInternal();
    }
}

# Allows you to capture and summon the souls of mobs!
## Shadows
- The ghostly entities which you can summon
- Can be obtained by killing an entity while holding a sculk totem in your main hand or offhand
- A small entity displaying soul particles will be spawned where the entity died, right clicking this entity will add it
to you shadow inventory
- The shadow inventory can be seen in the tooltip of any sculk totem and is not lost on death
- The summoned shadows keep most behaviours from their original entities (removes hostility and fleeing from entities)
- Shadows keep any extra data the entity had and are re-saved on each despawn, this allows them to keep data such as form
and equipment not only from the original entity, but also allows you to give your shadows extra equipment which they will keep
- **There are no limits!** - even boss mobs such as the dragon, wither and warden work
- Compatibility with other mods has not been tested, but there is a good chance of it working!

## Soul energy
- This energy is required to spawn shadows, it is obtained by holding a sculk totem while absorbing xp (1 soul energy =
30 xp points (NOT LEVELS)), keep in mind that it takes the xp instead of mending or your xp bar.
- The cost to spawn each shadow is determined based on its health, 1 health = 1 soul energy
- When despawning a shadow the soul energy is returned if the shadow is still alive
- If a spawned shadow dies, its soul won't disappear from your inventory, but you won't receive any soul energy back when
despawning the shadows.
- The max soul energy capacity is 10 by default, but it can be infinitely increased by eating echo shards
(1 shard gives +10 max soul energy)
- All soul energy requirements are ignored while in creative mode 

## Sculk Totem
- Can be crafted from a totem of undying and an echo shard
- Is fireproof
- Right click to spawn/despawn shadows present in your shadow inventory
- Hitting an entity with this totem makes all spawned shadows target it
- Shift right clicking stops the shadows from attacking
- If you forget something, pressing shift while hovering the item will show a description of its features 

## Sculk Emerald
- Can be crafted from an emerald and an echo shard
- Is fireproof
- Right clicking a soul with this item adds it to the items inventory instead of your own
- Has two modes which can be switched by left clicking while holding the item
- INPUT
  - Right clicking a spawned shadow will despawn it and transfer it from your to the items inventory
  - Right clicking normally will transfer the first shadow from your inventory to this item
  - Shift right clicking will transfer all shadows from your inventory to this item
- OUTPUT
  - Right clicking will transfer the first shadow from this item to your inventory
  - Shift right clicking will transfer all shadows from this item to your inventory 
- Can be cleared by using a crafting table (place it anywhere in the crafting grid and the result will be an empty sculk emerald) 
- If you forget something, pressing shift while hovering the item will show a description of its features

## /soulEnergy
A simple command which allows the manipulation of both soul energy and max soul energy, requires op
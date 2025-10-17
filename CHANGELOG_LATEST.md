## Animal Pens Paperized 1.3.7

Added events that allows to detect when players interact with plugin items:
- AnimalCatchEvent - triggered right before animal is added to cage/container
- AnimalReleaseEvent - triggered right before animal is released into world
- AnimalDepositEvent - triggered right before cage/container content is deposited into pen/aquarium
- AnimalWithdrawnEvent - triggered right before pen/aquarium content is withdrawn into hand/cage/container
- AnimalBlockInteractEvent - triggered right before processing interaction with animal pen/aquarium
- AnimalBlockAttackEvent - triggered right before processing entity killing in animal pen/aquarium
- AnimalBlockBreakEvent - triggered right before animal pen/aquarium is broken
- AnimalBlockPlaceEvent - triggered right before animal pen/aquarium is placed in world.

Fixed a bug that ignored `max_amount` in animal cage.
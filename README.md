# 🐾 Animal Pens Paperized

## ✅ Optimize Your Farms & Reduce Lag!

Tired of lag caused by massive animal farms? **Animal Pens** lets you store animals in specialized **pens** and **aquariums** while keeping full functionality!  
No more overcrowded barns - keep your animals neatly contained while still breeding, shearing, milking, and collecting drops like normal.

This plugin is reimplementation of Animal Pens mod.

***

## 🎮 How It Works:

✔️ **Animal Cage & Container** – Capture land animals with the **Animal Cage** and water creatures with the **Animal Container**.  
✔️ **Feeding animals acts as breeding** – No need for free-roaming animals, just feed them inside their pen!  
✔️ **Most vanilla interactions**(1) are possible, including:  
    🗡️ _Animal farming_ for loot  
    ✂️ _Shearing sheep_  
    🥛 _Milking cows, mooshrooms, and goats_  
    🍄 _Collecting mushroom stew from mooshrooms_  
    🐝 _Bee pollen regeneration_  
    🐢 _Turtle scutes dropping based on settings_  
    🖌️ _Brush scutes from armadillo_  
    🧺 _Water bucket to pick up fishes or axolotls_

✔️ **Custom Interactions:**  
🔹 Chickens, Turtles and Sniffers – Use a bucket to collect eggs.  
🔹 Turtles – Drop **scutes** after feeding (configurable timing).  
🔹 Sniffers – Require a bowl to drop seeds.  
🔹 Fish Feeding – Fish eat **kelp & seagrass**, while other water creatures eat **fish**.  
🔹 Frogs – Drop **Frog Lights** when fed a Magma Cube.

✔️ **Releasing animals** by crouch clicking on ground.

✔️ **Variant Storage & Selection**  
    • Picking up an animal **saves its variant** inside the cage.  
    • Right-click an empty-handed animal to **view stored variants**.

(1) Features not added:  
     Any animal transformation  
     Any animal interaction with other entities

***

## 🔧 Customizable Settings:

Easily adjust gameplay with the **mod’s configuration file** _\[plugins/AnimalPenPlugin/config.json\]_, allowing you to tweak:

⚙️ **Cooldowns for actions** – Example:  
    • Feeding animals  
    • Shearing sheep  
    • Collecting eggs from chickens/turtles  
    • Milking cow

🗡️ **Attack cooldown** - Allows to change how fast players can kill animals in pen.

📦 **Drop Limits** – Prevent item overflow:  
    • 🐑 Wool drop limit  
    • 🥚 Egg drop limit

📏 **Animal Growth System** (Optional):  
    • If enabled, **more animals = larger pen display**.  
    • Adjustable growth multiplier.

📊 **Statistics** – Allows to toggle if interactions with animal in pen should affect statistics.

🔼 **Advancements** – Allows to toggle if interactions with animal in pen should affect advancements.

🚫 **Blocked Animals List** – Prevent specific mobs from being captured.

***

### Customizable Food Items

You can also add or change food items for each animal. You can do it with data packs.
To add/change animal food you need to create a file: `\[plugins/AnimalPenPlugin/animal_foods.json\].json`

```
{
  // optional value. Defines when the food items should be loaded
  "<animal>": [
    "<mod>:<item>", // for items
    "#<mod>:<tag>"  // for tags (# at front)
  ]
}
```

### Recipes

Recipes can be adjusted via datapacks, however, you must follow examples that are added by default: [Recipes Data Pack](https://github.com/BONNePlayground/AnimalPenPaperized/tree/master/src/main/resources/animal_pen_data_pack/data/animal_pen/recipe)

### Resource Pack

Plugin comes with resource pack you can add to your client to show custom item icons as well as make animal pen and aquarium look nicer.
Default Resource Pack: https://github.com/BONNePlayground/AnimalPenPaperized/tree/master/src/main/resources/animal_pen_resource_pack

### Localization

Plugin comes with server side messages `messages.yml`. However, using resource pack you can customize translations for client.

***

## 📜 Commands:

Use these commands (server moderators) to manage the mod:

💾 `/animal_pen reset` – Resets the config file to default values.  
🔄 `/animal_pen reload` – Reloads the config file without restarting the game.
🎒 `/animal_pen items` – Custom creative menu to get custom items.

***

## 🎨 Mod Artwork:

Credit to **Breadcrumb5550** for the mod’s blocks and items!

***

With **Animal Pens**, you can **reduce lag, keep your farms organized, and maintain full interactivity!**

## ❓ FAQ

🔹 Precise Clicking

In Minecraft Paper, you have limited control over the overall system, which is why interactions are separated between entities and blocks:

- Animal cages and water containers can only interact when placed on smooth stone slabs.
- Killing and other entity interactions can only be performed by clicking directly on the entity.
- Breaking pens and aquariums is only possible by breaking the base smooth stone slab.

🔹 Is Water Necessary for Aquariums?

In Minecraft, fish outside of water will always appear sideways. This is a hard-coded behavior for all water animals - they automatically change their pose when out of water. While this can be modified in modded environments, it is not possible in a Paper plugin.
You can reduce this issue by using the Animal Pens Resource Pack.
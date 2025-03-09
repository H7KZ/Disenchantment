# Config file handbook

```yaml

# Available locales - do not change this value
locales: [ "cs", "en" ]

# Boolean - Enable or disable the plugin
enabled: true

# String - Set the locale for the plugin, available locales are in the list above
locale: "en"

# EventPriority - Set the priority for the events
# Available priorities (sorted by priority): LOWEST, LOW, NORMAL, HIGH, HIGHEST, MONITOR
# Read more here: https://hub.spigotmc.org/javadocs/spigot/org/bukkit/event/EventPriority.html
event-priorities:
  disenchant-click: HIGHEST
  disenchant: HIGHEST
  shatter-click: HIGHEST
  shatter: HIGHEST
  
# ----------------------------------------------------------------------------------------

disenchantment:
   # Boolean - Enable or disable the disenchantment feature
   enabled: true
   # List<String> - List of worlds where the disenchantment is disabled
   disabled-worlds: [ ]
   # List<String> - List of materials that are not allowed to be disenchanted
   disabled-materials: [ ]
   # List<String> - List of enchantments states for how they should be handled
   # Format is:  "enchantment:keep" or "enchantment:disabled"
   # Keep = Do not remove the enchantment from the item
   # Disabled = Do not proceed with the disenchantment if the enchantment is present
   enchantments-states: [ ]
   anvil:
      sound:
         # Boolean - Enable or disable the anvil sound
         enabled: true
         # Double - Edit the volume and pitch of the anvil sound
         volume: 1.0
         pitch: 1.0
      repair:
         # Boolean - Enable or disable the repair cost reset back to 0 when the item is disenchanted
         reset: false
         # Boolean - Enable or disable the repair cost
         cost: true
         # Double - Base value for the cost of the disenchantment
         base: 10
         # Double - This multiplication is applied to the base value
         # The enchantments are sorted by level in descending order, so the more enchantments you have, the more expensive it is
         # Multiplication value from 0 to [max int value] (0.25 = 25%)
         #
         # It is calculated by the following:
         #   base + (level * multiply)
         #
         # Example: base = 10, sharpness = 5, fire_aspect = 1, multiplier = 0.25
         #
         #   10 + (1 * 1) = 11  ->  11 + (5 * 1.25) = 17.25  ->  17.25
         multiply: 0.25

# ----------------------------------------------------------------------------------------

shatterment:
   # Boolean - Enable or disable the shatterment feature
   enabled: true
   # List<String> - List of worlds where the shatterment is disabled
   disabled-worlds: [ ]
   # List<String> - List of enchantments states for how they should be handled
   # Format is:  "enchantment:keep" or "enchantment:disabled"
   # Keep = Do not remove the enchantment from the item
   # Disabled = Do not proceed with the disenchantment if the enchantment is present
   enchantments-states: [ ]
   anvil:
      sound:
         # Boolean - Enable or disable the anvil sound
         enabled: true
         # Double - Edit the volume and pitch of the anvil sound
         volume: 1.0
         pitch: 1.0
      repair:
         # Boolean - Enable or disable the repair cost reset back to 0 when the item is disenchanted
         reset: false
         # Boolean - Enable or disable the repair cost
         cost: true
         # Double - Base value for the cost of the shatterment
         base: 10
         # Double - This multiplication is applied to the base value
         # The enchantments are sorted by level in descending order, so the more enchantments you have, the more expensive it is
         # Multiplication value from 0 to [max int value] (0.25 = 25%)
         #
         # It is calculated by the following:
         #   base + (level * multiply)
         #
         # Example: base = 10, sharpness = 5, fire_aspect = 1, multiplier = 0.25
         #
         #   10 + (1 * 1) = 11  ->  11 + (5 * 1.25) = 17.25  ->  17.25
         multiply: 0.25
```

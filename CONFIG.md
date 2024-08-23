# Config file handbook

```yaml
# DO NOT EDIT THIS VALUE
migration: 1
# ----------------------------------------------------------------------------------------
# For descriptions of each option, see the wiki:
# https://github.com/H7KZ/Disenchantment/CONFIG.md
# ----------------------------------------------------------------------------------------

# Boolean - Enable or disable the plugin
enabled: true
# Boolean - Enable or disable logging
logging-enable: true
# String - Logging level: INFO, DEBUG
logging-level: "INFO"

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
         # Example: base = 10, sharpness = 5, knockback = 1, multiplier = 0.25
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
         # Example: base = 10, sharpness = 5, knockback = 1, multiplier = 0.25
         #
         #   10 + (1 * 1) = 11  ->  11 + (5 * 1.25) = 17.25  ->  17.25
         multiply: 0.25
```

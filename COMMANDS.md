## Commands

### Command aliases:
- `/disenchantment`
- `/disenchant`

### Enchantment states:
- `enable` - Enchantment can be removed from items.
- `keep` - Enchantment stays on items.
- `delete` - Enchantment is removed from item and destroyed.
- `disable` - Disables the entire disenchantment/shatterment process.

### Command list

- `/disenchantment` - Help page 1
- `/disenchantment help [<1,...,8>]` - Help pages 1-8
---
- `/disenchantment diagnostic ['all']` - Show diagnostic information about the plugin. Use 'all' to show all information.
- `/disenchantment gui` - Open GUI for disenchantment configuration.
- `/disenchantment status` - Show if the plugin is enabled or disabled.
- `/disenchantment toggle` - Toggle the plugin on/off.
---
- `/disenchantment disenchant:enchantments` - Show current list of enchantment settings for disenchantment.
- `/disenchantment disenchant:enchantments [enchantment] ['enable', 'keep', 'delete', 'disable']` - Change configuration of disabled enchantments.
- `/disenchantment disenchant:worlds` - Show current list of disabled worlds for disenchantment.
- `/disenchantment disenchant:worlds [world]` - Toggle specific world to enable/disable disenchantment.
- `/disenchantment disenchant:materials` - Show current list of disabled materials for disenchantment.
- `/disenchantment disenchant:materials [material]` - Toggle specific material to enable/disable disenchantment.
---
- `/disenchantment disenchant:repair` - Show current configuration of repair cost for disenchantment.
- `/disenchantment disenchant:repair ['enable', 'disable']` - Enable/disable repair cost.
- `/disenchantment disenchant:repair reset ['enable', 'disable']` - Enable/disable the reset of repair cost to 0 after disenchantment.
- `/disenchantment disenchant:repair base [int]` - Change value of the base repair cost.
- `/disenchantment disenchant:repair multiply [float]` - Change value of the multiplication for repair cost.
---
- `/disenchantment disenchant:sound` - Show current configuration of anvil sound for disenchantment.
- `/disenchantment disenchant:sound ['enable', 'disable']` - Enable/disable anvil sound.
- `/disenchantment disenchant:sound volume [float]` - Change value of the anvil sound volume.
- `/disenchantment disenchant:sound pitch [float]` - Change value of the anvil sound pitch.
---
- `/disenchantment shatter:enchantments` - Show current list of enchantment settings for shatterment.
- `/disenchantment shatter:enchantments [enchantment] ['enable', 'keep', 'delete', 'disable']` - Change configuration of disabled enchantments.
- `/disenchantment shatter:worlds` - Show current list of disabled worlds for shatterment.
- `/disenchantment shatter:worlds [world]` - Toggle specific world to enable/disable shatterment.
---
- `/disenchantment shatter:repair` - Show current configuration of repair cost for shatterment.
- `/disenchantment shatter:repair ['enable', 'disable']` - Enable/disable repair cost.
- `/disenchantment shatter:repair reset ['enable', 'disable']` - Enable/disable the reset of repair cost to 0 after shatterment.
- `/disenchantment shatter:repair base [int]` - Change value of the base repair cost.
- `/disenchantment shatter:repair multiply [float]` - Change value of the multiplication for repair cost.
---
- `/disenchantment shatter:sound` - Show current configuration of anvil sound for shatterment.
- `/disenchantment shatter:sound ['enable', 'disable']` - Enable/disable anvil sound.
- `/disenchantment shatter:sound volume [float]` - Change value of the anvil sound volume.
- `/disenchantment shatter:sound pitch [float]` - Change value of the anvil sound pitch.

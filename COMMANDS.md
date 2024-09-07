## Commands

> Command aliases:
> - `/disench`
> - `/disenchant`
> - `/disenchantment`

#### Command legend (syntax)

- `[ ]` - Required
- `|` - Or
- `' '` - Text
- `< >` - Type
- Types:
   - `<number>` - Any number in format `12` or `14.3`
   - `<enchantment name>` - Enchantment key (e.g. `flame`)
   - `<world name>` - World name (e.g. `world_the_end`)
   - `<material name>` - Material key (e.g. `diamond_sword`)

// TODO: Review this and write current commands

### Command list

- `/disenchantment` - Help
- `/disenchantment status` - Is plugin enabled or disabled
- `/disenchantment toggle` - Enable or disable the plugin


- `/disenchantment disenchant_enchantments` - Get the enchantments settings
- `/disenchantment disenchant_enchantments [<enchantment name>] ['enable' | 'keep' | 'cancel']` - Set which enchantments are allowed to be removed
    - Enchantment command examples:
    - `/disenchantment disenchant_enchantments [<enchantment name>] enable` - Remove the enchantment from the list
    - `/disenchantment disenchant_enchantments [<enchantment name>] keep` - Enchantment will be kept on an item when disenchanting
    - `/disenchantment disenchant_enchantments [<enchantment name>] cancel` - Entire disenchanting process will be canceled if item has this enchantment
- `/disenchantment disenchant_worlds` - Get the worlds settings
- `/disenchantment disenchant_worlds [<world name>]` - Enable or disable the plugin in a specific world
- `/disenchantment disenchant_materials` - Get the materials settings
- `/disenchantment disenchant_materials [<material name>]` - Set which materials are not allowed to be disenchanted


- `/disenchantment disenchant_repair` - Get the repair settings
- `/disenchantment disenchant_repair ['enable' | 'disable']` - Enable or disable the repair cost
- `/disenchantment disenchant_repair reset ['enable' | 'disable']` - Enable or disable the repair cost reset
- `/disenchantment disenchant_repair base [<number>]` - Set the base cost of disenchanting items
- `/disenchantment disenchant_repair multiply [<number>]` - Set the multiplier when disenchanting items with multiple enchantments


- `/disenchantment disenchant_sound` - Get the sound settings
- `/disenchantment disenchant_sound ['enable' | 'disable']` - Enable or disable the anvil sound
- `/disenchantment disenchant_sound volume [<number>]` - Set the anvil sound volume
- `/disenchantment disenchant_sound pitch [<number>]` - Set the anvil sound pitch


- `/disenchantment shatter_enchantments` - Get the enchantments settings
- `/disenchantment shatter_enchantments [<enchantment name>] ['enable' | 'keep' | 'cancel']` - Set which enchantments are allowed to be removed
    - Enchantment command examples:
    - `/disenchantment shatter_enchantments [<enchantment name>] enable` - Remove the enchantment from the list
    - `/disenchantment shatter_enchantments [<enchantment name>] keep` - Enchantment will be kept on an item when disenchanting
    - `/disenchantment shatter_enchantments [<enchantment name>] cancel` - Entire disenchanting process will be canceled if item has this enchantment
- `/disenchantment shatter_worlds` - Get the worlds settings
- `/disenchantment shatter_worlds [<world name>]` - Enable or disable the plugin in a specific world


- `/disenchantment shatter_repair` - Get the repair settings
- `/disenchantment shatter_repair ['enable' | 'disable']` - Enable or disable the repair cost
- `/disenchantment shatter_repair reset ['enable' | 'disable']` - Enable or disable the repair cost reset
- `/disenchantment shatter_repair base [<number>]` - Set the base cost of disenchanting items
- `/disenchantment shatter_repair multiply [<number>]` - Set the multiplier when disenchanting items with multiple enchantments


- `/disenchantment shatter_sound` - Get the sound settings
- `/disenchantment shatter_sound ['enable' | 'disable']` - Enable or disable the anvil sound
- `/disenchantment shatter_sound volume [<number>]` - Set the anvil sound volume
- `/disenchantment shatter_sound pitch [<number>]` - Set the anvil sound pitch

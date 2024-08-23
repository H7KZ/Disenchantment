## Commands

> Command aliases:
> - `/disench`
> - `/disenchant`
> - `/disenchantment`

#### Command legend (syntax)

> - `[ ]` - Required
> - `( )` - Optional
> - `|` - Or
> - `' '` - Text
> - `< >` - Type
> - Types:
    >
- `number` - Any number in format `12` or `14.3`
>   - `enchantment name` - Enchantment key (e.g. `flame`)
>   - `world name` - World name (e.g. `world_the_end`)
>   - `material name` - Material key (e.g. `diamond_sword`)

### Command list

- `/disenchantment` - Help
- `/disenchantment gui` - Open the settings GUI (RECOMMENDED WAY TO CHANGE SETTINGS)
- `/disenchantment help [<number>]` - Help page
- `/disenchantment status` - Is plugin enabled or disabled
- `/disenchantment toggle` - Enable or disable the plugin
- `/disenchantment toggle [<world name>]` - Enable or disable the plugin in a specific world
- `/disenchantment repair` - Get the repair settings
- `/disenchantment repair ['enable' | 'disable']` - Enable or disable the repair cost
- `/disenchantment repair reset ['enable' | 'disable']` - Enable or disable the repair cost reset
- `/disenchantment repair base [<number>]` - Set the base cost of disenchanting items
- `/disenchantment repair multiply [<number>]` - Set the multiplier when disenchanting items with multiple enchantments
- `/disenchantment enchantments` - Get the enchantments settings
- `/disenchantment enchantments [enchantment name] ('keep' or 'cancel')` - Set which enchantments are allowed to be
  removed
    - Enchantment command examples:
    - `/disenchantment enchantments [enchantment name]` - Remove the enchantment from the list
    - `/disenchantment enchantments [enchantment name] keep` - Enchantment will be kept on an item when disenchanting
    - `/disenchantment enchantments [enchantment name] cancel` - Entire disenchanting process will be canceled if item
      has this enchantment
- `/disenchantment materials` - Get the materials settings
- `/disenchantment materials [<material name>]` - Set which materials are not allowed to be disenchanted
- `/disenchantment sound` - Get the sound settings
- `/disenchantment sound ['enable' | 'disable']` - Enable or disable the anvil sound
- `/disenchantment sound volume [<number>]` - Set the anvil sound volume
- `/disenchantment sound pitch [<number>]` - Set the anvil sound pitch

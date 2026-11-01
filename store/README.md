# Store pages

Ready-to-paste descriptions for every platform Disenchantment is published on. Images are hotlinked from this repo's
`master` branch (`https://raw.githubusercontent.com/H7KZ/Disenchantment/master/assets/...`), so **push `assets/` to
`master` before pasting**, or the images won't load.

| File                                 | Platform                                                    | Format                                 |
|--------------------------------------|-------------------------------------------------------------|----------------------------------------|
| [SPIGOTMC.bbcode](SPIGOTMC.bbcode)   | [SpigotMC](https://www.spigotmc.org/resources/110741)       | BBCode: paste in the BBCode editor mode |
| [MODRINTH.md](MODRINTH.md)           | [Modrinth](https://modrinth.com/plugin/disenchantment)      | Markdown                               |
| [CURSEFORGE.md](CURSEFORGE.md)       | CurseForge (Bukkit Plugins)                                 | Markdown: pick the Markdown editor      |
| [HANGAR.md](HANGAR.md)               | [Hangar](https://hangar.papermc.io) (PaperMC)               | Markdown                               |

## Short summaries

Each summary is kept under 100 characters, so it fits every platform's limit.

- **Tag line / summary:** `Disenchant items onto books and split enchanted books — right in the vanilla anvil.`
- **Alternative:** `Right enchant, wrong item? Move enchantments to books and split them using the anvil.`

## Categories / tags

- **Modrinth:** Utility, Game Mechanics, Economy · Loaders: Bukkit, Spigot, Paper, Folia, Purpur
- **SpigotMC:** Spigot → Mechanics
- **CurseForge:** Mechanics, Fixes
- **Hangar:** Gameplay, Admin Tools

## Media (in [`../assets`](../assets))

| File                                  | Use                                                            |
|---------------------------------------|----------------------------------------------------------------|
| `banner.png` (1280×320)               | Description header on every platform                           |
| `thumbnail.png` (1280×720)            | YouTube thumbnail, social preview card                         |
| `hero.jpg` (1920×1014)                | Gallery image, portfolio cover                                  |
| `Disenchantment Transparent *.png`    | Project icon (800×800, scale down on upload)                   |
| `gifs/*.gif` (all < 5 MB)             | Feature demos in descriptions + gallery                        |
| `screenshots/gui-*.png`               | One screenshot per admin GUI screen                            |
| `screenshots/anvil-result.png`        | Static anvil result (also `event.png`)                         |

`anvil.gif` and `event.png` keep their old names so existing hotlinks still work. They now hold the new footage.

### Modrinth / Hangar gallery (in order)

| # | File                            | Title                          | Description                                                       |
|---|---------------------------------|--------------------------------|-------------------------------------------------------------------|
| 1 | `gifs/disenchant.gif` ★featured | Disenchant onto books          | Every enchantment moves from the item onto a plain book.          |
| 2 | `gifs/before-after.gif`         | Vanilla vs. Disenchantment     | The vanilla anvil refuses. Disenchantment gives you the book.     |
| 3 | `gifs/split.gif`                | Split enchanted books          | Peel enchantments off a book one at a time.                       |
| 4 | `gifs/keep-enchant.gif`         | Keep chosen enchantments       | Mending set to "keep" stays on the sword.                         |
| 5 | `screenshots/gui-enchantments.png` | Per-enchantment control     | Enable, keep, delete or disable each enchantment.                 |
| 6 | `screenshots/gui-navigation.png`   | In-game admin GUI           | Every setting is one click away. No YAML editing.                 |
| 7 | `screenshots/gui-statistics.png`   | Statistics                  | See how often your players disenchant and split.                  |
| 8 | `hero.jpg`                      | Enchanting room                | Disenchantment needs nothing but the vanilla anvil.               |

## Showcase video

The 64 s showcase (`00-showcase-1080p.mp4`, kept outside the repo) is silent. Before uploading, add royalty-free
music, for example from the YouTube Audio Library.

- **Title:** `Disenchantment — Move Enchantments to Books & Split Them (Spigot / Paper / Folia Plugin)`
- **Description:** first two paragraphs of `MODRINTH.md` plus the Modrinth / SpigotMC / GitHub links.
- **Embedding:** after upload, add the video under the banner:
  - SpigotMC: `[MEDIA=youtube]VIDEO_ID[/MEDIA]`
  - Modrinth / Hangar / CurseForge: `<iframe>` or a linked thumbnail

## Announcement post (Reddit r/admincraft, Discord)

> **Disenchantment: move enchantments onto books and split enchanted books, using the vanilla anvil**
>
> Put an enchanted item plus a book in an anvil and you get the enchantments back on a book. Put a multi-enchant book
> plus a book in an anvil and one enchantment splits off. No new blocks, no player GUIs, no resource pack. Admins get an
> in-game config GUI, per-enchantment rules (keep/delete/disable), XP and Vault costs, world and material
> restrictions, and support for AdvancedEnchantments, EcoEnchants, ExcellentEnchants and more.
>
> Spigot / Paper / Folia · 1.18 – 26.x · free & open source (MIT)
> Modrinth: https://modrinth.com/plugin/disenchantment · SpigotMC: https://www.spigotmc.org/resources/110741

Attach `gifs/before-after.gif` or the showcase video natively instead of only linking.

# MBwRF

A custom Rushfight mode for MBedwars.

**Authors:** zimetuer, maciejk2  
**License:** All Rights Reserved - See [LICENSE](LICENSE)

## Features

- Custom kit system for Rushfight
- GUI-based kit editor
- Per-player kit data with database storage
- Map-specific configuration
- Custom respawn mechanics

## Requirements

- Spigot/Bukkit 1.8+
- [MBedwars](https://www.marcely.de/) plugin

### Optional Dependencies

- [SWM (Slime World Manager)](https://github.com/Grinderwolf/Slime-World-Manager) - Optimized world loading
- [FAWE 1.8 Reborn](https://github.com/cmclient/FAWE-1.8-Reborn/releases/tag/fawe-1.8-reborn) - Faster world editing

## Installation

1. Download the latest release
2. Place `MBwRF.jar` in your server's `plugins` folder
3. Start the server
4. Configure the plugin in `plugins/MBwRF/config.yml`

## Commands

| Command | Description | Permission |
|---------|-------------|------------|
| `/rushfight editkit` | Edit your kit | `rushfight.use` |
| `/rushfight resetkit` | Reset your kit | `rushfight.use` |
| `/rushfight list` | List available kits | `rushfight.use` |
| `/rushfight setkit` | Set a kit | `rushfight.admin` |
| `/rushfight addmap` | Add a map | `rushfight.admin` |
| `/rushfight removemap` | Remove a map | `rushfight.admin` |
| `/rushfight reload` | Reload configuration | `rushfight.admin` |

## Permissions

| Permission | Description | Default |
|------------|-------------|---------|
| `rushfight.use` | Access to basic commands | `true` |
| `rushfight.admin` | Access to admin commands | `op` |

## Building

```bash
mvn clean package
```

The compiled JAR will be in `target/MBwRF.jar`.

btw ts is fully vibecoded :pray::sob:

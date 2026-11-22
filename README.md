# Aim Trainer

Meant to train your aim for Diep.io

## Features

- 5 hit points and invulnerability flicker after gettting hit
- Reload bar (yellow while reloading, green when ready)
- Enemy tanks, will respawn with more and stronger tanks
- Score (100 points per kill) with a high score saved between sessions
-
## Controls

| Input | Action |
|---|---|
| WASD / Arrow keys | Move |
| Mouse | Aim |
| Hold left mouse button | Fire (limited by reload time) |
| R or Enter | Restart after game over |

## Running

requires ![Java](https://img.shields.io/badge/java-%23ED8B00.svg?style=for-the-badge&logo=openjdk&logoColor=white)

```
javac *.java
java App
```

[![Repo Card](https://github-readme-stats.vercel.app/api/pin/?username=WenxiWangBuilds&repo=YOUR-REPO-NAME&theme=dark)](https://github.com/WenxiWangBuilds/2d-tank-game)

## Files

| File | Purpose |
|---|---|
| `App.java` | Window and view and everything |
| `Player.java` | Technically a tank class, controls a lot including health and reload |
| `Enemy.java` | The enemy bot, subclass of player|
| `Bullet.java` | projectiles flying ons creen |
| `Vector.java` | 2D vector math |

The high score is stored in `highscore.txt` in the folder you run the game from. Delete it to reset.




made by @WenxiWangBuilds


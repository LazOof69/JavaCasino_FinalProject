# Java Casino

A Java Swing-based casino gaming application featuring multiple popular casino games, a user management system, and persistent local data storage.

## Features

- **User Accounts**: Register and login system for players
- **Persistent Data**: Player balances, game history, and statistics are saved locally
- **Player Progression**: Leveling system and daily bonuses
- **Leaderboards**: Track top players by balance, win rate, and games played

## Mini-Games Included

- **Blackjack (21點)**: Classic card game against the dealer
- **Slot Machine (老虎機)**: Standard 3x3 slot machine with multiple payout lines
- **Roulette (輪盤)**: Bet on numbers, colors, or odds/evens
- **Dice Game (骰子)**: Simple dice betting game
- **Lucky Wheel (幸運輪盤)**: Spin the wheel for a chance to win various prizes

## How to Build and Run

### Prerequisites
- Java Development Kit (JDK) 8 or higher
- Windows OS (for the batch scripts)

### Build
To compile the project and create the executable JAR file, run:
```cmd
build.bat
```
*Note: If `jar` is not in your PATH, it will create a `JavaCasino_Run.bat` to run the compiled class files instead.*

### Run
To start the application, simply run:
```cmd
run.bat
```
*(This script will automatically build the project first if no compiled files are found.)*

## Architecture & Technology

- Pure Java `Swing` for GUI
- Model-View-Controller (MVC) like pattern separation (`model`, `gui`, `service`, `game/card`)
- Object Serialization for local data persistence in `resources/data/`

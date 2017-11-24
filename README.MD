# The "Mastermind" bot
The basic idea of the bot is that it commands units on a macro scale. Based upon the environment and game data, it will generate 
"directives". These have a set of parameters including priority to automatically manage and assign ships to complete this directive.
This allows for bot logic to ignore individual ship management, instead the ships are deployed in a predictable and effective manner.

## Halite SDK Notes
This bot is built upon the kotlin halite starter kit. However, this has been modified for this bot's use. Everything based on the starter
kit is under the `halite` package and my wrapper over this kit is in the `wrapper` package.

## Deploying the bot
**Note:** These scripts expect the [Halite Client tool](https://halite.io/learn-programming-challenge/halite-cli-and-tools/halite-client-tools) to be installed.

### Running the bot locally against itself
Simply run `./localtest.sh`

### Deploying the bot to halite.io
First authenticate yourself with the halite client tool (only needed for first use) using `hlt auth`, then run `./deploy.sh`.

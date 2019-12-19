#!/bin/bash

java -cp target/AI-Pacman-0.0.1-SNAPSHOT.jar edu.ucsb.cs56.projects.games.pacman.PacMan -numGhosts 1 -loopDelay 40 -autoPlay -nBackgroundPlayers 4 -aiPlayerClassName edu.ucsb.cs56.projects.games.pacman.AIPlayerLearner -aiModelTrainerClassName edu.ucsb.cs56.projects.games.pacman.AIModelTrainerDeepLearning -leaderBoard pacmanleaderboardsingle.ser

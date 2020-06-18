#!/bin/bash

# Create docker container
#docker run --hostname my-rabbit --name some-rabbit -p 5671:5671 -p 5672:5672 -p 25672:25672 -p 15672:15672 -p 4369:4369 -d rabbitmq:3-management

# Start docker
docker start some-rabbit

java -cp target/AI-Pacman-0.0.1-SNAPSHOT-jar-with-dependencies.jar edu.ucsb.cs56.projects.games.pacman.PacMan -numGhosts 1 -loopDelay 40 -autoPlay -nBackgroundPlayers 4 -aiPlayerClassName edu.ucsb.cs56.projects.games.pacman.AIPlayerLearner -aiModelTrainerClassName edu.ucsb.cs56.projects.games.pacman.AIModelTrainerDeepLearning -leaderBoard pacmanleaderboardsingle.ser

# AI-Pacman
Various implementations of algorithms to learn what AI algorithms are suitable for online learning of a Pacman game.

Check the wiki for documentation.

You can either use Eclipse to edit and run (.project and .classpath) included, or run it from the command line just typing

When running there is a file (EventStream.csv) written in the root directory of the project. This is the event trace of the run. Work is in progress to dump out more data. Currently you just get the GameID, StepID, and event type for each event. 

The leaderboard doesn't work. We could add something that automatically updates the leader board using the class name as the user. That way we could automatically track which implementation is the current champion.

To implement your own AIPlayer, just subclass AIPlayer and implement the dataEvent method. Call keyPress to press keys, and get current game events and data from the input DataEvent. You will get a callback for each game tick, 25 per second if you set loopDelay to 40, plus one for each major game event (INTRO, EAT_PILL, etc.). Look at the event log or the DataEventType enum for event types. Take a peek at the AIPlayerRandom to understand how you can do a simple implementation. This AIPlayer just performs a randow walk through the grid.

Needed for true AI play:

- Add more data elements to DataEvents to cover Character positions etc.
- Add an online training framework AITrainer which takes completed games from MultiGame controller and presents them to the AITrainer for realtime learning

New nice to have features:

- Allow AIPlayer to submit scores on leader board

# AI-Pacman
Various implementations of algorithms to learn what AI algorithms are suitable for online learning of a Pacman game.

You can either use Eclipse to edit and run (.project and .classpath) included, or run it from the command line just typing

% ant run       
This requires that you install ant if don't have it. If someone wants to change it to maven we could have it pull down whatever machine learning library each AIPlayer needs for its implemetation.

When running there is a file (EventStream.csv) written in the root directory of the project. This is the event trace of the run. Work is in progress to dump out more data. Currently you just get the GameID, StepID, and event type for each event. 

The leaderboard doesn't work. We could add something that automatically updates the leader board using the class name as the user. That way we could automatically track which implementation is the current champion.

Command line switches are:

-loopDelay 

Delay between update of the game. This does not affect the rendering speed. Default value is 40, which makes the game run at standard human speed.

-autoPlay 

Runs the AIPayerRandom implementation of AIPlayer. Next step is to add dynamic class loading so you can kick off your own implementation.

-headLess

Runs the program without the graphical display. Only output is the event log file.

To implement your own AIPlayer, just subclass AIPlayer and implement the dataEvent method. Call keyPress to press keys, and get current game events and data from the input DataEvent. You will get a callback for each game tick, 25 per second if you set loopDelay to 40, plus one for each major game event (INTRO, EAT_PILL, etc.). Look at the event log or the DataEventType enum for event types. Take a peek at the AIPlayer to understand how you can do a simple implementation.

Basic bug fixes to-do:

- Fix Heisenbug requiring the user to press s within a few seconds of start when playing manually

Needed for true AI play:

- Add more data elements to DataEvents to cover Character positions etc.
- Add dynamic class loading to allow users to run their own AIPlayers
- Add a MultiGame controller to allow many concurrent -headLess games to run for testing
- Add an online training framework AITrainer which takes completed games from MultiGame controller and presents them to the AITrainer for realtime learning

New nice to have features:

- Allow AIPlayer to submit scores on leader board

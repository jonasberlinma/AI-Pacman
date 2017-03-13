package edu.ucsb.cs56.projects.games.pacman;

import java.io.FileNotFoundException;

public class AIGame implements Runnable {

	private AIPlayer aiPlayer = null;
	private Board board = null;
	private BoardRenderer boardRenderer;
	private String leaderBoard;
	private Thread aiGameThread;
	private boolean isRunning = false;

	public AIGame(String aiPlayerClassName, int loopDelay, boolean background)
			throws FileNotFoundException, ClassNotFoundException, InstantiationException, IllegalAccessException {
		board = new Board(!background);
		board.setLoopDelay(loopDelay);
		Class<?> theClass = Class.forName(aiPlayerClassName);

		aiPlayer = (AIPlayer) theClass.newInstance();
		aiPlayer.setBoard(board);
		aiGameThread = new Thread(this, "AI Game Player 1");
	}

	public void addBoardRendered(BoardRenderer boardRenderer, String leaderBoard) {
		this.boardRenderer = boardRenderer;
		this.leaderBoard = leaderBoard;
		board.addBoardRenderer(boardRenderer);
	}

	public Board getBoard() {
		return this.board;
	}

	public void start() {
		aiGameThread.start();
	}

	public void join() throws InterruptedException {
		aiGameThread.join();
	}

	public boolean isRunning(){
		return isRunning;
	}
	@Override
	public void run() {
		isRunning = true;
		if (boardRenderer != null) {
			boardRenderer.callLeaderboardMain(leaderBoard);

			boardRenderer.start();
		}
		aiPlayer.start();
		board.start();
		try {
			aiPlayer.join();
			board.stop();
			board.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		isRunning = false;
	}

}

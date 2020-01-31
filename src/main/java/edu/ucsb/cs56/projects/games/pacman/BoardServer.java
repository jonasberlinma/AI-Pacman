package edu.ucsb.cs56.projects.games.pacman;

import java.util.Vector;

public class BoardServer implements BoardInterface {

	private Board board;

	public BoardServer(Board board) {
		this.board = board;
	}

	@Override
	public int getBlocksize() {
		return board.getBlocksize();
	}

	@Override
	public int getNumblocks() {
		return board.getNumblocks();
	}

	@Override
	public int getScrsize() {
		return board.getScrsize();
	}

	@Override
	public Grid getGrid() {
		return board.getGrid();
	}

	@Override
	public PacPlayer getMsPacman() {
		return board.getMsPacman();
	}

	@Override
	public PacPlayer getPacman() {
		return board.getPacman();
	}

	@Override
	public Vector<Ghost> getGhosts() {
		return board.getGhosts();
	}

	@Override
	public GameType getGameType() {
		return board.getGameType();
	}

	@Override
	public int getNumPellet() {
		return board.getNumPellet();
	}

	@Override
	public int getScore() {
		return board.getScore();
	}

	@Override
	public void keyPressed(int key) {
		board.keyPressed(key);
	}

	@Override
	public void keyReleased(int key) {
		board.keyReleased(key);
	}

	@Override
	public boolean doPlayAudio() {
		return board.doPlayAudio();
	}

	@Override
	public int getAudioClipID() {
		return board.getAudioClipID();
	}

}

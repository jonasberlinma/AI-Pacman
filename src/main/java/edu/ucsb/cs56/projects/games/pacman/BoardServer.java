package edu.ucsb.cs56.projects.games.pacman;

import java.io.IOException;
import java.util.Vector;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

public class BoardServer implements BoardInterface {

	private Board board;
	private ObjectMapper objectMapper = null;

	public BoardServer(Board board) {
		this.board = board;
		objectMapper = new ObjectMapper();
		objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
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
		
		String json = null;
		PacPlayer pacPlayer = null;
		try {
			json = objectMapper.writeValueAsString(board.getPacman());
			pacPlayer = objectMapper.readValue(json, PacPlayer.class);
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return pacPlayer;
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

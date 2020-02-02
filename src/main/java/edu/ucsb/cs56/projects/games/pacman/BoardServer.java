package edu.ucsb.cs56.projects.games.pacman;

import java.io.IOException;
import java.util.Vector;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class BoardServer implements BoardInterface {

	private Board board;
	private ObjectMapper objectMapper = null;
	private boolean remote;

	public BoardServer(Board board, boolean remote) {
		this.board = board;
		this.remote = remote;
		objectMapper = new ObjectMapper();
		objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
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
		String json = null;
		Grid grid = null;
		if (remote) {
			try {
				json = objectMapper.writeValueAsString(board.getGrid());
				grid = objectMapper.readValue(json, Grid.class);
			} catch (JsonProcessingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			grid = board.getGrid();
		}
		return grid;
	}

	@Override
	public PacPlayer getMsPacman() {
		String json = null;
		PacPlayer pacPlayer = null;
		if (remote) {
			try {
				json = objectMapper.writeValueAsString(board.getMsPacman());
				pacPlayer = objectMapper.readValue(json, PacPlayer.class);
			} catch (JsonProcessingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			pacPlayer = board.getMsPacman();
		}

		return pacPlayer;
	}

	@Override
	public PacPlayer getPacman() {

		String json = null;
		PacPlayer pacPlayer = null;
		if (remote) {
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
		} else {
			pacPlayer = board.getPacman();
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

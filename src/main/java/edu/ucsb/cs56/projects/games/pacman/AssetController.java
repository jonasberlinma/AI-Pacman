package edu.ucsb.cs56.projects.games.pacman;

import java.awt.Image;
import java.io.IOException;

import javax.imageio.ImageIO;

import edu.ucsb.cs56.projects.games.pacman.Character.PlayerType;

public class AssetController {
	private static AssetController instance = null;

	public String assetImagePath;
	private String assetAudioPath;
	private String assetPacmanImagePath;
	private String assetMsPacmanImagePath;

	public Image[][] pacmanUp, pacmanDown, pacmanLeft, pacmanRight;
	public Audio[] pacmanAudio;

	private Audio beginningAudio;
	
	private Image ghostImage[];
	private Image scaredGhostImage;

	private AssetController() {
		assetImagePath = "assets/pacman/";
		assetAudioPath = "assets/audio/";
		assetPacmanImagePath = "assets/pacman/";
		assetMsPacmanImagePath = "assets/mspacman/";
		System.out.println("Loading assets");
		loadPacmanImages();
		loadGhostImages();
		loadAudio();

	}

	public static AssetController getInstance() {
		if (instance == null) {
			instance = new AssetController();
		}
		return instance;
	}

	public void loadPacmanImages() {
		pacmanUp = new Image[2][4];
		pacmanDown = new Image[2][4];
		pacmanLeft = new Image[2][4];
		pacmanRight = new Image[2][4];
		for (int i = 0; i < 2; i++) {
			switch (i) {
			case 0:
				assetImagePath = assetPacmanImagePath;
				break;
			case 1:
				assetImagePath = assetMsPacmanImagePath;
			}
			try {
				pacmanUp[i][0] = ImageIO.read(getClass().getResource(assetImagePath + "pacmanup.png"));
				pacmanUp[i][1] = ImageIO.read(getClass().getResource(assetImagePath + "up1.png"));
				pacmanUp[i][2] = ImageIO.read(getClass().getResource(assetImagePath + "up2.png"));
				pacmanUp[i][3] = ImageIO.read(getClass().getResource(assetImagePath + "up3.png"));
				pacmanDown[i][0] = ImageIO.read(getClass().getResource(assetImagePath + "pacmandown.png"));
				pacmanDown[i][1] = ImageIO.read(getClass().getResource(assetImagePath + "down1.png"));
				pacmanDown[i][2] = ImageIO.read(getClass().getResource(assetImagePath + "down2.png"));
				pacmanDown[i][3] = ImageIO.read(getClass().getResource(assetImagePath + "down3.png"));
				pacmanLeft[i][0] = ImageIO.read(getClass().getResource(assetImagePath + "pacmanleft.png"));
				pacmanLeft[i][1] = ImageIO.read(getClass().getResource(assetImagePath + "left1.png"));
				pacmanLeft[i][2] = ImageIO.read(getClass().getResource(assetImagePath + "left2.png"));
				pacmanLeft[i][3] = ImageIO.read(getClass().getResource(assetImagePath + "left3.png"));
				pacmanRight[i][0] = ImageIO.read(getClass().getResource(assetImagePath + "pacmanright.png"));
				pacmanRight[i][1] = ImageIO.read(getClass().getResource(assetImagePath + "right1.png"));
				pacmanRight[i][2] = ImageIO.read(getClass().getResource(assetImagePath + "right2.png"));
				pacmanRight[i][3] = ImageIO.read(getClass().getResource(assetImagePath + "right3.png"));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Load game sprites from images folder
	 */

	public void loadGhostImages() {
		
		ghostImage = new Image[2];
		assetImagePath = "assets/";
		try {
			ghostImage[0] = ImageIO.read(getClass().getResource(assetImagePath + "ghostred.png"));
			ghostImage[1] = ImageIO.read(getClass().getResource(assetImagePath + "ghostpink.png"));
			scaredGhostImage = ImageIO.read(getClass().getResource(assetImagePath + "ghostblue.png"));
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	/**
	 * Load game audio from audio folder
	 */
	public void loadAudio() {
		try {
			String[] sounds = { "chomp.wav", "eatfruit.wav" };
			pacmanAudio = new Audio[sounds.length];
			for (int i = 0; i < sounds.length; i++) {
				pacmanAudio[i] = new Audio(getClass().getResourceAsStream(assetAudioPath + sounds[i]));
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
		try {
			this.beginningAudio = new Audio(getClass().getResourceAsStream(assetAudioPath + "beginning.wav"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Plays a sound from pacman audio array.
	 *
	 * @param sound
	 *            sound effect ID
	 */
	public void playAudio(int sound) {
		try {
			pacmanAudio[sound].play();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public Image getLifeImage(PlayerType playerType) {
		return pacmanRight[playerType.ordinal()][3];
	}
	public void playIntroAudio(){
		try {
			this.beginningAudio.play();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public Image getScaredGhostImage(){
		return this.scaredGhostImage;
	}
	public Image getGhostImage(PlayerType playerType){
		Image ret = null;
		if(playerType == PlayerType.GHOST1){
			ret = ghostImage[0];
		} else if (playerType == PlayerType.GHOST2){
			ret = ghostImage[1];
		}
		return ret;
	}
}

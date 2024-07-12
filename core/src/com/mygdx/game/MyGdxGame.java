package com.mygdx.game;

import static com.mygdx.game.GameSettings.POSITION_ITERATIONS;
import static com.mygdx.game.GameSettings.STEP_TIME;
import static com.mygdx.game.GameSettings.VELOCITY_ITERATIONS;
import static com.mygdx.game.GraphicsSettings.END_HISTORY_ARRAY;
import static com.mygdx.game.GraphicsSettings.PLANET_HISTORY_ARRAY;
import static com.mygdx.game.GraphicsSettings.SPACE_HISTORY_ARRAY;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Box2D;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.TimeUtils;
import com.mygdx.game.manager.AudioManager;
import com.mygdx.game.manager.LevelMapManager;
import com.mygdx.game.manager.MemoryManager;
import com.mygdx.game.screens.HistoryScreen;
import com.mygdx.game.screens.MemoriesScreen;
import com.mygdx.game.screens.MenuScreen;
import com.mygdx.game.screens.PlanetGameScreen;
import com.mygdx.game.screens.SpaceGameScreen;

public class MyGdxGame extends Game {
	public World space;
	public World planet;

	public boolean canAccessPlanetLevel;
	private float accumulator;

	public State state;


	public SpriteBatch batch;
	public Vector3 touch;
	public OrthographicCamera camera;
	public SpaceGameScreen spaceScreen;
	public PlanetGameScreen planetScreen;
	public MenuScreen menuScreen;
	public MemoriesScreen memoriesScreen;
	public BitmapFont commonWhiteFont;
	public BitmapFont averageWhiteFont;
	public LevelMapManager levelMapManager;
	public HistoryScreen spaceHistory;
	public HistoryScreen planetHistory;
	public HistoryScreen endHistory;
	public boolean isContinue;
	public boolean needToSpaceIntro;
	public boolean needToPlanetIntro;

	public AudioManager audioManager;
	@Override
	public void create () {
		MemoryManager.clear();
		needToSpaceIntro = !MemoryManager.lodeNeedToSpaceIntro();
		needToPlanetIntro = !MemoryManager.loadNeedToPlanetIntro();
		canAccessPlanetLevel = MemoryManager.loadIsNextLevel();
		isContinue = false;

		Box2D.init();
		space = new World(new Vector2(0, 0), true);
		planet = new World(new Vector2((float) GameSettings.GRAVITY_PLANET_X, (float) GameSettings.GRAVITY_PLANET_Y), true);

		commonWhiteFont = FontBuilder.generate(50, Color.WHITE, GameResources.FONT_PATH);
		averageWhiteFont = FontBuilder.generate(25, Color.WHITE, GameResources.FONT_PATH);

		batch = new SpriteBatch();
		camera = new OrthographicCamera();
		camera.setToOrtho(false, GameSettings.SCREEN_WIDTH, GameSettings.SCREEN_HEIGHT);

		spaceScreen = new SpaceGameScreen(this);
		planetScreen = new PlanetGameScreen(this);
		menuScreen = new MenuScreen(this);
		memoriesScreen = new MemoriesScreen(this);
		spaceHistory = new HistoryScreen(this, SPACE_HISTORY_ARRAY, spaceScreen, false);
		planetHistory = new HistoryScreen(this, PLANET_HISTORY_ARRAY, planetScreen, false);
		endHistory = new HistoryScreen(this, END_HISTORY_ARRAY, menuScreen, true);
		audioManager = new AudioManager();
		state = State.ENDED;
		setScreen(menuScreen);

//		planetLevel();

		//setScreen(planetHistory);

		levelMapManager = new LevelMapManager();
	}

	public void stepWorld(World world) {
		float delta = Gdx.graphics.getDeltaTime();
		accumulator += Math.min(delta, 0.25f);

		if (accumulator >= STEP_TIME) {
			accumulator -= STEP_TIME;
			world.step(STEP_TIME, VELOCITY_ITERATIONS, POSITION_ITERATIONS);
		}
	}

	public void spaceLevel() {
		setScreen(spaceScreen);
		Gdx.input.setInputProcessor(spaceScreen);
		audioManager.menuMusic.stop();
		audioManager.spaceMusic.play();
		audioManager.planetMusic.stop();
	}

	public void planetLevel() {
		setScreen(planetScreen);
		Gdx.input.setInputProcessor(planetScreen);
		audioManager.menuMusic.stop();
		audioManager.spaceMusic.stop();
		audioManager.planetMusic.play();
	}

	public void mainMenuMusic() {
		audioManager.menuMusic.play();
		audioManager.spaceMusic.stop();
		audioManager.planetMusic.stop();
	}

	public void showSpaceIntro() {
		needToSpaceIntro = false;
		MemoryManager.saveNeedToSpaceIntro(true);
	}

	public void showPlanetIntro() {
		needToPlanetIntro = false;
		MemoryManager.saveNeedToPlanetIntro(true);
	}

	public void passSpaceLevel() {
		canAccessPlanetLevel = true;
		MemoryManager.saveIsNextLevel(true);
	}

	public void passPlanetLevel() {

	}
	
	@Override
	public void dispose () {
		batch.dispose();
	}
}

package com.mygdx.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.TimeUtils;
import com.mygdx.game.GameResources;
import com.mygdx.game.MyGdxGame;
import com.mygdx.game.components.ButtonView;
import com.mygdx.game.components.JoystickView;

public abstract class GameScreen extends ScreenAdapter {
    MyGdxGame myGdxGame;
    JoystickView joystick;
    long showTime;
    State state;
    ButtonView pauseButton;

    public GameScreen(MyGdxGame game) {
        this.myGdxGame = game;
    }

    @Override
    public void show() {
        joystick = new JoystickView(100, 100);
        pauseButton =new ButtonView(1200, 650, 50, 50, GameResources.PAUSE_ICON_IMG_PATH); // "pause_icon.png"
        showTime = TimeUtils.millis();
    }

    @Override
    public void render(float delta) {
        handleInput();
        myGdxGame.camera.update();
        myGdxGame.batch.setProjectionMatrix(myGdxGame.camera.combined);

        ScreenUtils.clear(Color.CLEAR);

        myGdxGame.batch.begin();
        draw();
        myGdxGame.batch.end();
    }

    protected void draw() {
        joystick.draw(myGdxGame.batch);
        pauseButton.draw(myGdxGame.batch);
    }

    protected void moveCamera(Vector2 move) {
        myGdxGame.camera.position.x += move.x;
        myGdxGame.camera.position.y += move.y;
    }

    protected void handleInput() {
        if (TimeUtils.millis() - showTime < 100) return;
        if (Gdx.input.isTouched()) {
            Vector3 touch = myGdxGame.camera.unproject(new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0));
            joystick.onTouch((int) touch.x, (int) touch.y);
        } else {
            joystick.toDefault();
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        joystick.dispose();
        pauseButton.dispose();
    }

    enum State {
        play,
        pause
    }
}
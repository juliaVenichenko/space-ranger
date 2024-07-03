package com.mygdx.game.screens;

import static com.mygdx.game.GameResources.CORE_IMG_PATH;
import static com.mygdx.game.GameResources.ENEMY_SHIP_IMG_PATH;
import static com.mygdx.game.GameSettings.BULLET_HEIGHT;
import static com.mygdx.game.GameSettings.CORE_HEIGHT;
import static com.mygdx.game.GameSettings.CORE_WIDTH;
import static com.mygdx.game.GameSettings.SCREEN_HEIGHT;
import static com.mygdx.game.GameSettings.SCREEN_WIDTH;
import static java.lang.Math.cos;
import static java.lang.Math.random;
import static java.lang.Math.sin;
import static java.lang.Math.toRadians;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.TimeUtils;
import com.mygdx.game.GameResources;
import com.mygdx.game.GameSession;
import com.mygdx.game.GameSettings;
import com.mygdx.game.MyGdxGame;
import com.mygdx.game.components.JoystickView;
import com.mygdx.game.components.ButtonView;
import com.mygdx.game.components.ImageView;
import com.mygdx.game.components.LiveView;
import com.mygdx.game.components.MovingBackgroundView;
import com.mygdx.game.components.TextView;
import com.mygdx.game.manager.ContactManager;
import com.mygdx.game.objects.BulletObject;
import com.mygdx.game.objects.CoreObject;
import com.mygdx.game.objects.EnemyObject;
import com.mygdx.game.objects.ShipObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;


public class SpaceGameScreen extends GameScreen {

    GameSession gameSession;
    MyGdxGame myGdxGame;

    Random random;

    // Objects
    public ShipObject shipObject;
    ArrayList<BulletObject> bulletArray;

    ArrayList<CoreObject> coreArray;

    ArrayList<EnemyObject> enemyArray;
    ContactManager contactManager;
    MovingBackgroundView backgroundView;
    ButtonView fireButton;
    ImageView backgroundFireButton;
    TextView purpose;
    LiveView live;

    public SpaceGameScreen(MyGdxGame myGdxGame) {
        super(myGdxGame);
        this.myGdxGame = myGdxGame;
        backgroundView = new MovingBackgroundView(GameResources.BACKGROUND_IMG_PATH);
        contactManager = new ContactManager(myGdxGame.world);
        shipObject = new ShipObject(
                GameSettings.SCREEN_WIDTH / 2, GameSettings.SCREEN_HEIGHT / 2,
                GameSettings.SHIP_WIDTH, GameSettings.SHIP_HEIGHT,
                GameResources.SHIP_IMG_PATH,
                myGdxGame.world
        );
        fireButton = new ButtonView(1050, 150, 100, 100, GameResources.FIRE_BUTTON_IMG_PATH); // "Remove-bg.ai_1720009081104.png"
        backgroundFireButton = new ImageView(1000, 100, GameResources.JOYSTICK_BACK_IMG_PATH);
        bulletArray = new ArrayList<>();
        coreArray = new ArrayList<>();
        enemyArray = new ArrayList<>();
        random = new Random();
        gameSession = new GameSession();
        purpose = new TextView(myGdxGame.averageWhiteFont, 500, 675, "Purpose: energy: .../3");
        live = new LiveView(0, 675);
    }

    @Override
    public void show() {
        // Генерация врагов и ядер (просто, чтобы было видно)
        generateCore();
        generateEnemy();
        joystick = new JoystickView(100, 100);
        showTime = TimeUtils.millis();
    }



    @Override
    public void render(float delta) {
        super.render(delta);
        final int padding = 10;
        if (shipObject.needToShoot()) {
            BulletObject Bullet = new BulletObject(
                    (int) (shipObject.getX() + cos(toRadians(shipObject.getRotation())) * (shipObject.getRadius() / 2 + BULLET_HEIGHT + padding)),
                    (int) (shipObject.getY() + sin(toRadians(shipObject.getRotation())) * (shipObject.getRadius() / 2 + BULLET_HEIGHT + padding)),
                    GameSettings.BULLET_WIDTH, BULLET_HEIGHT,
                    GameResources.BULLET_IMG_PATH,
                    myGdxGame.world, shipObject.getRotation()
            );
            bulletArray.add(Bullet);
        }
        for (EnemyObject enemy: enemyArray) enemy.move();
        live.setLeftLives(shipObject.getLivesLeft());
        myGdxGame.stepWorld();
        updateBullets();
        updateCore();
        updateEnemy();
        if (gameSession.victory())
            System.out.println("You Won!");
    }

    @Override
    protected void draw() {
        backgroundView.draw(myGdxGame.batch);
        shipObject.draw(myGdxGame.batch);
        for (BulletObject bullet : bulletArray) bullet.draw(myGdxGame.batch);
        for (CoreObject core: coreArray) core.draw(myGdxGame.batch);
        for (EnemyObject enemy: enemyArray) enemy.draw(myGdxGame.batch);
        backgroundFireButton.draw(myGdxGame.batch);
        fireButton.draw(myGdxGame.batch);
        purpose.draw(myGdxGame.batch);
        live.draw(myGdxGame.batch);
        super.draw();
    }

    @Override
    protected void moveCamera(Vector2 move) {
        super.moveCamera(move);
        joystick.onCameraUpdate(move.x, move.y);
        backgroundView.move(move.x, move.y);
    }

    @Override
    protected void handleInput() {
        super.handleInput();
        if (TimeUtils.millis() - showTime < 100) return;
        if (Gdx.input.isTouched()) {
            //myGdxGame.touch = myGdxGame.camera.unproject(new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0));
            shipObject.setRotation(joystick.getDegrees());
            Vector2 difference = shipObject.move();
            moveCamera(difference);
        }
    }


    // "Чистилки" объектов
    private void updateBullets() {
        // For Ship
        Iterator<BulletObject> iterator = bulletArray.iterator();
        while(iterator.hasNext()) {
            BulletObject bulletObject_now = iterator.next();
            if (bulletObject_now.destroy(shipObject.getX(), shipObject.getY())) {
                myGdxGame.world.destroyBody(bulletObject_now.body);
                iterator.remove();
            }
        }
    }

    private void updateCore() {
        Iterator<CoreObject> iterator = coreArray.iterator();
        while(iterator.hasNext()) {
            CoreObject core = iterator.next();
            if (core.destroy()) {
                gameSession.core_was_collected();
                myGdxGame.world.destroyBody(core.body);
                iterator.remove();
            }
        }
    }

    private void updateEnemy() {
        Iterator<EnemyObject> iterator = enemyArray.iterator();
        while(iterator.hasNext()) {
            EnemyObject enemy = iterator.next();
            if (enemy.destroy()) {
                myGdxGame.world.destroyBody(enemy.body);
                iterator.remove();
            }
        }
    }

    // Генераторы
    private void generateCore() {
        for(int i = 0; i < 3; i++) {
            CoreObject coreObject = new CoreObject(
                    shipObject.getX() - random.nextInt(SCREEN_WIDTH / 3) * 5,
                    shipObject.getY() - random.nextInt(SCREEN_HEIGHT / 3) * 5,
                    CORE_WIDTH, CORE_HEIGHT, myGdxGame.world,
                    CORE_IMG_PATH
            );
            coreArray.add(coreObject);
        }
    }

    private void generateEnemy() {
        for(int i = 0; i < 3; i++) {
            EnemyObject enemy = new EnemyObject(
                    shipObject.getX() - random.nextInt(SCREEN_WIDTH / 4) * 6,
                    shipObject.getY() - random.nextInt(SCREEN_HEIGHT / 4) * 6,
                    CORE_WIDTH, CORE_HEIGHT, myGdxGame.world,
                    ENEMY_SHIP_IMG_PATH
            );
            enemyArray.add(enemy);
        }
    }
}

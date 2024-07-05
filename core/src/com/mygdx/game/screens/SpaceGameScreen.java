package com.mygdx.game.screens;

import static com.mygdx.game.GameResources.CORE_IMG_PATH;
import static com.mygdx.game.GameResources.ENEMY_SHIP_IMG_PATH;
import static com.mygdx.game.GameSettings.BULLET_HEIGHT;
import static com.mygdx.game.GameSettings.Bullet_Speed;
import static com.mygdx.game.GameSettings.CHANCE_CORE_SPAWN;
import static com.mygdx.game.GameSettings.CORE_HEIGHT;
import static com.mygdx.game.GameSettings.CORE_WIDTH;
import static com.mygdx.game.GameSettings.ENEMY_HEIGHT;
import static com.mygdx.game.GameSettings.ENEMY_WIDTH;
import static com.mygdx.game.GameSettings.SCREEN_HEIGHT;
import static com.mygdx.game.GameSettings.SCREEN_WIDTH;
import static com.mygdx.game.State.PAUSED;
import static com.mygdx.game.State.PLAYING;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.toRadians;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.TimeUtils;
import com.mygdx.game.EntitySpawner;
import com.mygdx.game.GameResources;
import com.mygdx.game.GameSession;
import com.mygdx.game.GameSettings;
import com.mygdx.game.MyGdxGame;
import com.mygdx.game.components.ButtonView;
import com.mygdx.game.components.ImageView;
import com.mygdx.game.components.JoystickView;
import com.mygdx.game.components.LiveView;
import com.mygdx.game.components.MovingBackgroundView;
import com.mygdx.game.components.TextView;
import com.mygdx.game.manager.ContactManager;
import com.mygdx.game.objects.BoomObject;
import com.mygdx.game.objects.BulletObject;
import com.mygdx.game.objects.CoreObject;
import com.mygdx.game.objects.EnemyObject;
import com.mygdx.game.objects.ShipObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;


public class SpaceGameScreen extends GameScreen {
    MyGdxGame myGdxGame;

    Random random;

    // Objects
    public ShipObject shipObject;
    ArrayList<BulletObject> bulletArray;
    ArrayList<CoreObject> coreArray;
    ArrayList<BoomObject> boomArray;

    ArrayList<EnemyObject> enemyArray;
    ContactManager contactManager;
    MovingBackgroundView backgroundView;
    ButtonView fireButton;
    ImageView backgroundFireButton;
    TextView purpose;
    LiveView live;
    boolean isTouchedShoot;
    Random rd;
    EntitySpawner spawner;

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
        fireButton = new ButtonView(1113, 75, 100, 100, GameResources.FIRE_BUTTON_IMG_PATH); // "Remove-bg.ai_1720009081104.png"
        backgroundFireButton = new ImageView(1060, 25, GameResources.JOYSTICK_BACK_IMG_PATH);
        joystick = new JoystickView(25, 25);
        bulletArray = new ArrayList<>();
        coreArray = new ArrayList<>();
        enemyArray = new ArrayList<>();
        random = new Random();
        gameSession = new GameSession();
        purpose = new TextView(myGdxGame.averageWhiteFont, 500, 675, "Purpose: energy: 0/3");
        live = new LiveView(0, 675);
        isTouchedShoot = false;
        rd = new Random();
        spawner = new EntitySpawner();
        boomArray = new ArrayList<>();
    }

    @Override
    public void show() {
        super.show();
    }

    @Override
    public void render(float delta) {
        super.render(delta);
        if (!shipObject.isEnd()) {
            final int padding = 70;
            if (isTouchedShoot && shipObject.needToShoot()) {
                BulletObject Bullet = new BulletObject(
                        (int) (shipObject.getX() + cos(toRadians(shipObject.getRotation())) * (shipObject.getRadius() / 2 + BULLET_HEIGHT + padding)),
                        (int) (shipObject.getY() + sin(toRadians(shipObject.getRotation())) * (shipObject.getRadius() / 2 + BULLET_HEIGHT + padding)),
                        GameSettings.BULLET_WIDTH, BULLET_HEIGHT,
                        GameResources.BULLET_IMG_PATH,
                        myGdxGame.world, shipObject.getRotation(), Bullet_Speed, false
                );
                bulletArray.add(Bullet);
            }
            if (gameSession.shouldSpawn()) {
                if (rd.nextInt(100) < CHANCE_CORE_SPAWN) generateCore();
                else generateEnemy();
            }
            for (EnemyObject enemy : enemyArray) {
                BulletObject bullet = enemy.move(shipObject.getX(), shipObject.getY());
                if (bullet != null) bulletArray.add(bullet);
            }
            live.setLeftLives(shipObject.getLivesLeft());
            myGdxGame.stepWorld();
            updateBullets();
            updateCore();
            updateEnemy();
            updateBoom();
            if (gameSession.victory()) {
                System.out.println("You Won!");
                shipObject.moleHoleAnim();
            }
            if (joystick.isTouched()) {
                shipObject.setRotation(joystick.getDegrees());
                shipObject.move();
            }
            myGdxGame.camera.position.x = shipObject.getX();
            myGdxGame.camera.position.y = shipObject.getY();
            backgroundView.move(myGdxGame.camera.position.x,
                    myGdxGame.camera.position.y);
            for (BoomObject boomObject : boomArray) boomObject.Boom_action();
        } else {
            myGdxGame.secondLevel();
        }
    }

    @Override
    protected void drawStatic() {
        backgroundFireButton.draw(myGdxGame.batch);
        fireButton.draw(myGdxGame.batch);
        purpose.draw(myGdxGame.batch);
        live.draw(myGdxGame.batch);
        super.drawStatic();
    }

    @Override
    protected void drawDynamic() {
        backgroundView.draw(myGdxGame.batch);
        for (BoomObject boom: boomArray) boom.draw(myGdxGame.batch);
        shipObject.draw(myGdxGame.batch);
        for (BulletObject bullet : bulletArray) bullet.draw(myGdxGame.batch);
        for (CoreObject core: coreArray) core.draw(myGdxGame.batch);
        for (EnemyObject enemy: enemyArray) enemy.draw(myGdxGame.batch);
        super.drawDynamic();
    }

    @Override
    protected void moveCamera(Vector2 move) {
        super.moveCamera(move);
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
                if (core.wasCollected) {
                    gameSession.core_was_collected();
                    purpose.setText(String.format("Purpose: energy: %d/3", gameSession.getCoreCollected()));
                }
                myGdxGame.world.destroyBody(core.body);
                boomArray.add(new BoomObject(core.x, core.y));
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
                BoomObject boom = new BoomObject(enemy.getX(), enemy.getY());
                boomArray.add(boom);
                iterator.remove();
            }
        }
    }

    private void updateBoom() {
        Iterator<BoomObject> iterator = boomArray.iterator();
        while (iterator.hasNext()) {
            BoomObject boom = iterator.next();
            if (boom.isNotAlive()) {
                iterator.remove();
            }
        }
    }

    // Генераторы
    private void generateCore() {
        EntitySpawner.Pair pair = spawner.newPair(shipObject.getX(), shipObject.getY(), CORE_WIDTH / 2, CORE_HEIGHT / 2);
        CoreObject coreObject = new CoreObject(
                (int) pair.x, (int) pair.y,
                CORE_WIDTH, CORE_HEIGHT, myGdxGame.world,
                CORE_IMG_PATH
        );
        coreArray.add(coreObject);
    }

    private void generateEnemy() {
        EntitySpawner.Pair pair = spawner.newPair(shipObject.getX(), shipObject.getY(), ENEMY_WIDTH / 2, ENEMY_HEIGHT / 2);
        EnemyObject enemy = new EnemyObject(
                (int) pair.x, (int) pair.y,
                ENEMY_WIDTH, ENEMY_HEIGHT, myGdxGame.world,
                ENEMY_SHIP_IMG_PATH
        );
        enemyArray.add(enemy);
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        super.touchDown(screenX, screenY, pointer, button);
        screenX = Math.round((float) screenX * (float) SCREEN_WIDTH / (float) Gdx.graphics.getWidth());
        screenY = Math.round((float) screenY * (float) SCREEN_HEIGHT / (float) Gdx.graphics.getHeight());
        if (backgroundFireButton.isHit(screenX, SCREEN_HEIGHT - screenY)) isTouchedShoot = true;
        return true;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        super.touchUp(screenX, screenY, pointer, button);
        screenX = Math.round((float) screenX * (float) SCREEN_WIDTH / (float) Gdx.graphics.getWidth());
        screenY = Math.round((float) screenY * (float) SCREEN_HEIGHT / (float) Gdx.graphics.getHeight());
        if (backgroundFireButton.isHit(screenX, SCREEN_HEIGHT - screenY) && gameSession.state == PLAYING) isTouchedShoot = false;
        return true;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        screenX = Math.round((float) screenX * (float) SCREEN_WIDTH / (float) Gdx.graphics.getWidth());
        screenY = Math.round((float) screenY * (float) SCREEN_HEIGHT / (float) Gdx.graphics.getHeight());
        if (screenX <= SCREEN_WIDTH / 2 && gameSession.state == PLAYING) joystick.onTouch(screenX, SCREEN_HEIGHT - screenY);
        else if (backgroundFireButton.isHit(screenX, SCREEN_HEIGHT - screenY) && gameSession.state == PLAYING)
            isTouchedShoot = true;
        else if (gameSession.state == PLAYING)
            joystick.toDefault();
        return true;
    }
}

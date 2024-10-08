package com.kasarlooper.spaceranger.screens;

import static com.kasarlooper.spaceranger.GameResources.ASTEROID_IMG_PATH;
import static com.kasarlooper.spaceranger.GameResources.CORE_IMG_PATH;
import static com.kasarlooper.spaceranger.GameResources.ENEMY_SHIP_IMG_PATH;
import static com.kasarlooper.spaceranger.GameSettings.ASTEROID_WIDTH_MAX;
import static com.kasarlooper.spaceranger.GameSettings.ASTEROID_WIDTH_MIN;
import static com.kasarlooper.spaceranger.GameSettings.BULLET_HEIGHT;
import static com.kasarlooper.spaceranger.GameSettings.Bullet_Speed;
import static com.kasarlooper.spaceranger.GameSettings.CHANCE_ASTEROID_SPAWN;
import static com.kasarlooper.spaceranger.GameSettings.CHANCE_CORE_SPAWN;
import static com.kasarlooper.spaceranger.GameSettings.CORE_HEIGHT;
import static com.kasarlooper.spaceranger.GameSettings.CORE_WIDTH;
import static com.kasarlooper.spaceranger.GameSettings.ENEMY_HEIGHT;
import static com.kasarlooper.spaceranger.GameSettings.ENEMY_WIDTH;
import static com.kasarlooper.spaceranger.GameSettings.SCREEN_HEIGHT;
import static com.kasarlooper.spaceranger.GameSettings.SCREEN_WIDTH;
import static com.kasarlooper.spaceranger.State.ENDED;
import static com.kasarlooper.spaceranger.State.PAUSED;
import static com.kasarlooper.spaceranger.State.PLAYING;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.toRadians;

import com.badlogic.gdx.Gdx;
import com.kasarlooper.spaceranger.EntitySpawner;
import com.kasarlooper.spaceranger.GameResources;
import com.kasarlooper.spaceranger.GameSettings;
import com.kasarlooper.spaceranger.GraphicsSettings;
import com.kasarlooper.spaceranger.MyGdxGame;
import com.kasarlooper.spaceranger.components.ButtonView;
import com.kasarlooper.spaceranger.components.ImageView;
import com.kasarlooper.spaceranger.components.JoystickView;
import com.kasarlooper.spaceranger.components.LiveView;
import com.kasarlooper.spaceranger.components.MovingBackgroundView;
import com.kasarlooper.spaceranger.components.TextView;
import com.kasarlooper.spaceranger.manager.ContactManager;
import com.kasarlooper.spaceranger.objects.AsteroidObject;
import com.kasarlooper.spaceranger.objects.BoomObject;
import com.kasarlooper.spaceranger.objects.BulletObject;
import com.kasarlooper.spaceranger.objects.CoreObject;
import com.kasarlooper.spaceranger.objects.EnemyObject;
import com.kasarlooper.spaceranger.objects.ShipObject;
import com.kasarlooper.spaceranger.session.SpaceGameSession;

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
    ArrayList<AsteroidObject> asteroidArray;
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
        session = new SpaceGameSession();
        backgroundView = new MovingBackgroundView(GameResources.BACKGROUND_IMG_PATH, GraphicsSettings.DEPTH_SPACE_BACKGROUND_SPEED_RATIO);
        contactManager = new ContactManager(myGdxGame.space);
        shipObject = new ShipObject(
                GameSettings.SCREEN_WIDTH / 2, GameSettings.SCREEN_HEIGHT / 2,
                GameSettings.SHIP_WIDTH, GameSettings.SHIP_HEIGHT,
                String.format(GameResources.SHIP_IMG_PATH, 3),
                myGdxGame.space
        );
        fireButton = new ButtonView(1113, 75, 100, 100, GameResources.FIRE_BUTTON_IMG_PATH); // "Remove-bg.ai_1720009081104.png"
        backgroundFireButton = new ImageView(1060, 25, GameResources.JOYSTICK_BACK_IMG_PATH);
        joystick = new JoystickView(25, 25);
        bulletArray = new ArrayList<>();
        coreArray = new ArrayList<>();
        enemyArray = new ArrayList<>();
        asteroidArray = new ArrayList<>();
        random = new Random();
        purpose = new TextView(myGdxGame.averageWhiteFont, 500, 675, "Цель - энергия: 0/3");
        live = new LiveView(0, 675);
        isTouchedShoot = false;
        rd = new Random();
        spawner = new EntitySpawner();
        boomArray = new ArrayList<>();
    }

    @Override
    public void show() {
        super.show();
        //enemyArray.add(new EnemyObject(100, 100, ENEMY_WIDTH, ENEMY_HEIGHT, myGdxGame.world, ENEMY_SHIP_IMG_PATH));
    }

    @Override
    public void render(float delta) {
        super.render(delta);
        if (session.state == PAUSED) {
            isTouchedShoot = false;
            joystick.toDefault();
        }
        if (shipObject.isAlive()) {
            myGdxGame.camera.position.x = shipObject.getX();
            myGdxGame.camera.position.y = shipObject.getY();
            backgroundView.move(shipObject.getX(), shipObject.getY());
            if (!shipObject.isEnd()) {
                if (session.state == PLAYING) {
                    final int padding = 30;
                    if (isTouchedShoot && shipObject.needToShoot()) {
                        BulletObject Bullet = new BulletObject(
                                (int) (shipObject.getX() + cos(toRadians(shipObject.getRotation())) * (shipObject.getRadius() / 2 + BULLET_HEIGHT + padding)),
                                (int) (shipObject.getY() + sin(toRadians(shipObject.getRotation())) * (shipObject.getRadius() / 2 + BULLET_HEIGHT + padding)),
                                GameSettings.BULLET_WIDTH, BULLET_HEIGHT,
                                GameResources.BULLET_IMG_PATH,
                                myGdxGame.space, shipObject.getRotation(), Bullet_Speed, false
                        );
                        bulletArray.add(Bullet);
                        myGdxGame.audioManager.soundBullet.play(0.2f);
                    }
                    if (session.shouldSpawn()) {
                        if (rd.nextInt(100) < CHANCE_CORE_SPAWN) generateCore();
                        else if (rd.nextInt(100) < CHANCE_ASTEROID_SPAWN) generateAsteroid();
                        else generateEnemy();
                    }
                    for (EnemyObject enemy : enemyArray) {
                        BulletObject bullet = enemy.move(shipObject.getX(), shipObject.getY());
                        if (bullet != null) bulletArray.add(bullet);
                    }
                    live.setLeftLives(shipObject.getLivesLeft());
                    myGdxGame.stepWorld(myGdxGame.space);
                    updateBullets();
                    updateCore();
                    updateEnemy();
                    updateBoom();
                    if (session.victory()) {
                        myGdxGame.passSpaceLevel();
                        shipObject.moleHoleAnim();
                    }
                    if (joystick.isTouched() && (joystick.getX() != 0 || joystick.getY() != 0)) {
                        shipObject.setRotation(joystick.getDegrees());
                        shipObject.move();
                    } else shipObject.stop();
                    for (BoomObject boomObject : boomArray) boomObject.Boom_action();
                }
            } else {
                session.state = ENDED;
            }
        }
        else {
            session.state = ENDED;
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
        for (AsteroidObject asteroid : asteroidArray) asteroid.draw(myGdxGame.batch);
        super.drawDynamic();
    }

    // "Чистилки" объектов
    private void updateBullets() {
        // For Ship
        Iterator<BulletObject> iterator = bulletArray.iterator();
        while(iterator.hasNext()) {
            BulletObject bulletObject_now = iterator.next();
            if (bulletObject_now.destroy(shipObject.getX(), shipObject.getY())) {
                myGdxGame.space.destroyBody(bulletObject_now.body);
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
                    ((SpaceGameSession) session).core_was_collected();
                    purpose.setText(String.format("Цель - энергия: %d/3", ((SpaceGameSession) session).getCoreCollected()));
                    myGdxGame.audioManager.soundEnergyGive.play(0.2f);
                } else {
                    boomArray.add(new BoomObject(core.x, core.y));
                    myGdxGame.audioManager.soundBoom.play(0.2f);
                }
                myGdxGame.space.destroyBody(core.body);
                iterator.remove();
            }
        }
    }

    private void updateEnemy() {
        Iterator<EnemyObject> iterator = enemyArray.iterator();
        while(iterator.hasNext()) {
            EnemyObject enemy = iterator.next();
            if (enemy.destroy()) {
                myGdxGame.space.destroyBody(enemy.body);
                BoomObject boom = new BoomObject(enemy.getX(), enemy.getY());
                boomArray.add(boom);
                myGdxGame.audioManager.soundBoom.play(0.2f);
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

    @Override
    public void restartGame() {
        super.restartGame();
        Iterator<CoreObject> iterator_core = coreArray.iterator();
        while (iterator_core.hasNext()) {
            myGdxGame.space.destroyBody(iterator_core.next().body);
            iterator_core.remove();
        }
        Iterator<EnemyObject> iterator_enemy = enemyArray.iterator();
        while (iterator_enemy.hasNext()) {
            myGdxGame.space.destroyBody(iterator_enemy.next().body);
            iterator_enemy.remove();
        }
        Iterator<AsteroidObject> iteratorAsteroid = asteroidArray.iterator();
        while (iteratorAsteroid.hasNext()) {
            myGdxGame.space.destroyBody(iteratorAsteroid.next().body);
            iteratorAsteroid.remove();
        }
        if (shipObject != null) myGdxGame.space.destroyBody(shipObject.body);
        shipObject = new ShipObject(
                GameSettings.SCREEN_WIDTH / 2, GameSettings.SCREEN_HEIGHT / 2,
                GameSettings.SHIP_WIDTH, GameSettings.SHIP_HEIGHT,
                String.format(GameResources.SHIP_IMG_PATH, 3),
                myGdxGame.space
        );
        purpose.setText("Цель - энергия: 0/3");
        live.setLeftLives(3);
        bulletArray.clear();
        boomArray.clear();
        session.startGame();
    }

    // Генераторы
    private void generateCore() {
        EntitySpawner.Pair pair = spawner.newPair(shipObject.getX(), shipObject.getY(), CORE_WIDTH / 2, CORE_HEIGHT / 2, shipObject.getRotation());
        CoreObject coreObject = new CoreObject(
                (int) pair.x, (int) pair.y,
                CORE_WIDTH, CORE_HEIGHT, myGdxGame.space,
                CORE_IMG_PATH
        );
        coreArray.add(coreObject);
    }

    private void generateEnemy() {
        EntitySpawner.Pair pair = spawner.newPair(shipObject.getX(), shipObject.getY(), ENEMY_WIDTH / 2, ENEMY_HEIGHT / 2, shipObject.getRotation());
        EnemyObject enemy = new EnemyObject(
                (int) pair.x, (int) pair.y,
                ENEMY_WIDTH, ENEMY_HEIGHT, myGdxGame.space,
                ENEMY_SHIP_IMG_PATH
        );
        enemyArray.add(enemy);
    }

    private void generateAsteroid() {
        int size = ASTEROID_WIDTH_MIN + rd.nextInt(ASTEROID_WIDTH_MAX - ASTEROID_WIDTH_MIN);
        EntitySpawner.Pair pair = spawner.newPair(shipObject.getX(), shipObject.getY(), size / 2, size / 2, shipObject.getRotation());
        AsteroidObject asteroid = new AsteroidObject(
                ASTEROID_IMG_PATH,
                (int) pair.x, (int) pair.y,
                size, size, myGdxGame.space,
                shipObject.getX(), shipObject.getY());
        asteroidArray.add(asteroid);
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        super.touchDown(screenX, screenY, pointer, button);
        screenX = Math.round((float) screenX * (float) SCREEN_WIDTH / (float) Gdx.graphics.getWidth());
        screenY = Math.round((float) screenY * (float) SCREEN_HEIGHT / (float) Gdx.graphics.getHeight());
        if (backgroundFireButton.isHit(screenX, SCREEN_HEIGHT - screenY)) isTouchedShoot = true;
        else if (session.state == PLAYING) {
            if (screenX <= SCREEN_WIDTH / 2) {
                if (joystick.isTouched()) joystick.onDrag(screenX, SCREEN_HEIGHT - screenY);
                else joystick.onTouch(screenX, SCREEN_HEIGHT - screenY);
            }
        }
        return true;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        super.touchUp(screenX, screenY, pointer, button);
        screenX = Math.round((float) screenX * (float) SCREEN_WIDTH / (float) Gdx.graphics.getWidth());
        screenY = Math.round((float) screenY * (float) SCREEN_HEIGHT / (float) Gdx.graphics.getHeight());
        if (backgroundFireButton.isHit(screenX, SCREEN_HEIGHT - screenY) && session.state == PLAYING) isTouchedShoot = false;
        else if (session.state == PLAYING) {
            if (screenX < SCREEN_WIDTH / 2)
                joystick.toDefault();
        }
        return true;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        screenX = Math.round((float) screenX * (float) SCREEN_WIDTH / (float) Gdx.graphics.getWidth());
        screenY = Math.round((float) screenY * (float) SCREEN_HEIGHT / (float) Gdx.graphics.getHeight());
        if (screenX <= SCREEN_WIDTH / 2 && session.state == PLAYING) joystick.onDrag(screenX, SCREEN_HEIGHT - screenY);
        else if (backgroundFireButton.isHit(screenX, SCREEN_HEIGHT - screenY) && session.state == PLAYING)
            isTouchedShoot = true;
        else {
            isTouchedShoot = false;
            joystick.toDefault();
            shipObject.stop();
        }
        return true;
    }

    @Override
    public void dispose() {
        super.dispose();
        if (shipObject != null) shipObject.dispose();
        if (bulletArray != null) {
            for (BulletObject bullet : bulletArray) {
                bullet.dispose();
            }
        }
        if (coreArray != null) {
            for (CoreObject core : coreArray) {
                core.dispose();
            }
        }
        if (boomArray != null) {
            for (BoomObject boom : boomArray) {
                boom.dispose();
            }
        }
        if (enemyArray != null) {
            for (EnemyObject enemy : enemyArray) {
                enemy.dispose();
            }
        }
        if (asteroidArray != null) {
            for (AsteroidObject asteroid : asteroidArray) {
                asteroid.dispose();
            }
        }
        if (backgroundView != null) backgroundView.dispose();
        if (fireButton != null) fireButton.dispose();
        if (backgroundFireButton != null) backgroundFireButton.dispose();
        if (purpose != null) purpose.dispose();
        if (live != null) live.dispose();
    }

    @Override
    public void win() {
        while (((SpaceGameSession)session).getCoreCollected() < 3) ((SpaceGameSession)session).core_was_collected();
    }

    public void lose() {
        shipObject.livesLeft = 0;
    }
}


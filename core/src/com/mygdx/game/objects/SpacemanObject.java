package com.mygdx.game.objects;

import static com.mygdx.game.GameSettings.SCALE;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.Shape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.TimeUtils;
import com.mygdx.game.GameResources;

public class SpacemanObject extends PhysicsObject {
    int defaultY;
    int defaultFrame;
    int speed;
    int jumpImpulse;
    Sprite sprite;
    int i;
    boolean isRightStep;
    boolean isLeftStep;
    boolean isStop;
    boolean isJump;
    Texture[] left;
    Texture[] right;
    long jumpTime;

    public SpacemanObject(int x, int y, int wight, int height, String texturePath, int defaultFrame, int speed, int jumpImpulse, World world) {
        super(String.format(texturePath, defaultFrame), x, y, wight, height, world);
        defaultY = y;
        this.defaultFrame = defaultFrame;
        this.speed = speed;
        this.jumpImpulse = jumpImpulse;
        sprite = new Sprite(texture);

        initTextures(defaultFrame);
    }

    protected void initTextures(int defaultFrame) {
        left = new Texture[14];
        right = new Texture[14];
        for (int i = 2; i <= 14; i+=2) {
            int j = i / 2 + 3;
            if (j > 7) j -= 7;
            left[i - 2] = new Texture(String.format(GameResources.COSMONAUT_ANIM_LEFT_IMG_PATTERN, j));
            left[i - 1] = new Texture(String.format(GameResources.COSMONAUT_ANIM_LEFT_IMG_PATTERN, j));
            right[i - 2] = new Texture(String.format(GameResources.COSMONAUT_ANIM_RIGHT_IMG_PATTERN, j));
            right[i - 1] = new Texture(String.format(GameResources.COSMONAUT_ANIM_RIGHT_IMG_PATTERN, j));
        }
        i = 0;
        isJump = false;
    }

    @Override
    protected Shape getShape(float x, float y, float width, float height) {
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(width * SCALE / 2f, height * SCALE / 2f);
        return shape;
    }

    @Override
    public void draw(SpriteBatch batch) {
        sprite.setBounds(getX() - (width / 2f), getY() - (height / 2f), width, height);
        sprite.draw(batch);
    }

    public void stepLeft() {
        isRightStep = false;
        isLeftStep = true;
    }

    public void stepRight() {
        isRightStep = true;
        isLeftStep = false;
    }

    public void jump() {
        if (!isJump) {
            isJump = true;
            jumpTime = TimeUtils.millis();
            body.applyLinearImpulse(new Vector2(0, jumpImpulse), body.getWorldCenter(), false);
        }
    }

    public void stop() {
        body.setLinearVelocity(0, body.getLinearVelocity().y);
        isStop = true;
    }

    public void updateFrames() {
        if (isStop) {
            if (i == 0) {
                isStop = false;
                isLeftStep = false;
                isRightStep = false;
                body.setLinearVelocity(0, body.getLinearVelocity().y);
            }
        }
        if ((isRightStep || isLeftStep) && !isJump) {
            i++;
            if (i >= right.length) i = 0;
            sprite.setTexture(isLeftStep ? left[i] : right[i]);
        }
        if (isRightStep) body.setLinearVelocity(speed, body.getLinearVelocity().y);
        else if (isLeftStep) body.setLinearVelocity(-speed, body.getLinearVelocity().y);
    }

    public void updateJump() {
        if (Math.abs(body.getLinearVelocity().y) < 0.01 && TimeUtils.millis() - jumpTime > 50) {
            body.setAwake(true);
            isJump = false;
        }
    }

    @Override
    public void hit(Type type) {
    }

    public Type type() {
        return Type.Player;
    }
}

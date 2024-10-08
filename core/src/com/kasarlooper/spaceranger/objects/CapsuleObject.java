package com.kasarlooper.spaceranger.objects;

import static com.kasarlooper.spaceranger.GameResources.CAPSULE_IMG_PATH;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class CapsuleObject extends GameObject implements Drawable {
    int width, height;
    Texture texture;

    public CapsuleObject(int x, int y, int width, int height) {
        super(x, y);
        this.width = width;
        this.height = height;
        texture = new Texture(CAPSULE_IMG_PATH);
    }

    public boolean isCollision(int x, int y) {
        return x >= this.x && x <= this.x + width && y <= this.y && y >= this.y - height;
    }

    @Override
    public void draw(SpriteBatch batch) {
        batch.draw(texture, x, y - height, width, height);
    }

    public void dispose() {
        texture.dispose();
    }
}

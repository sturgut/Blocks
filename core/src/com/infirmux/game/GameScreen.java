package com.infirmux.game;

import java.util.Iterator;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.TimeUtils;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.input.GestureDetector.GestureListener;

/**
 * Created by sulo on 11/11/2014.
 */
public class GameScreen implements Screen, GestureListener{

    final Blocks game;

    Texture dropImage;
    Texture fireImage;

    Texture bucketImage;
    Sound dropSound;
    Music rainMusic;
    OrthographicCamera camera;
    Rectangle bucket;
    Array<Rectangle> raindrops;
    Array<Rectangle> firedrops;

    long lastDropTime;
    int dropsGathered;
    private String tapMsg = "No Tap performed yet";
    private String flingMsg = "No Fling performed yet";

    private float touchX;
    private float touchY;

    private float lastVelocityX = 0;
    private float lastVelocityY = 0;


    public GameScreen(final Blocks gam) {
        this.game = gam;

        GestureDetector gd = new GestureDetector(this);
        Gdx.input.setInputProcessor(gd);

// load the images for the droplet and the bucket, 64x64 pixels each
        dropImage = new Texture(Gdx.files.internal("droplet.png"));
        fireImage = new Texture(Gdx.files.internal("firelet.png"));

        bucketImage = new Texture(Gdx.files.internal("bucket.png"));

// load the drop sound effect and the rain background "music"
        dropSound = Gdx.audio.newSound(Gdx.files.internal("Ping.wav"));
        //rainMusic = Gdx.audio.newMusic(Gdx.files.internal("rain.mp3"));
        //rainMusic.setLooping(true);

// create the camera and the SpriteBatch
        camera = new OrthographicCamera();
        camera.setToOrtho(false, 1920, 1080);

// create a Rectangle to logically represent the bucket
        bucket = new Rectangle();
        bucket.x = 800 / 2 - 64 / 2; // center the bucket horizontally
        bucket.y = 20; // bottom left corner of the bucket is 20 pixels above
// the bottom screen edge
        bucket.width = 64;
        bucket.height = 64;

// create the raindrops array and spawn the first raindrop
        raindrops = new Array<Rectangle>();
        firedrops = new Array<Rectangle>();

        //spawnRaindrop();
        spawnFireDrop();
    }

    private void spawnRaindrop() {
       /* Rectangle raindrop = new Rectangle();
        raindrop.x = MathUtils.random(0, 800 - 64);
        raindrop.y = 64;
        raindrop.width = 64;
        raindrop.height = 64;
        raindrops.add(raindrop);
        lastDropTime = TimeUtils.nanoTime();
        */
    }
    private void spawnFireDrop() {
        Rectangle firedrop = new Rectangle();
        firedrop.x = MathUtils.random(0, 1920 - 64);
        firedrop.y = 1080-64;
        firedrop.width = 64;
        firedrop.height = 64;
        firedrops.add(firedrop);
        lastDropTime = TimeUtils.nanoTime();

    }

    private void spawnNewRaindrop() {
        Rectangle raindrop = new Rectangle();
        raindrop.x = touchX - 64; // MathUtils.random(0, 800 - 64);
        raindrop.y = 64 + 64;
        raindrop.width = 64;
        raindrop.height = 64;
        raindrops.add(raindrop);
        lastDropTime = TimeUtils.nanoTime();
    }

    private void spawnNewFiredrop() {
        Rectangle firedrop = new Rectangle();
        firedrop.x = touchX - 64; // MathUtils.random(0, 800 - 64);
        firedrop.y = 1080 - 64;
        firedrop.width = 64;
        firedrop.height = 64;
        firedrops.add(firedrop);
        lastDropTime = TimeUtils.nanoTime();
    }


    @Override
    public void render(float delta) {
        // clear the screen with a dark blue color. The
// arguments to glClearColor are the red, green
// blue and alpha component in the range [0,1]
// of the color to be used to clear the screen.
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

// tell the camera to update its matrices.
        camera.update();

// tell the SpriteBatch to render in the
// coordinate system specified by the camera.
        game.batch.setProjectionMatrix(camera.combined);

// begin a new batch and draw the bucket and
// all drops
        game.batch.begin();
        game.font.draw(game.batch, "Drops Collected: " + dropsGathered, 0, 480);
        game.font.draw(game.batch, "Tap: " + tapMsg, 0, 450);
        game.font.draw(game.batch, "Fling : " + flingMsg, 0, 420);

        game.batch.draw(bucketImage, bucket.x, bucket.y);
        for (Rectangle raindrop : raindrops) {
            game.batch.draw(dropImage, raindrop.x, raindrop.y);
        }
        for (Rectangle firedrop : firedrops) {
            game.batch.draw(fireImage, firedrop.x, firedrop.y);
        }
        game.batch.end();

// process user input
        if (Gdx.input.isTouched()) {
            Vector3 touchPos = new Vector3();
            touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
            camera.unproject(touchPos);
            bucket.x = touchPos.x - 64 / 2;
        }


// make sure the bucket stays within the screen bounds
        if (bucket.x < 0)
            bucket.x = 0;
        if (bucket.x > 1920 - 64)
            bucket.x = 1920 - 64;

// check if we need to create a new raindrop
        if (TimeUtils.nanoTime() - lastDropTime > 1000000000) {
            //spawnRaindrop();
            spawnFireDrop();

        }
// move the raindrops, remove any that are beneath the bottom edge of
// the screen or that hit the bucket. In the later case we play back
// a sound effect as well.
        Iterator<Rectangle> iter = raindrops.iterator();
        while (iter.hasNext()) {
            Rectangle raindrop = iter.next();
            raindrop.y += 200 * Gdx.graphics.getDeltaTime();
            if (raindrop.y + 64 > 1080)
                iter.remove();
            if (raindrop.overlaps(bucket)) {
                dropsGathered++;
                dropSound.play();
                iter.remove();
            }
        }

        Iterator<Rectangle> fiter = firedrops.iterator();
        while (fiter.hasNext()) {
            Rectangle firedrop = fiter.next();
            firedrop.y -= 200 * Gdx.graphics.getDeltaTime();
            if (firedrop.y + 64 < 64)
                fiter.remove();
            if (firedrop.overlaps(bucket)) {
                dropsGathered++;
                dropSound.play();
                fiter.remove();
            }
        }

    }

    @Override
    public void resize(int width, int height) {

    }

    @Override
    public void show() {
        // start the playback of the background music
// when the screen is shown
        //rainMusic.play();
    }

    @Override
    public void hide() {

    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void dispose() {
        dropImage.dispose();
        bucketImage.dispose();
        dropSound.dispose();
        rainMusic.dispose();
    }

    @Override
    public boolean touchDown(float x, float y, int pointer, int button) {
        tapMsg = "Touchdown performed, " + Float.toString(x) +
                "," + Float.toString(y);
        touchX = x;
        touchY = y;
        return true;
    }

    @Override
    public boolean tap(float x, float y, int count, int button) {
        //tapMsg = "Tap performed, finger" + Integer.toString(button);
        return true;
    }

    @Override
    public boolean longPress(float x, float y) {
        return false;
    }

    @Override
    public boolean fling(float velocityX, float velocityY, int button) {
        flingMsg =  "Fling performed, velocity:" + Float.toString(velocityX) +
                "," + Float.toString(velocityY);

        Gdx.app.log("Blocks", "Fling performed, velocity: (" + Float.toString(Math.abs(lastVelocityX)) + "," + Float.toString(Math.abs(velocityX)) +
                ") (" + Float.toString(Math.abs(velocityY)) + "," + Float.toString(Math.abs(velocityY) ));


            Gdx.app.log("Blocks", "Math.abs(lastVelocityX) > 500  && Math.abs(lastVelocityY) < 500");

            if (velocityY < 0) {
                spawnNewRaindrop();
            }else{
                spawnNewFiredrop();
            }
            lastVelocityX = velocityX;
            lastVelocityY = velocityY;



        return true;
    }

    @Override
    public boolean pan(float x, float y, float deltaX, float deltaY) {
        return false;
    }

    @Override
    public boolean panStop(float x, float y, int pointer, int button) {
        return false;
    }

    @Override
    public boolean zoom(float initialDistance, float distance) {
        return false;
    }

    @Override
    public boolean pinch(Vector2 initialPointer1, Vector2 initialPointer2, Vector2 pointer1, Vector2 pointer2) {
        return false;
    }
}

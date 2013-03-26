package com.rosalila.patchouli_lwp;

import org.andengine.engine.LimitedFPSEngine;
import org.andengine.engine.camera.Camera;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;

import org.andengine.entity.particle.BatchedSpriteParticleSystem;
import org.andengine.entity.particle.emitter.PointParticleEmitter;
import org.andengine.entity.particle.initializer.AccelerationParticleInitializer;
import org.andengine.entity.particle.initializer.BlendFunctionParticleInitializer;
import org.andengine.entity.particle.initializer.RotationParticleInitializer;
import org.andengine.entity.particle.initializer.VelocityParticleInitializer;
import org.andengine.entity.particle.initializer.GravityParticleInitializer;
import org.andengine.entity.particle.modifier.AlphaParticleModifier;
import org.andengine.entity.particle.modifier.ExpireParticleInitializer;
import org.andengine.entity.particle.modifier.RotationParticleModifier;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.background.Background;
import org.andengine.entity.sprite.AnimatedSprite;
import org.andengine.entity.util.FPSLogger;
import org.andengine.entity.sprite.UncoloredSprite;
import org.andengine.extension.ui.livewallpaper.BaseLiveWallpaperService;
import org.andengine.input.sensor.acceleration.AccelerationData;
import org.andengine.input.sensor.acceleration.IAccelerationListener;

import org.andengine.opengl.texture.TextureOptions;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.andengine.opengl.texture.atlas.bitmap.BuildableBitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.source.IBitmapTextureAtlasSource;
import org.andengine.opengl.texture.atlas.buildable.builder.BlackPawnTextureAtlasBuilder;
import org.andengine.opengl.texture.atlas.buildable.builder.ITextureAtlasBuilder.TextureAtlasBuilderException;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.texture.region.TiledTextureRegion;

import org.andengine.util.debug.Debug;
import android.opengl.GLES20;
import android.util.DisplayMetrics;
import android.view.WindowManager;

public class Patchouli extends BaseLiveWallpaperService implements IAccelerationListener{

//================================================================================
//                                  Fields
//================================================================================
	private static final int MAX_FRAMES_PER_SECOND = 100;
	
	private static int CAMERA_WIDTH = 512;
	private static int CAMERA_HEIGHT = 720;
	
    private Camera mCamera;
    private Scene mScene;
	private VelocityParticleInitializer<UncoloredSprite> mVelocityParticleInitializer;

	private BuildableBitmapTextureAtlas mPatchouliTextureAtlas;
	private TiledTextureRegion patchouliTextureRegion;
	
	@Override
	public EngineOptions onCreateEngineOptions() {
		
		final DisplayMetrics displayMetrics = new DisplayMetrics();
		WindowManager wm = (WindowManager)getSystemService(WINDOW_SERVICE);
		wm.getDefaultDisplay().getMetrics(displayMetrics);
		wm.getDefaultDisplay().getRotation();
		CAMERA_WIDTH = displayMetrics.widthPixels;
		CAMERA_HEIGHT = displayMetrics.heightPixels;
		this.mCamera = new Camera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);
		
		return new EngineOptions(true, ScreenOrientation.PORTRAIT_FIXED, new RatioResolutionPolicy(CAMERA_WIDTH, CAMERA_HEIGHT), this.mCamera);
	}

	@Override
	public void onCreateResources(OnCreateResourcesCallback  createResourcesCallback) throws Exception {
		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");
        //Total size of tiled image
		this.mPatchouliTextureAtlas = new BuildableBitmapTextureAtlas(this.getTextureManager(), 1024, 256, TextureOptions.NEAREST);
		this.patchouliTextureRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(this.mPatchouliTextureAtlas, this, "patchouli_tiled_3.png", 4, 1);
		this.getEngine().getTextureManager().loadTexture(this.mPatchouliTextureAtlas);
		
		
		try {
			this.mPatchouliTextureAtlas.build(new BlackPawnTextureAtlasBuilder<IBitmapTextureAtlasSource, BitmapTextureAtlas>(0, 0, 0));
			this.mPatchouliTextureAtlas.load();
		} catch (TextureAtlasBuilderException e) {
			Debug.e(e);
		}
		createResourcesCallback.onCreateResourcesFinished();
	}
	
	@Override
	public void onCreateScene(OnCreateSceneCallback createSceneCallback) throws Exception{
		this.mEngine.registerUpdateHandler(new FPSLogger());
		mScene= new Scene();
		
		this.mScene.setBackground(new Background(0.2492f, 0.2112f, 0.2252f));
		
		/* Calculate the coordinates for the face, so its centered on the camera. */
		final float centerX = (CAMERA_WIDTH - this.patchouliTextureRegion.getWidth()) / 2;
		final float centerY = (CAMERA_HEIGHT - this.patchouliTextureRegion.getHeight()) / 2;
		
		/* Animated Patchouli */
		final AnimatedSprite patchouli = new AnimatedSprite(centerX, centerY, this.patchouliTextureRegion, this.getVertexBufferObjectManager());
		patchouli.animate(150);
		this.mScene.attachChild(patchouli);
		createSceneCallback.onCreateSceneFinished(mScene);
	}
	
	@Override
	public org.andengine.engine.Engine onCreateEngine(final
		EngineOptions pEngineOptions)
	{
		return new LimitedFPSEngine(pEngineOptions, MAX_FRAMES_PER_SECOND);
		
	}
		
	
	@Override
	public void onPopulateScene(Scene arg0, OnPopulateSceneCallback populateSceneCallback)
			throws Exception {
		populateSceneCallback.onPopulateSceneFinished();
	}

	@Override
	public void onAccelerationAccuracyChanged(AccelerationData pAccelerationData) {
		// TODO Auto-generated method stub
		
	}
// Change the petals to move along the axes of the accelerometer
	@Override
	public void onAccelerationChanged(AccelerationData pAccelerationData) {
		final float minVelocityX = (pAccelerationData.getX() + 2) * 2;
		final float maxVelocityX = (pAccelerationData.getX() - 2) * 2;	
		final float minVelocityY = (pAccelerationData.getY() - 4) * 5;
		final float maxVelocityY = (pAccelerationData.getY() - 6) * 5;
		this.mVelocityParticleInitializer.setVelocity(minVelocityX, maxVelocityX, minVelocityY, maxVelocityY);	
	}
	}



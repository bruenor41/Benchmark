
package magiclib.graphics;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.view.KeyEvent;
import android.view.MotionEvent;

import java.nio.Buffer;
import java.nio.ByteBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import magiclib.Global;
import magiclib.core.EmuConfig;
import magiclib.core.EmuManager;
import magiclib.core.Screen;
import magiclib.graphics.opengl.GLTexture;
import magiclib.graphics.opengl.ShaderProgramType;
import magiclib.graphics.opengl.ShaderPrograms;
import magiclib.logging.Log;

public class EmuVideo extends GLSurfaceView {
	public static EmuVideo surface;

    public static int sceneShiftX;
	public static int sceneShiftY;
	public static boolean recalculateSceneShift;
	public static Rect viewPort;
	public static int scaleFactor;
	public static VideoScaleMode videoScale;
	public static int scaleWidth;
	public static int scaleHeight;
	public static double percVideoSceneShiftX;
	public static double percVideoSceneShiftY;

	public VideoRenderer renderer;
		
	public EmuVideo(Context context)
	{
		super(context);
								
		this.setFocusableInTouchMode(true);
		this.setFocusable(true);		
		//this.setPreserveEGLContextOnPause(true);
		this.setEGLContextClientVersion(2);
		this.renderer = new VideoRenderer(getContext());
		//this is for emulator, comment this in real build
		//this.setEGLConfigChooser(8 , 8, 8, 8, 16, 0);
		this.setRenderer(renderer);
		this.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);

		sceneShiftX = 0;
		sceneShiftY = 0;
		recalculateSceneShift = true;
		viewPort = new Rect();
    }

	@Override
	public void onPause() {
		super.onPause();

		renderer.dispose();
	}

	public class VideoRenderer implements Renderer
	{
		final static int DEFAULT_WIDTH = 640;//800;
		final static int DEFAULT_HEIGHT = 400;//600;
	
		Context context;

		Rect srcRect = new Rect();
		Rect dstRect = new Rect();

		boolean isDirty = false;		
		
		public int mSrc_width = 0;
		public int mSrc_height = 0;
		public int mStartLine = 0;
		public int mEndLine = 0;
		public Boolean mDirty = false;
		public Buffer videoBuffer = null;
		public boolean resize = false;

		int tmp = 0;
		int dst_width = 0;
		int dst_height = 0;
		int tmpX = 0;
		int tmpY = 0;
		boolean changedFilter = false;
		boolean release = false;
		boolean updateViewPort = true;
		float sceneMagnify;
		boolean sceneZoomEnabled;
		
		// Our matrices
		private final float[] mtrxProjection = new float[16];
		private final float[] mtrxView = new float[16];
		private final float[] mtrxProjectionAndView = new float[16];		

		GLTexture texture = new GLTexture();
		
		public VideoRenderer(Context context)
		{
			this.context = context;
			this.videoBuffer = ByteBuffer.allocateDirect(DEFAULT_WIDTH * DEFAULT_HEIGHT * 2);
			sceneMagnify = 1;
			sceneZoomEnabled = false;
		}

		@Override
		public void onSurfaceCreated(GL10 gl, EGLConfig config)
		{
			GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1);
			GLES20.glEnable(GLES20.GL_BLEND);
			GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA);

			ShaderPrograms.load(ShaderProgramType.normal);
			
			//scene filter
			this.texture.program = ShaderPrograms.load(EmuConfig.graphic_filter);
            this.texture.smooth = EmuConfig.graphic_filter == ShaderProgramType.linear;

			this.texture.resize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
		    
		    if (Log.DEBUG)
		    {
		    	Global.glesVersion = GLES20.glGetString(GLES20.GL_VERSION);
		    	Global.glslVersion = GLES20.glGetString(GLES20.GL_SHADING_LANGUAGE_VERSION);
		    }
		}		
		
		@Override
		public void onSurfaceChanged(GL10 gl, int width, int height)
		{
			this.texture.windowSize[0] = width;
			this.texture.windowSize[1] = height;

			viewPort.set(0, 0, width, height);
			GLES20.glViewport(0, 0, width, height);

		    for(int i=0;i<16;i++)
		    {
		    	mtrxProjection[i] = 0.0f;
		    	mtrxView[i] = 0.0f;
		    	mtrxProjectionAndView[i] = 0.0f;
		    }

		    Matrix.orthoM(mtrxProjection, 0,
					0, width,
					height, 0,
					-1, 1);
			Matrix.setLookAtM(mtrxView, 0,
					0f, 0f, 1f,
					0f, 0f, 0f,
					0f, 1.0f, 0.0f);
			Matrix.multiplyMM(mtrxProjectionAndView, 0,
					mtrxProjection, 0,
					mtrxView, 0);
	        
	        for (int i=0; i < ShaderPrograms.count; i++)
	        {
	        	ShaderPrograms.update(i, mtrxProjectionAndView);
	        }

			if (Screen.isLandscape()) {
				scaleFactor = EmuConfig.scaleFactor;
				videoScale = EmuConfig.videoScale;
				scaleWidth = EmuConfig.scaleWidth;
				scaleHeight = EmuConfig.scaleHeight;
				percVideoSceneShiftX = EmuConfig.percVideoSceneShiftX;
				percVideoSceneShiftY = EmuConfig.percVideoSceneShiftY;
			} else {
				scaleFactor = EmuConfig.scaleFactorP;
				videoScale = EmuConfig.videoScaleP;
				scaleWidth = EmuConfig.scaleWidthP;
				scaleHeight = EmuConfig.scaleHeightP;
				percVideoSceneShiftX = EmuConfig.percVideoScenePShiftX;
				percVideoSceneShiftY = EmuConfig.percVideoScenePShiftY;
			}

			recalculateSceneShift = true;

			this.resetZoom();
			//required by orientation change
		    forceRedraw(true);
		}

		@Override
		public void onDrawFrame(GL10 gl) {
			//Log.log("draw");
			//long start = System.nanoTime();			

			GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

			if (resize) {
				texture.resize(mSrc_width, mSrc_height);
				resize = false;
				updateViewPort = true;
				recalculateSceneShift = true;
			}

			VideoRedraw(mSrc_width, mSrc_height, mStartLine, mEndLine);

			//long time = System.nanoTime() - start;
			//if (Log.DEBUG) Log.log("onDrawFrame : " + (time / 1000000));
		}

		private void VideoRedraw(int src_width, int src_height, int startLine, int endLine)
		{
			if ((src_width <= 0) || (src_height <= 0))
				return;
									
			try
			{
				if (updateViewPort) {
					updateViewPort = false;
					calcScreenDimensions(src_width, src_height);
					texture.set(dstRect.left, dstRect.top, dstRect.width(), dstRect.height());
				}

				synchronized (mDirty) {
					if (mDirty && videoBuffer != null) {
						videoBuffer.position(0);
						//Log.log("src_width[" + src_width + "] src_height[" + src_height + "]");
						//Log.log("draws src_width * src_height * 2=[" + (src_width * src_height * 2) + "]videoBuffer.remaining()=[" + videoBuffer.remaining() + "]");
						if (src_width * src_height * 2 == videoBuffer.remaining()) {
							//texture.loadGLTexture(gl, src_width, src_height, videoBuffer);

							if (endLine > src_height) {
								endLine = src_height;
							}

							texture.loadGLTexture(src_width, src_height, startLine, endLine, videoBuffer);
						}
					}
					mDirty = false;
				}

				//FPSTest();
				if (changedFilter) {
					changeOpenGLFilter();
				}

				texture.draw();
			}
			catch(Exception exc)
			{				
				//if (Log.DEBUG) Log.log(exc.getMessage());	
			}
		}

		/*private long startTime = 0;
		private long lastTime = 0;
		private long nowTime = 0;
		private int counter = 0;
		private String counterResult = "";

		private void FPSTest() {
			nowTime = System.currentTimeMillis();
			if (startTime == 0) {
				lastTime = startTime = nowTime;
			}

			counter++;

			if (nowTime - lastTime >= 1000) {
				counterResult += "," + counter;
				lastTime = nowTime;
				counter = 0;
			}

			if (nowTime - startTime >= 10000) {
				//Log.log("FPS over 10 seconds : " + counter);
				Message msg = uiHandler.getHandler().obtainMessage(uiHandler.TEST);
				msg.obj = "" + counterResult;
				uiHandler.getHandler().sendMessageDelayed(msg, 0);

				startTime = nowTime;
				counterResult = "";
			}
		}*/

		private void calcScreenDimensions(int src_width, int src_height) {
			if (sceneZoomEnabled && zoomToTexturePoint) {
				prevDst_width = dst_width;
				prevDst_height = dst_height;
			}

			dst_width = getWidth();
			dst_height = getHeight();
			isDirty = false;
			tmp = src_width * dst_height / src_height;

			switch (videoScale) {
				case fullscreen: {
					break;
				}
				case custom:
				case fit_screen:
				default: {
					if (tmp < dst_width) {
						dst_width = tmp;
					} else if (tmp > dst_width) {
						dst_height = src_height * dst_width / src_width;
					}

					if (videoScale == VideoScaleMode.custom) {
						dst_width = dst_width * scaleWidth / 100;
						dst_height = dst_height * scaleHeight / 100;
					}
					break;
				}
			}

			dst_width *= scaleFactor * 0.01f;
			dst_height *= scaleFactor * 0.01f;

			tmpX = ((getWidth() - dst_width) >> 1);
			tmpY = 0;

			if (recalculateSceneShift) {
				sceneShiftX = (int) (((double) getWidth() / 100) * percVideoSceneShiftX);
				sceneShiftY = (int) (((double) getHeight() / 100) * percVideoSceneShiftY);

				if (videoScale != VideoScaleMode.custom)
					correctScenePosition(tmpX, tmpY);

				recalculateSceneShift = false;
			}

			tmpX += sceneShiftX;
			tmpY += sceneShiftY;

			if (sceneZoomEnabled) {
				dst_width = (int) (dst_width * sceneMagnify);
				dst_height = (int) (dst_height * sceneMagnify);

				if (zoomToTexturePoint) {
					int shiftX = (int) ((double) magnifyToTexPercX / 100 * Math.abs(dst_width - prevDst_width));
					int shiftY = (int) ((double) magnifyToTexPercY / 100 * Math.abs(dst_height - prevDst_height));

					if (dst_width > prevDst_width) {
						shiftX *= -1;
					}

					if (dst_height > prevDst_height) {
						shiftY *= -1;
					}

					//Log.log("sceneZoom perc[" + magnifyToTexPercX + "," + magnifyToTexPercY + "] diff[" + diffX + "," + diffY + "] prevSize[" + prevDst_width + "," + prevDst_height + "] dstSize[" + dst_width + "," + dst_height + "] shiftXY[" + shiftX + "," + shiftY + "]");

					sceneShiftX += shiftX;
					sceneShiftY += shiftY;

					tmpX += shiftX;
					tmpY += shiftY;
				}
			}

			srcRect.set(0, 0, src_width, src_height);
			dstRect.set(0, 0, dst_width, dst_height);
			dstRect.offset(tmpX, tmpY);
		}

		private int prevDst_width, prevDst_height, magnifyToTexPercX, magnifyToTexPercY;
		private boolean zoomToTexturePoint;

		private void setZoomPoint(int x, int y) {
			zoomToTexturePoint = (x >dstRect.left && x <dstRect.left + dstRect.width() && y >dstRect.top && y < dstRect.top + dstRect.height());
			if (zoomToTexturePoint) {
				magnifyToTexPercX = (int)(100 / (double)dstRect.width() * (x - dstRect.left));
				magnifyToTexPercY = (int)(100 / (double)dstRect.height() * (y - dstRect.top));
			}
		}

		private void clearZoomPoint() {
			zoomToTexturePoint = false;
		}

		private void zoom(float magnify) {
			sceneMagnify = magnify;
		}

		private void setZoomOn(boolean enabled) {
			sceneZoomEnabled = enabled;
		}

		private float getSceneZoomValue() {
			return sceneMagnify;
		}

		private void resetZoom() {
			sceneZoomEnabled = false;
			zoomToTexturePoint = false;
			recalculateSceneShift = true;
			sceneMagnify = 1;
		}

		private void changeOpenGLFilter() {
			if (this.texture.program.type != EmuConfig.graphic_filter)
			{
				if (this.texture.program.type != ShaderProgramType.normal &&
						this.texture.program.type != ShaderProgramType.linear)
				{
					ShaderPrograms.release(this.texture.program.type);
				}

				this.texture.program = ShaderPrograms.load(EmuConfig.graphic_filter);
				this.texture.smooth = (this.texture.program.type == ShaderProgramType.linear);

				ShaderPrograms.update(EmuConfig.graphic_filter, mtrxProjectionAndView);
			}

			changedFilter = false;
		}

		private void dispose() {
			try {
				//delete main texture program
				ShaderPrograms.release();

				//delete main texture
				if (texture != null) {
					texture.dispose();
				}
			} catch (Exception exc) {

			}
		}

		private void correctScenePosition(int x, int y)
		{
			if ((sceneShiftX + x + dst_width) > getWidth())
			{
				sceneShiftX = getWidth() - (x + dst_width);						
			}
			
			if ((sceneShiftX + x) < 0)
			{
				sceneShiftX = -x;
			}					
			
			if ((sceneShiftY + y + dst_height) > getHeight())
			{
				sceneShiftY = getHeight() - (y + dst_height);						
			}
			
			if ((sceneShiftY + y) < 0)
			{
				sceneShiftY = -y;
			}			
		}
		
		public Rect getScreenRect()
		{
			return dstRect;
		}
		
		public void forceRedraw(boolean updateViewPort)
		{
			this.updateViewPort = updateViewPort;
			requestRender();
		}
		
		public void resetScreen() {
			//empty for now
		}
	}

	public void release()
	{
		if (Log.DEBUG)  Log.log("video release");

		//setOnKeyListener(null);
		//setOnGenericMotionListener(null);

		if (renderer == null)
			return;

		try {
			synchronized (renderer.mDirty)
			{
				renderer.mDirty = true;
				renderer.release = true;
				requestRender();

				int counter = 0;

				while (renderer.mDirty && counter < 200)
				{
					Thread.sleep(10);
					counter++;
				}
			}
		} catch (Exception e) {

		}

		if (Log.DEBUG)  Log.log("/video release");
	}

	public void changeFilter()
	{
		renderer.changedFilter = true;
	}
	
	public void forceRedraw()
	{
		renderer.forceRedraw(false);
	}

	public void forceRedraw(boolean updateDestScreen)
	{
		renderer.forceRedraw(updateDestScreen);
	}

	public Bitmap geBitmap()
	{
		Bitmap bmp = null;
	
		try
		{
			synchronized (renderer.mDirty)
			{
				bmp = Bitmap.createBitmap(renderer.mSrc_width, renderer.mSrc_height, Bitmap.Config.RGB_565);
				renderer.videoBuffer.position(0);
				bmp.copyPixelsFromBuffer(renderer.videoBuffer);
			}
		}
		catch(Exception exc){			
		}
		
		return bmp;
	}

	public static Rect getScreenRect() {
		return surface.renderer.getScreenRect();
	}

	public static void setVideoRenderMode(int renderMode) {
		surface.setRenderMode(renderMode);
	}

	public static void redraw(boolean updateViewPort)
	{
		surface.forceRedraw(updateViewPort);
	}

	public static void redraw()
	{
		surface.forceRedraw();
	}

	public static void setVideoBackgroundResource(int resource) {
		surface.setBackgroundResource(0);
	}

	public static void setZoomPoint(int x, int y) {
		surface.renderer.setZoomPoint(x, y);
	}

	public static void zoom(float magnify) {
		surface.renderer.zoom(magnify);
	}

	public static void setZoomOn() {
		surface.renderer.setZoomOn(true);
	}

	public static void setZoomOff() {
		surface.renderer.setZoomOn(false);
	}

	public static void clearZoomPoint() {
		surface.renderer.clearZoomPoint();
	}

	public static float getSceneZoomValue() {
		return surface.renderer.getSceneZoomValue();
	}

	public static void resetZoom() {
		surface.renderer.resetZoom();
	}

	public static void changeVideoFilter()
	{
		surface.changeFilter();
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) 
	{
		return EmuManager.onTouch(event);
	}
	
	@Override
	public boolean onHoverEvent (MotionEvent event)
	{
		return EmuManager.onTouch(event);
	}
		
	@Override
	public boolean onKeyDown(int keyCode, final KeyEvent event) 
	{
		if (event.getRepeatCount() > 0) {
			return true;
		}
		return EmuManager.onHandleKey(keyCode, event);
	}
	
	@Override
	public boolean onKeyUp(int keyCode, final KeyEvent event) 
	{
		if (event.getRepeatCount() > 0) {
			return true;
		}
		return EmuManager.onHandleKey(keyCode, event);
	}
}


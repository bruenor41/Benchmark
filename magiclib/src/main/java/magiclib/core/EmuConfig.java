package magiclib.core;

import magiclib.graphics.VideoScaleMode;
import magiclib.graphics.opengl.ShaderProgramType;
import magiclib.mouse.MouseButton;
import magiclib.mouse.MouseType;

public class EmuConfig
{
	   public static MouseType mouse_type = MouseType.absolute;
		
	   public static MouseButton mouse_button = MouseButton.left;

	   public static MouseButton spen_primary_button = MouseButton.left;

	   public static boolean mouse_max_enabled = true;
	   
	   public static int mouse_max_width = 1279;
		
	   public static int mouse_max_height = 399;
	   	   		
	   //screen scale in landscape orientation
	   public static int scaleFactor = 100;

	   //screen scale in portrait orientation
	   public static int scaleFactorP = 100;

	   public static int scaleWidth = 100;

	   public static int scaleWidthP = 100;

	   public static int scaleHeight = 100;

	   public static int scaleHeightP = 100;

	   public static boolean speedPatchR = false;

	   public static boolean speedPatchC = false;

	   public static boolean roundAbsoluteByVideoMode = false;
	   
	   public static VideoScaleMode videoScale = VideoScaleMode.fit_screen;

	   public static VideoScaleMode videoScaleP = VideoScaleMode.fit_screen;

	   public static double percVideoSceneShiftX = 0;

	   public static double percVideoScenePShiftX = 0;
	   
	   public static double percVideoSceneShiftY = 0;

	   public static double percVideoScenePShiftY = 0;

	   public static boolean dimNavigationBar = false;

	   public static int graphic_filter = ShaderProgramType.normal;

	   public static int mouse_sensitivity = 100;

	   public EmuConfig() {
		   super();
	   }
	   
	   public void init()
	   {		   		   

	   }
}
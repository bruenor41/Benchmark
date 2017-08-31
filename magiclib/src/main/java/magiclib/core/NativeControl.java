package magiclib.core;

public class NativeControl
{	
	public static native void nativeMouse(int x, int y, int down_x, int down_y, int action, int button);
	public static native int nativeKey(int keyCode, int down, int ctrl, int alt, int shift);
	public static native void nativeDpad(int index, int x, int y, int action, int button);
	public static native void nativeJoystick(int index, int x, int y, int action, int button);
	public static native int nativeGetMouseMaxX();
	public static native int nativeGetMouseMaxY();

	//return true to clear modifier
	public static void sendNativeKey(int keyCode, boolean down, boolean ctrl, boolean alt, boolean shift)
	{
		nativeKey(keyCode, (down)?1:0, (ctrl)?1:0, (alt)?1:0, (shift)?1:0);
	}
}


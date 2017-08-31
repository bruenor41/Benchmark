package magiclib.core;

public class NativeCore
{
	public static native int nativeGetArchitecture();
	public static native int nativeHasNEON();
	public static native String nativeGetRealPath(String path);
}

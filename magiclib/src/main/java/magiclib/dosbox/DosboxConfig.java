package magiclib.dosbox;

import magiclib.Global;

public class DosboxConfig
{
	public static String generateDosboxConfig()
	{
		StringBuilder script = new StringBuilder();

		script.append("[dosbox]\n");
		script.append("machine=svga_s3\n");
		script.append("memsize=16\n");
		script.append("\n");

		script.append("[render]\n");
		script.append("frameskip=0\n");
		script.append("aspect=false\n");
		script.append("\n");

		script.append("[cpu]\n");
		script.append("core=dynamic\n");
		script.append("cputype=auto\n");
		script.append("cycles=max 105%\n");
		script.append("\n");

		script.append("[mixer]\n");
		script.append("androidFasterAudio=true\n");
		script.append("blocksize=1024\n");
		script.append("prebuffer=10\n");
		script.append("rate=22050\n");
		script.append("\n");

		script.append("[midi]\n");
		script.append("mpu401=none\n");
		script.append("\n");

		script.append("[speaker]\n");
		script.append("pcspeaker=true\n");
		script.append("pcrate=22050\n");
		script.append("tandy=off\n");
		script.append("disney=false\n");
		script.append("\n");

		script.append("[sblaster]\n");
		script.append("sbtype=sb16\n");
		script.append("sbmixer=true\n");
		script.append("oplmode=opl2\n");
		script.append("oplemu=fast\n");
		script.append("oplrate=22050\n");
		script.append("\n");

		script.append("[ipx]\n");
		script.append("ipx=false\n");
		script.append("\n");

		script.append("[serial]\n");
		script.append("serial1=disabled\n");
		script.append("serial2=disabled\n");
		script.append("serial3=disabled\n");
		script.append("serial4=disabled\n");
		script.append("\n");

		script.append("[dos]\n");
		script.append("xms=true\n");
		script.append("ems=true\n");
		script.append("umb=true\n");
		script.append("keyboardlayout=auto\n");
		script.append("\n");

		script.append("[joystick]\n");
		script.append("joysticktype=none\n");
		script.append("\n");

		script.append("[autoexec]\n");
		script.append("@Echo Off\n");
		script.append("mount c: \"" + Global.currentGameDOSROOTPath + "\"\n");
		script.append("c:\n");
		script.append("cd DOOMTEST\n");
		script.append("doom -timedemo demo3\n");
		script.append("pause\n");
		script.append("exit\n");

		return script.toString();
	}
}

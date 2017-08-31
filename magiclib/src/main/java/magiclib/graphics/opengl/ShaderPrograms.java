
package magiclib.graphics.opengl;

import android.content.res.AssetManager;
import android.opengl.GLES20;

import java.io.IOException;
import java.io.InputStream;

import magiclib.Global;

public class ShaderPrograms
{
    public static int count = 14;
    public static ShaderProgram [] sp = new ShaderProgram[count];

    private static String getNameByType(int type)
    {
        switch (type)
        {
            case ShaderProgramType.primitive: {return "primitive";}
            case ShaderProgramType.normal: {return "normal";}
            case ShaderProgramType.linear: {return "normal";}
            case ShaderProgramType.test: {return "test";}
            case ShaderProgramType.hq2x: {return "hq2x";}
            case ShaderProgramType.hq4x: {return "hq4x";}
            case ShaderProgramType.mcgreen: {return "mcgreen";}
            case ShaderProgramType._2xSaI: {return "2xSaI";}
            case ShaderProgramType.superEagle: {return "superEagle";}
            case ShaderProgramType.crt: {return "crt";}
            case ShaderProgramType.scanline: {return "scanline";}
            case ShaderProgramType._5xBR: {return "5xBR";}
            case ShaderProgramType.grayscale: {return "grayscale";}
            case ShaderProgramType.mcamber: {return "mcamber";}

            default : return null;
        }
    }

    public static ShaderProgram load(int type)
    {
        try
        {
            if (type < 0 || type > (count - 1))
                return null;

            if (sp[type] != null)
                return sp[type];

            ShaderProgram program = sp[type] = new ShaderProgram();
            program.type = type;

            String name = getNameByType(type);
            //load vertex shader
            AssetManager am = Global.context.getAssets();
            program.vertexHandle = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER);

            InputStream inputStream = am.open("Shaders/" + name + "/" + name + "_vs.glsl");

            GLES20.glShaderSource(program.vertexHandle, Global.getStringFromInputStream(inputStream));
            GLES20.glCompileShader(program.vertexHandle);
/*
			String result = GLES20.glGetShaderInfoLog(vertex_shader);
			if (result.length() > 0) {
				uiLog.log("Compilation error vs: " + result);
			}*/

            inputStream.close();

            //load fragment shader
            program.fragmentHandle = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER);

            inputStream = am.open("Shaders/" + name + "/" + name + "_fs.glsl");

            GLES20.glShaderSource(program.fragmentHandle, Global.getStringFromInputStream(inputStream));
            GLES20.glCompileShader(program.fragmentHandle);
/*
			result = GLES20.glGetShaderInfoLog(fragment_shader);
			if (result.length() > 0) {
				uiLog.log("Compilation error fs: " + result);
			}*/

            inputStream.close();

            program.id = GLES20.glCreateProgram();
            GLES20.glAttachShader(program.id, program.vertexHandle);
            GLES20.glAttachShader(program.id, program.fragmentHandle);
            GLES20.glLinkProgram(program.id);

/*			result = GLES20.glGetShaderInfoLog(program.id);
			if (result.length() > 0) {
				uiLog.log("Link error: " + result);
			}*/

            return program;
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        return null;
    }

    public static void update(int type, float[] mtrxProjectionAndView)
    {
        try
        {
            if (type < 0 || type > (count - 1))
                return;

            ShaderProgram program = sp[type];

            if (program == null)
                return;

            GLES20.glUseProgram(program.id);

            if (type == ShaderProgramType.primitive)
            {
                program.colorHandle = GLES20.glGetUniformLocation(program.id, "vColor");
                program.positionHandle = GLES20.glGetAttribLocation(program.id, "vPosition");
                program.matrixHandle = GLES20.glGetUniformLocation(program.id, "uMVPMatrix");
            }
            else
            {
                // handle to vertex shader's vPosition member
                program.positionHandle = GLES20.glGetAttribLocation(program.id, "vPosition");
                // handle to texture coordinates location
                program.texCoordLoc = GLES20.glGetAttribLocation(program.id, "a_texCoord" );
                // handle to shape's transformation matrix
                program.matrixHandle = GLES20.glGetUniformLocation(program.id, "uMVPMatrix");
                // handle to textures locations
                program.samplerLoc = GLES20.glGetUniformLocation (program.id, "s_texture" );
                program.texSize = GLES20.glGetUniformLocation(program.id, "p_texSize");
                program.winSize = GLES20.glGetUniformLocation(program.id, "p_winSize");
            }

            GLES20.glUniformMatrix4fv(program.matrixHandle, 1, false, mtrxProjectionAndView, 0);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static void release(int type)
    {
        ShaderProgram program = sp[type];
        if (program == null) {
            return;
        }

        GLES20.glDeleteProgram(program.id);
        GLES20.glDeleteShader(program.vertexHandle);
        GLES20.glDeleteShader(program.fragmentHandle);

        sp[type] = null;
    }

    public static void release() {
        for (int i=0; i<count;i++) {
            release(i);
        }
    }
}
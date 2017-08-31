
package magiclib.graphics.opengl;

import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLUtils;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;

public class GLTexture
{
    public boolean smooth = false;
    public ShaderProgram program;

    public float left = 0;
    public float top = 0;
    public float width = 0;
    public float height = 0;

    private int textureSize[] = new int[2];
    public int windowSize[] = new int[2];

    public FloatBuffer vertexBuffer;
    private float vertices[] =
            {
                    0.0f, 0.0f,  0.0f,	// V1 - bottom left
                    0.0f, 0.0f,  0.0f,	// V2 - top left
                    0.0f, 0.0f,  0.0f,	// V3 - bottom right
                    0.0f, 0.0f,  0.0f	// V4 - top right
            };

    public FloatBuffer textureBuffer;
    private float texture[] =
            {
                    0.0f, 1.0f,		// top left		(V2)
                    0.0f, 0.0f,		// bottom left	(V1)
                    1.0f, 1.0f,		// top right	(V4)
                    1.0f, 0.0f		// bottom right	(V3)
            };

    private int[] glTextureID = {-1};

    public boolean isEmpty()
    {
        return (glTextureID[0] == -1);
    }

    public GLTexture()
    {
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(vertices.length * 4);
        byteBuffer.order(ByteOrder.nativeOrder());

        // allocates the memory from the byte buffer
        vertexBuffer = byteBuffer.asFloatBuffer();

        // fill the vertexBuffer with the vertices
        vertexBuffer.put(vertices);

        // set the cursor position to the beginning of the buffer
        vertexBuffer.position(0);

        byteBuffer = ByteBuffer.allocateDirect(texture.length * 4);
        byteBuffer.order(ByteOrder.nativeOrder());
        textureBuffer = byteBuffer.asFloatBuffer();
        textureBuffer.put(texture);
        textureBuffer.position(0);

        windowSize[0] = windowSize[1] = 0;
    }

    public void dispose()
    {
        if (glTextureID[0] != -1)
        {
            GLES20.glDeleteTextures(1, glTextureID, 0);
            glTextureID[0] = -1;
        }
    }

    public void set(float left, float top)
    {
        set(left, top, this.width, this.height);
    }

    public void set(float left, float top, float width, float height)
    {
        if ((this.left == left) && (this.top == top) && (this.width == width) && (this.height == height))
        {
            return;
        }

        this.left = left;
        this.top = top;
        this.width = width;
        this.height = height;

        vertices[0] = left;
        vertices[1] = top + height;

        vertices[3] = left;
        vertices[4] = top;

        vertices[6] = left + width;
        vertices[7] = top + height;

        vertices[9] = left + width;
        vertices[10] = top;

        vertexBuffer.put(vertices);
        vertexBuffer.position(0);
    }

    public void resize(int width, int height)
    {
        if (glTextureID[0] == -1)
        {
            GLES20.glGenTextures(1, glTextureID, 0);
        }

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, glTextureID[0]);

        if (smooth)
        {
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        }
        else
        {
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);
        }

        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D,
                0,
                GLES20.GL_RGB,
                width,
                height,
                0,
                GLES20.GL_RGB,
                GLES20.GL_UNSIGNED_SHORT_5_6_5,
                null);
    }

    public void loadGLTexture(int width, int height, int startLine, int endLine, Buffer pixels)
    {
        if (glTextureID[0] == -1)
        {
            GLES20.glGenTextures(1, glTextureID, 0);
            program = ShaderPrograms.sp[ShaderProgramType.normal];
        }

        textureSize[0] = width;
        textureSize[1] = height;

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, glTextureID[0]);

        if (smooth)
        {
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        }
        else
        {
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);
        }

        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

        //Log.log("callbackVideo loadGLTexture width[" + width + ", range[" + startLine + "," + (endLine - startLine) + "]");

        pixels.position(startLine * width * 2);

        //long t1 = System.nanoTime();

        GLES20.glTexSubImage2D(GL10.GL_TEXTURE_2D,
                0,
                0,
                startLine,
                width,
                endLine - startLine,
                GL10.GL_RGB,
                GL10.GL_UNSIGNED_SHORT_5_6_5,
                pixels);

        /*int error = gl.glGetError();
        if (error != GL10.GL_NO_ERROR)
        {
            Log.log("callbackVideo error No : " + error);
        }*/

            //long t2 = System.nanoTime();
        //Log.log("loadGLTexture diff : " + (t2-t1) + "[" + startLine + "," + endLine + "]");
    }

    public void loadGLTexture(int width, int height, Buffer pixels)
    {
        if (glTextureID[0] == -1)
        {
            GLES20.glGenTextures(1, glTextureID, 0);
            program = ShaderPrograms.sp[ShaderProgramType.normal];
        }

        textureSize[0] = width;
        textureSize[1] = height;

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, glTextureID[0]);

        if (smooth)
        {
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        }
        else
        {
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);
        }

        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

        //long t1 = System.nanoTime();

        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D,
                0,
                GLES20.GL_RGB,
                width,
                height,
                0,
                GLES20.GL_RGB,
                GLES20.GL_UNSIGNED_SHORT_5_6_5,
                pixels );

        //long t2 = System.nanoTime();
        //Log.log("loadGLTexture diff : " + (t2-t1));
    }

    public void loadGLTexture(Bitmap bitmap)
    {
        if (glTextureID[0] == -1)
        {
            GLES20.glGenTextures(1, glTextureID, 0);
            program = ShaderPrograms.sp[ShaderProgramType.normal];
        }

        textureSize[0] = bitmap.getWidth();
        textureSize[1] = bitmap.getHeight();

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, glTextureID[0]);

        if (smooth)
        {
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        }
        else
        {
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);
        }

        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
    }

    public void draw()
    {
        GLES20.glUseProgram(program.id);

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, glTextureID[0]);

        // Enable generic vertex attribute array
        GLES20.glEnableVertexAttribArray(program.positionHandle);
        GLES20.glEnableVertexAttribArray(program.texCoordLoc);

        // Prepare the triangle coordinate data
        GLES20.glVertexAttribPointer(program.positionHandle, 3, GLES20.GL_FLOAT, false, 0, vertexBuffer);
        // Prepare the texturecoordinates
        GLES20.glVertexAttribPointer (program.texCoordLoc, 2, GLES20.GL_FLOAT, false, 0, textureBuffer);

        // Apply the projection and view transformation
        //GLES20.glUniformMatrix4fv(mtrxhandle, 1, false, projectionMatrix, 0);

        // Set the sampler texture unit to 0, where we have saved the texture.
        GLES20.glUniform1i (program.samplerLoc, 0);

        if (windowSize[0] > 0)
        {
            GLES20.glUniform2i(program.texSize, textureSize[0], textureSize[1]);
            GLES20.glUniform2i(program.winSize, windowSize[0], windowSize[1]);
        }

        //Draw the triangles
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(program.positionHandle);
        GLES20.glDisableVertexAttribArray(program.texCoordLoc);
    }
}
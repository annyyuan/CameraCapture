package cn.bgxt.camerapicturedemo;  
  
import java.nio.Buffer;
import java.nio.ByteBuffer;  
import java.nio.ByteOrder;  
import java.nio.FloatBuffer;  
import java.nio.ShortBuffer;  



//import com.yuv.display.Utils;

import android.annotation.SuppressLint;
import android.opengl.GLES11Ext;  
import android.opengl.GLES20;  
import android.opengl.Matrix; 
import android.util.Log;  
  
@SuppressLint("InlinedApi")
public class OpenGLDraw {  
    private final String vertexShaderCode =  
            "attribute vec4 vPosition;" +  
            "attribute vec4 inputTextureCoordinate;" +  
            "uniform mat4 uMVPMatrix;" + 
            "uniform mat4 uTexMatrix;" +
            "varying vec2 textureCoordinate;" +  
            "void main()" +  
            "{"+  
                "gl_Position = uMVPMatrix * vPosition;"+  
                "textureCoordinate = (uTexMatrix * inputTextureCoordinate).xy;" +  
            "}";  
  
    private final String fragmentShaderCode =  
           "#extension GL_OES_EGL_image_external : require\n"+  
            "precision mediump float;" +  
            "uniform sampler2D Ytex,Utex,Vtex;" +
            "varying vec2 textureCoordinate;\n" +  
            "void main() {" +  
            "  float r,g,b,y,u,v;" +
            "  y=texture2D(Ytex,textureCoordinate).r;"  +
            "  u=texture2D(Utex,textureCoordinate).r;"  +
            "  v=texture2D(Vtex,textureCoordinate).r;"  +              
            "  y=1.1643*(y-0.0625);"  +
            "  u=u-0.5;"  +
            "  v=v-0.5;"  +      
            "  r=y+1.5958*v;"  +
            "  g=y-0.39173*u-0.81290*v;"  +
            "  b=y+2.017*u;"+
            "  gl_FragColor = vec4(r,g,b,1.0);" +  
            "}";  
 
    private final int mProgram;  
  
    private final int FLOAT_SIZE = 4;
  
    static float vertices[] = {  
       -1.0f, -1.0f, 1.0f,   
        1.0f, -1.0f, 1.0f,  
       -1.0f,  1.0f, 1.0f,
        1.0f,  1.0f, 1.0f
    };  
  
    static float texCoords[] = {  
        1.0f, 1.0f,  
        0.0f, 1.0f,  
        1.0f, 0.0f,  
        0.0f, 0.0f,  
    };  
    
    static final float[] IDENTITY_MATRIX;
    
    static final String  V_POSITION = "vPosition";
    static final String  INPUT_TEXTURE_COORDINATE = "inputTextureCoordinate";
    static final String  Y_TEXTURE = "Ytex";
    static final String  U_TEXTURE = "Utex";
    static final String  V_TEXTURE = "Vtex";
    static final String  U_MVP_MATRIX = "uMVPMatrix";
    static final String  U_TEX_MATRIX = "uTexMatrix";
  
    private int[]  textures = new int[3];
    private FloatBuffer   vertexBuffer;
    private FloatBuffer   texBuffer;
    private int[] textureLocations = new int[3];
    private int   uMvpMatrixLocation;
    private int   uTexMatrixLocation;
    private int   avPosition;
    private int   ainputTextureCoordinate;
    private float[]   mvpMatrix = new float[16];
    private float[]   modelViewMatrix = new float[16];
    
    static {
    	IDENTITY_MATRIX = new float[16];
    	Matrix.setIdentityM(IDENTITY_MATRIX, 0);
    }
  
    public OpenGLDraw()  
    {  
    	Log.i("main","create opengl");
    	
    	checkGlError("clear error");
        int i;
        for(i=0;i<3;i++){
        	textures[i] = createTextureID();
        }
        checkGlError("CreateTexture");
        
        ByteBuffer buffer = ByteBuffer.allocateDirect(vertices.length * FLOAT_SIZE);
        buffer.order(ByteOrder.nativeOrder());
        vertexBuffer = buffer.asFloatBuffer();
        vertexBuffer.put(vertices);
        vertexBuffer.position(0);
        
        buffer = ByteBuffer.allocateDirect(texCoords.length * FLOAT_SIZE);
        buffer.order(ByteOrder.nativeOrder());
        texBuffer = buffer.asFloatBuffer();
        texBuffer.put(texCoords);
        texBuffer.position(0);
        
        checkGlError("allocate native buffer");
        
        int vertexShader    = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);  
        int fragmentShader  = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);  
        
        checkGlError("loadshader");
  
        mProgram = GLES20.glCreateProgram();             // create empty OpenGL ES Program  
        if (mProgram != 0) {
        	
            GLES20.glAttachShader(mProgram, fragmentShader);
            checkGlError("glAttachShader 2");
            GLES20.glAttachShader(mProgram, vertexShader); 
            checkGlError("glAttachShader");
            GLES20.glLinkProgram(mProgram);
            int[] linkStatus = new int[1];
            GLES20.glGetProgramiv(mProgram, GLES20.GL_LINK_STATUS, linkStatus, 0);
            if (linkStatus[0] != GLES20.GL_TRUE) {
                Log.e("opengl","Could not link program: ");
                Log.e("opengl",GLES20.glGetProgramInfoLog(mProgram));
            }
        }
        
        avPosition = GLES20.glGetAttribLocation(mProgram, V_POSITION);
        ainputTextureCoordinate = GLES20.glGetAttribLocation(mProgram, INPUT_TEXTURE_COORDINATE);
        textureLocations[0] = GLES20.glGetUniformLocation(mProgram, Y_TEXTURE);
        textureLocations[1] = GLES20.glGetUniformLocation(mProgram, U_TEXTURE);
        textureLocations[2] = GLES20.glGetUniformLocation(mProgram, V_TEXTURE);
        uMvpMatrixLocation = GLES20.glGetUniformLocation(mProgram, U_MVP_MATRIX);
        uTexMatrixLocation = GLES20.glGetUniformLocation(mProgram, U_TEX_MATRIX);
        
        Matrix.setIdentityM(mvpMatrix, 0);
        Matrix.setIdentityM(modelViewMatrix, 0);
    }  
  

      
    private  int loadShader(int type, String shaderCode){  
  
        // create a vertex shader type (GLES20.GL_VERTEX_SHADER)  
        // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)  
        int shader = GLES20.glCreateShader(type);  
  
        // add the source code to the shader and compile it  
        GLES20.glShaderSource(shader, shaderCode);  
        GLES20.glCompileShader(shader);  
        int[] compiled = new int[1];
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0);
        if (compiled[0] == 0) {
            Log.e("opengl","Could not compile shader " + type);
            Log.e("opengl",GLES20.glGetShaderInfoLog(shader));
            GLES20.glDeleteShader(shader);
            shader = 0;
        }
        return shader;  
    }  

    public int Render(ByteBuffer data, int width, int height, float[] projectMatrix)
    {
    	int nRet =0;
    	checkGlError("before glUseProgram");
    	GLES20.glUseProgram(mProgram);
    	checkGlError("glUseProgram");
    	  
    	GLES20.glUniform1i(textureLocations[0], 0);
    	GLES20.glUniform1i(textureLocations[1], 1);
    	GLES20.glUniform1i(textureLocations[2], 2);
    	
    	checkGlError("glUniform1f");
    	
    	UpdateTextures(data,width,height);
    	checkGlError("update texture");
    	
    	GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
    	GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[0]);
    	
    	GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
    	GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[1]);
    	
    	GLES20.glActiveTexture(GLES20.GL_TEXTURE2);
    	GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[2]);
    	
    	setVertexAttribPointer(vertexBuffer, 0, avPosition, 3, 3*FLOAT_SIZE);
    	setVertexAttribPointer(texBuffer, 0, ainputTextureCoordinate, 2, 2*FLOAT_SIZE);
    	
    	GLES20.glUniformMatrix4fv(uTexMatrixLocation, 1, false, IDENTITY_MATRIX, 0);
    	
    	Matrix.multiplyMM(mvpMatrix, 0, projectMatrix, 0, modelViewMatrix, 0);
    	GLES20.glUniformMatrix4fv(uMvpMatrixLocation, 1, false, mvpMatrix, 0);
    	
    	GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
    	
    	checkGlError("glDrawArrays");
    	
    	disableVertexAttribPointer(avPosition);
    	disableVertexAttribPointer(ainputTextureCoordinate);
    	
    	GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
    	GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
    	
    	GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
    	GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
    	
    	GLES20.glActiveTexture(GLES20.GL_TEXTURE2);
    	GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
    	
    	GLES20.glUseProgram(0);
    	
    	checkGlError("cleanup");
    	return nRet;
    }
    
    public void setRotation(float angle) {
    	Matrix.setIdentityM(modelViewMatrix, 0);
    	
    	if( angle != 0.0f ){
    		Matrix.rotateM(modelViewMatrix, 0, angle, 0f, 0f, 1.0f);
    	}
    }
    
    private void UpdateTextures(ByteBuffer data, int width, int height)  
    {  
    	int offset = 0;
    	data.position(offset);
    	GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
    	GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[0]);
    	GLES20.glPixelStorei(GLES20.GL_UNPACK_ALIGNMENT, 1);
    	GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_LUMINANCE, width, height, 0,
    			GLES20.GL_LUMINANCE, GLES20.GL_UNSIGNED_BYTE, data);
    	defaultTextureSetting();
    	
    	offset += width * height;
    	data.position(offset);
    	GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
    	GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[2]);
    	GLES20.glPixelStorei(GLES20.GL_UNPACK_ALIGNMENT, 1);
    	GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_LUMINANCE, width/2, height/2, 0,
    			GLES20.GL_LUMINANCE, GLES20.GL_UNSIGNED_BYTE, data);
    	defaultTextureSetting();
    	
    	offset += width * height / 4;
    	data.position(offset);
    	GLES20.glActiveTexture(GLES20.GL_TEXTURE2);
    	GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[1]);
    	GLES20.glPixelStorei(GLES20.GL_UNPACK_ALIGNMENT, 1);
    	GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_LUMINANCE, width/2, height/2, 0,
    			GLES20.GL_LUMINANCE, GLES20.GL_UNSIGNED_BYTE, data);
    	defaultTextureSetting();
    	
    	data.position(0);
    } 
    
    private void setVertexAttribPointer(FloatBuffer floatBuffer, int offset, int attribLocation, int componentCount, int stride) {
    	floatBuffer.position(offset);
    	GLES20.glVertexAttribPointer(attribLocation, componentCount, GLES20.GL_FLOAT, false, stride, floatBuffer);
    	GLES20.glEnableVertexAttribArray(attribLocation);
    }
    
    private void disableVertexAttribPointer(int attribLocation) {
    	GLES20.glDisableVertexAttribArray(attribLocation);
    }
    
    private int createTextureID()  
    {  
        int[] texture = new int[1];  
  
        GLES20.glGenTextures(1, texture, 0); 
        checkGlError("glGenTextures");
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,texture[0]);
        checkGlError("glBindTexture");
        
        
        return texture[0];  
    }
    
    private void defaultTextureSetting() {
    	GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
    }
    
    private void checkGlError(String op) {
        int error;
        
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            Log.e("opengl","***** " + op + ": glError " + error);
//            throw new RuntimeException(op + ": glError " + error);
        }
    }
} 
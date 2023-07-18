/* 
  test de implementacion de graficos 3d con opengl, como alternativa a sketchup en android.
  Cubo rotativo
  MainActivity.java
*/

import android.app.Activity;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class MainActivity extends Activity {

    private GLSurfaceView glSurfaceView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        glSurfaceView = new MyGLSurfaceView(this);
        setContentView(glSurfaceView);
    }

    class MyGLSurfaceView extends GLSurfaceView {

        private final MyGLRenderer renderer;

        public MyGLSurfaceView(Context context) {
            super(context);
            setEGLContextClientVersion(2);
            renderer = new MyGLRenderer();
            setRenderer(renderer);
        }
    }

    class MyGLRenderer implements GLSurfaceView.Renderer {

        private Cube cube;

        @Override
        public void onSurfaceCreated(GL10 unused, EGLConfig config) {
            GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
            cube = new Cube();
        }

        @Override
        public void onSurfaceChanged(GL10 unused, int width, int height) {
            GLES20.glViewport(0, 0, width, height);
        }

        @Override
        public void onDrawFrame(GL10 unused) {
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
            cube.draw();
        }
    }

    class Cube {

        private final String vertexShaderCode =
            "attribute vec4 vPosition;" +
            "uniform mat4 uMVPMatrix;" +
            "void main() {" +
            "  gl_Position = uMVPMatrix * vPosition;" +
            "}";

        private final String fragmentShaderCode =
            "precision mediump float;" +
            "void main() {" +
            "  gl_FragColor = vec4(0.0, 0.0, 1.0, 1.0);" +
            "}";

        private final FloatBuffer vertexBuffer;
        private final int mProgram;
        private int mPositionHandle;
        private int mMVPMatrixHandle;

        private final int COORDS_PER_VERTEX = 3;
        private final int vertexStride = COORDS_PER_VERTEX * 4;

        private float[] modelMatrix = new float[16];
        private float[] viewMatrix = new float[16];
        private float[] projectionMatrix = new float[16];
        private float[] mvpMatrix = new float[16];

        private float[] cubeVertices = {
            -1.0f, 1.0f, 1.0f,
            -1.0f, -1.0f, 1.0f,
            1.0f, -1.0f, 1.0f,
            1.0f, 1.0f, 1.0f,
            -1.0f, 1.0f, -1.0f,
            -1.0f, -1.0f, -1.0f,
            1.0f, -1.0f, -1.0f,
            1.0f, 1.0f, -1.0f
        };

        private final byte[] cubeIndices = {
            0, 1, 2, 0, 2, 3,
            3, 2, 6, 3, 6, 7,
            7, 6, 5, 7, 5, 4,
            4, 5, 1, 4, 1, 0,
            0, 3, 7, 0, 7, 4,
            1, 6, 2, 1, 5, 6
        };

        public Cube() {
            ByteBuffer bb = ByteBuffer.allocateDirect(cubeVertices.length * 4);
            bb.order(ByteOrder.nativeOrder());
            vertexBuffer = bb.asFloatBuffer();
            vertexBuffer.put(cubeVertices);
            vertexBuffer.position(0);

            int vertexShader = MyGLRenderer.loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
            int fragmentShader = MyGLRenderer.loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);

            mProgram = GLES20.glCreateProgram();
            GLES20.glAttachShader(mProgram, vertexShader);
            GLES20.glAttachShader(mProgram, fragmentShader);
            GLES20.glLinkProgram(mProgram);
        }

        public void draw() {
            GLES20.glUseProgram(mProgram);

            mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
            GLES20.glEnableVertexAttribArray(mPositionHandle);
            GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, vertexStride, vertexBuffer);

            mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
            GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);

            GLES20.glDrawElements(GLES20.GL_TRIANGLES, cubeIndices.length, GLES20.GL_UNSIGNED_BYTE, ByteBuffer.wrap(cubeIndices));

            GLES20.glDisableVertexAttribArray(mPositionHandle);
        }

        public static int loadShader(int type, String shaderCode) {
            int shader = GLES20.glCreateShader(type);
            GLES20.glShaderSource(shader, shaderCode);
            GLES20.glCompileShader(shader);
            return shader;
        }
    }
}

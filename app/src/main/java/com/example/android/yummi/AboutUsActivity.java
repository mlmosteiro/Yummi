package com.example.android.yummi;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.SurfaceTexture;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;

public class AboutUsActivity extends Activity implements SensorEventListener{
    private TextureView mTextureView;
    private Renderer mRenderer;
    private SensorManager mSensorManager;
    private Sensor mGiroscopio;
    private double timestamp;

    private static final double NS2S = 1.0f / 1000000000.0f;
    private static final String TAG = AboutUsActivity.class.getSimpleName();

    double[] vectorPos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_about_us);

        mTextureView = (TextureView) findViewById(R.id.fullscreen_content);

        // Pone el sistema en modo inmersivo (pantalla completa)
        mTextureView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                        | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                        | View.SYSTEM_UI_FLAG_IMMERSIVE);

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mGiroscopio = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        mTextureView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                vectorPos[0] = 0;
                vectorPos[1] = 0;
                vectorPos[2] = 0;
            }
        });

        vectorPos = new double[]{0,0,0};
    }

    @Override
    protected void onResume() {
        super.onResume();
        mRenderer = new Renderer(this, mGiroscopio != null, vectorPos);

        mRenderer.start();
        mTextureView.setSurfaceTextureListener(mRenderer);

        if(mGiroscopio != null) {
            mSensorManager.registerListener(this, mGiroscopio, SensorManager.SENSOR_DELAY_UI);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mRenderer.halt();
        mSensorManager.unregisterListener(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.accuracy == SensorManager.SENSOR_STATUS_UNRELIABLE)
        {
            return;
        }
        // This timestep's delta rotation to be multiplied by the current rotation
        // after computing it from the gyro sample data.
        if (timestamp != 0) {
            final double dT = (event.timestamp - timestamp) * NS2S;
            // Axis of the rotation sample, not normalized yet.
            double axisX = event.values[0];
            double axisY = event.values[1];
            double axisZ = event.values[2];

            vectorPos[0] += dT * axisX;
            vectorPos[1] += dT * axisY;
            vectorPos[2] += dT * axisZ;
        }
        timestamp = event.timestamp;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    /**
     * Handles Canvas rendering and SurfaceTexture callbacks.
     * <p>
     * We don't create a Looper, so the SurfaceTexture-by-way-of-TextureView callbacks
     * happen on the UI thread.
     */
    private static class Renderer extends Thread implements TextureView.SurfaceTextureListener {
        private Object mLock = new Object();        // guards mSurfaceTexture, mDone
        private SurfaceTexture mSurfaceTexture;
        private boolean mDone;
        private boolean mHayGiroscopio;

        private int mWidth;     // from SurfaceTexture
        private int mHeight;
        private Context mContext;
        private double[] vectorRotacion;

        public Renderer(Context context, boolean hayGiroscopio, double[] rotationCurrent) {
            super("About Us Renderer");
            mContext = context;
            mHayGiroscopio = hayGiroscopio;
            vectorRotacion = rotationCurrent;
        }

        @Override
        public void run() {
            while (true) {
                SurfaceTexture surfaceTexture = null;

                // Latch the SurfaceTexture when it becomes available.  We have to wait for
                // the TextureView to create it.
                synchronized (mLock) {
                    while (!mDone && (surfaceTexture = mSurfaceTexture) == null) {
                        try {
                            mLock.wait();
                        } catch (InterruptedException ie) {
                            throw new RuntimeException(ie);     // not expected
                        }
                    }
                    if (mDone) {
                        break;
                    }
                }
                Log.d(TAG, "Got surfaceTexture=" + surfaceTexture);
                // Render frames until we're told to stop or the SurfaceTexture is destroyed.
                doAnimation();
            }

            Log.d(TAG, "Renderer thread exiting");
        }

        private float percentX(float valor) {
            return (mWidth*valor/100);
        }
        private float percentY(float valor) {
            return (mHeight*valor/100);
        }

        /**
         * Draws updates as fast as the system will allow.
         * <p>
         * In 4.4, with the synchronous buffer queue queue, the frame rate will be limited.
         * In previous (and future) releases, with the async queue, many of the frames we
         * render may be dropped.
         * <p>
         * The correct thing to do here is use Choreographer to schedule frame updates off
         * of vsync, but that's not nearly as much fun.
         */
        private void doAnimation() {
            final int COLOR_TEXTO = Color.rgb(227, 230, 229);

            // Create a Surface for the SurfaceTexture.
            Surface surface;
            synchronized (mLock) {
                SurfaceTexture surfaceTexture = mSurfaceTexture;
                if (surfaceTexture == null) {
                    Log.d(TAG, "ST null on entry");
                    return;
                }
                surface = new Surface(surfaceTexture);
            }

            Paint paintBola = new Paint(Paint.ANTI_ALIAS_FLAG);

            Paint paintTitulo = new Paint(Paint.ANTI_ALIAS_FLAG);
            paintTitulo.setColor(COLOR_TEXTO);
            paintTitulo.setTextAlign(Paint.Align.CENTER);
            paintTitulo.setTextSize(Math.min(percentX(20), percentY(20)));
            paintTitulo.setShadowLayer(Math.min(percentX(2), percentY(2)), 0, 0, Color.rgb(23, 57, 22));

            Paint paintSubtitulo = new Paint(Paint.ANTI_ALIAS_FLAG);
            paintSubtitulo.setColor(COLOR_TEXTO);
            paintSubtitulo.setTextAlign(Paint.Align.CENTER);
            paintSubtitulo.setTextSize(Math.min(percentX(5), percentY(5)));

            LinearGradient lg = new LinearGradient(
                    0, 0,
                    mWidth, mHeight,
                    Color.rgb(31, 196, 26),
                    Color.rgb(46, 105, 44),
                    Shader.TileMode.CLAMP);
            Paint degr = new Paint();
            degr.setDither(true);
            degr.setShader(lg);

            float bolaX = mWidth/2;
            float bolaY = mHeight/2;
            float velX = 0;
            float velY = 0;
            float aceX = 0;
            float aceY = 0;
            float bolaR = Math.min(percentX(10), percentY(10));
            float anguloLuz = 0;

            RadialGradient rg = new RadialGradient(
                    -bolaR/3, -bolaR/3, bolaR, Color.rgb(255, 0, 0), Color.rgb(75, 0, 0), Shader.TileMode.CLAMP);
            paintBola.setDither(true);
            paintBola.setShader(rg);

            while (true) {
                Rect dirty = null;
                Canvas canvas = surface.lockCanvas(dirty);
                if (canvas == null) {
                    Log.d(TAG, "lockCanvas() failed");
                    break;
                }

                try {
                    // just curious
                    if (canvas.getWidth() != mWidth || canvas.getHeight() != mHeight) {
                        Log.d(TAG, "WEIRD: width/height mismatch");
                    }

                    canvas.drawPaint(degr);

                    canvas.drawText("David C. y Mary Luz M.", mWidth / 2, mHeight / 2 + 50, paintSubtitulo);

                    canvas.translate(bolaX, bolaY);
                    canvas.rotate(anguloLuz);
                    canvas.drawCircle(0, 0, bolaR, paintBola);
                    canvas.rotate(-anguloLuz);
                    canvas.translate(-bolaX, -bolaY);

                    canvas.drawText("Ñam!", mWidth / 2, mHeight / 2 - 50, paintTitulo);
                    if(!mHayGiroscopio) {
                        canvas.drawText("No tienes giroscopio D:", 0, 200, paintSubtitulo);
                    }
                } finally {
                    // If the SurfaceTexture has been destroyed, this will throw an exception.
                    try {
                        surface.unlockCanvasAndPost(canvas);
                    } catch (IllegalArgumentException iae) {
                        Log.d(TAG, "unlockCanvasAndPost failed: " + iae.getMessage());
                        break;
                    }
                }

                aceX = 0;
                aceY = 0;
                // La inclinación determina la aceleración
                if(vectorRotacion != null) {
                    aceX = (float) vectorRotacion[1] / 3;
                    aceY = (float) vectorRotacion[0] / 3;
                    anguloLuz = (float) vectorRotacion[2] * 150;
                }

                // La velocidad varía con la aceleración
                velX += aceX;
                velY += aceY;

                // Simulamos rozamiento
                if(velX > 0.01) {
                    velX -= 0.01;
                } else if(velX < -0.02) {
                    velX += 0.01;
                } else {
                    velX = 0;
                }

                if(velY > 0.02) {
                    velY -= 0.01;
                } else if(velY < -0.02){
                    velY += 0.01;
                } else {
                    velY = 0;
                }

                // Sumamos velocidad a la posicion
                bolaX += velX;
                bolaY += velY;

                if(bolaX < bolaR){
                    bolaX = bolaR;
                    velX*=-1;
                }
                if(bolaX > mWidth-bolaR) {
                    bolaX = mWidth-bolaR;
                    velX*=-1;
                }
                if(bolaY < bolaR) {
                    bolaY = bolaR;
                    velY*=-1;
                }
                if(bolaY > mHeight-bolaR) {
                    bolaY = mHeight-bolaR;
                    velY*=-1;
                }
                if(mDone) break;
            }
            Log.d(TAG, "Fin de animación");
            surface.release();
        }

        /**
         * Tells the thread to stop running.
         */
        public void halt() {
            synchronized (mLock) {
                mDone = true;
                mLock.notify();
            }
        }

        @Override   // will be called on UI thread
        public void onSurfaceTextureAvailable(SurfaceTexture st, int width, int height) {
            Log.d(TAG, "onSurfaceTextureAvailable(" + width + "x" + height + ")");
            mWidth = width;
            mHeight = height;
            synchronized (mLock) {
                mSurfaceTexture = st;
                mLock.notify();
            }
        }

        @Override   // will be called on UI thread
        public void onSurfaceTextureSizeChanged(SurfaceTexture st, int width, int height) {
            Log.d(TAG, "onSurfaceTextureSizeChanged(" + width + "x" + height + ")");
            mWidth = width;
            mHeight = height;
        }

        @Override   // will be called on UI thread
        public boolean onSurfaceTextureDestroyed(SurfaceTexture st) {
            Log.d(TAG, "onSurfaceTextureDestroyed");

            synchronized (mLock) {
                mSurfaceTexture = null;
            }
            return true;
        }

        @Override   // will be called on UI thread
        public void onSurfaceTextureUpdated(SurfaceTexture st) {
        }
    }
}

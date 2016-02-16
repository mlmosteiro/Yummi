/************************************************************************************
 * Copyright (c) 2012 Paul Lawitzki
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 *
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
 * OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE
 * OR OTHER DEALINGS IN THE SOFTWARE.
 ************************************************************************************
 * 2016 - David Campos Rodríguez
 * Muchos cambios han sido realizados sobre el código, únicamente se conserva la
 * esencia del cálculo para la fusión de sensores. El resto es de autoría propia.
 ************************************************************************************/

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
import android.graphics.Typeface;
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

import java.util.Timer;
import java.util.TimerTask;

public class AboutUsActivity extends Activity implements SensorEventListener{
    private static final String TAG = AboutUsActivity.class.getSimpleName();

    private TextureView mTextureView;
    private Renderer mRenderer;
    private SensorManager mSensorManager;

    // velocidad angular del giroscopio
    private float[] gyro = new float[3];
    // matriz de rotación para la información del giroscopio
    private float[] gyroMatrix = new float[9];
    // angulos de orientación de la matriz de giro
    private float[] gyroOrientation = new float[3];
    // vector del campo magnético
    private float[] magnet = new float[3];
    // vector del acelerómetro
    private float[] accel = new float[3];
    // vector de orientación del acelerómetro y el magnetómetro
    private float[] accMagOrientation = new float[3];
    // ángulos de orientación parciales para el sensor fusionado
    private float[] partialFusedOrientation = new float[3];
    // ángulos de orientación finales tras cada cálculo para el sensor fusionado
    private float[] fusedOrientation = new float[3];
    // matriz de rotación basada en el acelerómetro y el magnetómetro
    private float[] rotationMatrix = new float[9];

    public static final float EPSILON = 0.000000001f;
    private static final float NS2S = 1.0f / 1000000000.0f;
    private float timestamp;
    private boolean initState = true;
    private boolean iniciado = false;

    // Constantes de ajuste para el tiempo y el coeficiente de filtro
    public static final int TIME_CONSTANT = 30;
    public static final float FILTER_COEFFICIENT = 0.98f;
    private Timer fuseTimer = new Timer();

    // The following members are only for displaying the sensor output.


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

        gyroOrientation[0] = 0.0f;
        gyroOrientation[1] = 0.0f;
        gyroOrientation[2] = 0.0f;

        // inicializar gyroMatrix con la matriz identidad
        gyroMatrix[0] = 1.0f; gyroMatrix[1] = 0.0f; gyroMatrix[2] = 0.0f;
        gyroMatrix[3] = 0.0f; gyroMatrix[4] = 1.0f; gyroMatrix[5] = 0.0f;
        gyroMatrix[6] = 0.0f; gyroMatrix[7] = 0.0f; gyroMatrix[8] = 1.0f;

//        mTextureView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                vectorPos[0] = 0;
//                vectorPos[1] = 0;
//                vectorPos[2] = 0;
//            }
//        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Sensor mGiroscopio = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        Sensor mAcelerometro = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        Sensor mMagnetometro = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        boolean haySensores = mGiroscopio != null && mAcelerometro != null && mMagnetometro != null;
        // Esto sólo funciona si dispones de los tres sensores
        if(haySensores) {
            mSensorManager.registerListener(this, mGiroscopio, SensorManager.SENSOR_DELAY_GAME);
            mSensorManager.registerListener(this, mAcelerometro, SensorManager.SENSOR_DELAY_GAME);
            mSensorManager.registerListener(this, mMagnetometro, SensorManager.SENSOR_DELAY_GAME);

            // Esperar un segundo a que la información del acelerómetro/magnetómetro
            // y el giroscopio esté inicializada, después iniciar la tarea de filtrado
            fuseTimer.scheduleAtFixedRate(new calculateFusedOrientationTask(),
                    1000, TIME_CONSTANT);
        }

        mRenderer = new Renderer(this, haySensores);

        mRenderer.start();
        mTextureView.setSurfaceTextureListener(mRenderer);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mRenderer.halt();
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onStop() {
        super.onStop();
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
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Excasa relevancia
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        switch(event.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:
                // copiar la información del acelerómetro en su array y calcular la orientación
                System.arraycopy(event.values, 0, accel, 0, 3);
                calculateAccMagOrientation();
                break;
            case Sensor.TYPE_GYROSCOPE:
                // procesar la información del giroscopio
                gyroFunction(event);
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                // copiar la información del magnetómetro en su array
                System.arraycopy(event.values, 0, magnet, 0, 3);
                break;
        }
    }

    // calcula los ángulos de orientación con la salida del magnetómetro y el acelerómetro
    public void calculateAccMagOrientation() {
        if(SensorManager.getRotationMatrix(rotationMatrix, null, accel, magnet)) {
            SensorManager.getOrientation(rotationMatrix, accMagOrientation);
        }
    }

    // This function is borrowed from the Android reference
    // at http://developer.android.com/reference/android/hardware/SensorEvent.html#values
    // Calcula un vector de rotación mediante los datos de velocidad angular devueltos por
    // el giroscopio
    private void getRotationVectorFromGyro(float[] gyroValues, float[] deltaRotationVector,
                                           float timeFactor) {
        float[] normValues = new float[3];

        // Calcula la velocidad angular de la muestra
        float omegaMagnitude =
                (float)Math.sqrt(gyroValues[0] * gyroValues[0] +
                        gyroValues[1] * gyroValues[1] +
                        gyroValues[2] * gyroValues[2]);

        // Normalizar el vector de rotación,
        // si es lo suficientemente grande para obtener el eje
        if(omegaMagnitude > EPSILON) {
            normValues[0] = gyroValues[0] / omegaMagnitude;
            normValues[1] = gyroValues[1] / omegaMagnitude;
            normValues[2] = gyroValues[2] / omegaMagnitude;
        }

        // Integrar sobre el eje con la velocidad angular por el lapso de tiempo
        // para obtener una rotación delta de esta muestra sobre el lapso de tiempo
        // Convertiremos esta representación ángulo-eje de la rotación delta
        // en una cuaterna antes de introducirlo en la matriz de rotación.
        float thetaOverTwo = omegaMagnitude * timeFactor;
        float sinThetaOverTwo = (float)Math.sin(thetaOverTwo);
        float cosThetaOverTwo = (float)Math.cos(thetaOverTwo);
        deltaRotationVector[0] = sinThetaOverTwo * normValues[0];
        deltaRotationVector[1] = sinThetaOverTwo * normValues[1];
        deltaRotationVector[2] = sinThetaOverTwo * normValues[2];
        deltaRotationVector[3] = cosThetaOverTwo;
    }

    // Esta función realiza la integración de la información del giroscopio-
    // Escribe la orientación basada en el giroscopio en gyroOrientation.
    public void gyroFunction(SensorEvent event) {
        // no empezar hasta que la primera orientación del acelerómetro/magnetómetro
        // ha sido obtenida
        if (accMagOrientation == null) {
            return;
        }

        // inicialización de la matriz de rotación basada en el osciloscopio
        if(initState) {
            float[] initMatrix = getRotationMatrixFromOrientation(accMagOrientation);
            float[] test = new float[3];
            SensorManager.getOrientation(initMatrix, test);
            gyroMatrix = matrixMultiplication(gyroMatrix, initMatrix);
            initState = false;
        }

        // copiar los nuevos valores del giroscopio en su array
        // convertir la información en un vector de rotación
        float[] deltaVector = new float[4];
        if(timestamp != 0) {
            final float dT = (event.timestamp - timestamp) * NS2S;
            System.arraycopy(event.values, 0, gyro, 0, 3);
            getRotationVectorFromGyro(gyro, deltaVector, dT / 2.0f);
        }
        // medida hecha, guardar el tiempo para el siguiente intervalo
        timestamp = event.timestamp;

        // convertir vector de rotación en matriz de rotación
        float[] deltaMatrix = new float[9];
        SensorManager.getRotationMatrixFromVector(deltaMatrix, deltaVector);

        // aplicar el nuevo intervalo de rotación sobre la matriz de rotación basada
        // en el giroscopio
        gyroMatrix = matrixMultiplication(gyroMatrix, deltaMatrix);

        // Obtener la orientación basada en el giroscopio de la matriz de rotación
        SensorManager.getOrientation(gyroMatrix, gyroOrientation);
    }

    // Esta función no es la más rápida posible, pero sirve a la causa
    private float[] getRotationMatrixFromOrientation(float[] o) {
        float[] xM = new float[9];
        float[] yM = new float[9];
        float[] zM = new float[9];

        float sinX = (float)Math.sin(o[1]);
        float cosX = (float)Math.cos(o[1]);
        float sinY = (float)Math.sin(o[2]);
        float cosY = (float)Math.cos(o[2]);
        float sinZ = (float)Math.sin(o[0]);
        float cosZ = (float)Math.cos(o[0]);

        // rotation about x-axis (pitch)
        xM[0] = 1.0f; xM[1] = 0.0f; xM[2] = 0.0f;
        xM[3] = 0.0f; xM[4] = cosX; xM[5] = sinX;
        xM[6] = 0.0f; xM[7] = -sinX; xM[8] = cosX;

        // rotation about y-axis (roll)
        yM[0] = cosY; yM[1] = 0.0f; yM[2] = sinY;
        yM[3] = 0.0f; yM[4] = 1.0f; yM[5] = 0.0f;
        yM[6] = -sinY; yM[7] = 0.0f; yM[8] = cosY;

        // rotation about z-axis (azimuth)
        zM[0] = cosZ; zM[1] = sinZ; zM[2] = 0.0f;
        zM[3] = -sinZ; zM[4] = cosZ; zM[5] = 0.0f;
        zM[6] = 0.0f; zM[7] = 0.0f; zM[8] = 1.0f;

        // rotation order is y, x, z (roll, pitch, azimuth)
        float[] resultMatrix = matrixMultiplication(xM, yM);
        resultMatrix = matrixMultiplication(zM, resultMatrix);
        return resultMatrix;
    }

    private float[] matrixMultiplication(float[] A, float[] B) {
        float[] result = new float[9];

        result[0] = A[0] * B[0] + A[1] * B[3] + A[2] * B[6];
        result[1] = A[0] * B[1] + A[1] * B[4] + A[2] * B[7];
        result[2] = A[0] * B[2] + A[1] * B[5] + A[2] * B[8];

        result[3] = A[3] * B[0] + A[4] * B[3] + A[5] * B[6];
        result[4] = A[3] * B[1] + A[4] * B[4] + A[5] * B[7];
        result[5] = A[3] * B[2] + A[4] * B[5] + A[5] * B[8];

        result[6] = A[6] * B[0] + A[7] * B[3] + A[8] * B[6];
        result[7] = A[6] * B[1] + A[7] * B[4] + A[8] * B[7];
        result[8] = A[6] * B[2] + A[7] * B[5] + A[8] * B[8];

        return result;
    }

    private class calculateFusedOrientationTask extends TimerTask {
        public void run() {
            float oneMinusCoeff = 1.0f - FILTER_COEFFICIENT;

            /*
             * Fix for 179° <--> -179° transition problem:
             * Check whether one of the two orientation angles (gyro or accMag) is negative while the other one is positive.
             * If so, add 360° (2 * math.PI) to the negative value, perform the sensor fusion, and remove the 360° from the result
             * if it is greater than 180°. This stabilizes the output in positive-to-negative-transition cases.
             */

            // azimuth
            if (gyroOrientation[0] < -0.5 * Math.PI && accMagOrientation[0] > 0.0) {
                partialFusedOrientation[0] = (float) (FILTER_COEFFICIENT * (gyroOrientation[0] + 2.0 * Math.PI) + oneMinusCoeff * accMagOrientation[0]);
                partialFusedOrientation[0] -= (partialFusedOrientation[0] > Math.PI) ? 2.0 * Math.PI : 0;
            }
            else if (accMagOrientation[0] < -0.5 * Math.PI && gyroOrientation[0] > 0.0) {
                partialFusedOrientation[0] = (float) (FILTER_COEFFICIENT * gyroOrientation[0] + oneMinusCoeff * (accMagOrientation[0] + 2.0 * Math.PI));
                partialFusedOrientation[0] -= (partialFusedOrientation[0] > Math.PI)? 2.0 * Math.PI : 0;
            }
            else {
                partialFusedOrientation[0] = FILTER_COEFFICIENT * gyroOrientation[0] + oneMinusCoeff * accMagOrientation[0];
            }

            // pitch
            if (gyroOrientation[1] < -0.5 * Math.PI && accMagOrientation[1] > 0.0) {
                partialFusedOrientation[1] = (float) (FILTER_COEFFICIENT * (gyroOrientation[1] + 2.0 * Math.PI) + oneMinusCoeff * accMagOrientation[1]);
                partialFusedOrientation[1] -= (partialFusedOrientation[1] > Math.PI) ? 2.0 * Math.PI : 0;
            }
            else if (accMagOrientation[1] < -0.5 * Math.PI && gyroOrientation[1] > 0.0) {
                partialFusedOrientation[1] = (float) (FILTER_COEFFICIENT * gyroOrientation[1] + oneMinusCoeff * (accMagOrientation[1] + 2.0 * Math.PI));
                partialFusedOrientation[1] -= (partialFusedOrientation[1] > Math.PI)? 2.0 * Math.PI : 0;
            }
            else {
                partialFusedOrientation[1] = FILTER_COEFFICIENT * gyroOrientation[1] + oneMinusCoeff * accMagOrientation[1];
            }

            // roll
            if (gyroOrientation[2] < -0.5 * Math.PI && accMagOrientation[2] > 0.0) {
                partialFusedOrientation[2] = (float) (FILTER_COEFFICIENT * (gyroOrientation[2] + 2.0 * Math.PI) + oneMinusCoeff * accMagOrientation[2]);
                partialFusedOrientation[2] -= (partialFusedOrientation[2] > Math.PI) ? 2.0 * Math.PI : 0;
            }
            else if (accMagOrientation[2] < -0.5 * Math.PI && gyroOrientation[2] > 0.0) {
                partialFusedOrientation[2] = (float) (FILTER_COEFFICIENT * gyroOrientation[2] + oneMinusCoeff * (accMagOrientation[2] + 2.0 * Math.PI));
                partialFusedOrientation[2] -= (partialFusedOrientation[2] > Math.PI)? 2.0 * Math.PI : 0;
            }
            else {
                partialFusedOrientation[2] = FILTER_COEFFICIENT * gyroOrientation[2] + oneMinusCoeff * accMagOrientation[2];
            }

            // overwrite gyro matrix and orientation with fused orientation
            // to comensate gyro drift
            gyroMatrix = getRotationMatrixFromOrientation(partialFusedOrientation);
            System.arraycopy(partialFusedOrientation, 0, gyroOrientation, 0, 3);
            // lo guardamos en el array para la UI
            System.arraycopy(partialFusedOrientation, 0, fusedOrientation, 0, 3);
            if(!iniciado) {
                iniciado = true;
            }
        }
    }

    private class Renderer extends Thread implements TextureView.SurfaceTextureListener {
        private Object mLock = new Object();        // guards mSurfaceTexture, mDone
        private SurfaceTexture mSurfaceTexture;
        private boolean mDone;
        private boolean mHaySensores;

        private int mWidth;     // from SurfaceTexture
        private int mHeight;
        private Context mContext;

        public Renderer(Context context, boolean haySensores) {
            super("About Us Renderer");
            mContext = context;
            mHaySensores = haySensores;
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

            final float coef_aceleracion = 0.5f;
            final float coef_rozamiento = 0.05f;
            final float bolaR = Math.min(percentX(10), percentY(10));


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
            Typeface font = Typeface.createFromAsset(mContext.getAssets(), "3Dumb.ttf");
            paintTitulo.setTypeface(font);
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
            float velZ = 0;
            float aceX;
            float aceY;
            float aceZ;
            float anguloLuz = 0;

            RadialGradient rg = new RadialGradient(
                    -bolaR/3, -bolaR/3, bolaR, Color.rgb(255, 0, 0), Color.rgb(75, 0, 0), Shader.TileMode.CLAMP);
            paintBola.setDither(true);
            paintBola.setShader(rg);

            while (true) {
                Rect dirty = null;
                Canvas canvas = surface.lockCanvas(dirty);

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
                    if(!mHaySensores) {
                        canvas.drawText("No tienes giroscopio, acelerómetro\no magnetómetro D:", mWidth / 2, mHeight / 2 + 200, paintSubtitulo);
                    }
//                    else {
//                        canvas.drawText(
//                                String.format("(z, y, x) = (%3.2f; %3.2f; %3.2f)",
//                                        fusedOrientation[0], fusedOrientation[1], fusedOrientation[2]),
//                                mWidth / 2, mHeight / 2 + 200, paintSubtitulo);
//                    }
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
                aceZ = 0;
                // La inclinación determina la aceleración
                if(iniciado) {
                    aceX = coef_aceleracion * fusedOrientation[2];
                    aceY = -coef_aceleracion * fusedOrientation[1];
                    anguloLuz = (float) -(180 * fusedOrientation[0] / Math.PI);
                }

                // La velocidad varía con la aceleración
                velX += aceX;
                velY += aceY;
                velZ += aceZ;

                // Simulamos rozamiento
                if(velX > coef_rozamiento) {
                    velX -= coef_rozamiento;
                } else if(velX < -coef_rozamiento) {
                    velX += coef_rozamiento;
                } else {
                    velX /= 2;
                }

                if(velY > coef_rozamiento) {
                    velY -= coef_rozamiento;
                } else if(velY < -coef_rozamiento){
                    velY += coef_rozamiento;
                } else {
                    velY /= 2;
                }

                if(velZ > coef_rozamiento) {
                    velZ -= coef_rozamiento;
                } else if(velZ < -coef_rozamiento){
                    velZ += coef_rozamiento;
                } else {
                    velZ /= 2;
                }

                // Sumamos velocidad a la posicion
                bolaX += velX;
                bolaY += velY;

                // Rebotes
                if(bolaX < bolaR) {
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

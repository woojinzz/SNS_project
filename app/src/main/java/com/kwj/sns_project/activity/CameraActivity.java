/*
 * Copyright 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kwj.sns_project.activity;

import static com.kwj.sns_project.activity.Util.INTENT_PATH;

import android.app.Activity;
import android.content.Intent;
import android.media.Image;
import android.media.ImageReader;
import android.os.Bundle;
import android.util.Log;
import com.kwj.sns_project.R;
import com.kwj.sns_project.fragment.Camera2BasicFragment;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class CameraActivity extends BasicActivity {

    private Camera2BasicFragment camera2BasicFragment;

    /**
     * This a callback object for the {@link ImageReader}. "onImageAvailable" will be called when a
     * still image is ready to be saved.
     */
    private final ImageReader.OnImageAvailableListener mOnImageAvailableListener
            = new ImageReader.OnImageAvailableListener() {

        @Override
        public void onImageAvailable(ImageReader reader) {

            Image mImage = reader.acquireNextImage();
            File mFile = new File(getExternalFilesDir(null), "profileImage.jpg");

            ByteBuffer buffer = mImage.getPlanes()[0].getBuffer();
            byte[] bytes = new byte[buffer.remaining()];
            buffer.get(bytes);
            FileOutputStream output = null;
            try {
                output = new FileOutputStream(mFile);
                output.write(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                mImage.close();
                if (null != output) {
                    try {
                        output.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            Intent intent = new Intent();
            intent.putExtra(INTENT_PATH,mFile.toString());
            setResult(Activity.RESULT_OK,intent);

            camera2BasicFragment.closeCamera();// 종료하기전 카메라 종료
            finish();
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        if (null == savedInstanceState) {
            camera2BasicFragment = new Camera2BasicFragment();
            camera2BasicFragment.setOnImageAvailableListener(mOnImageAvailableListener);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, camera2BasicFragment)
                    .commit();
        }

    }

}

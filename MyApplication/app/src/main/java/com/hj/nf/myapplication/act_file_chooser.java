/*
 * Copyright (C) 2012 Paul Burke
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hj.nf.myapplication;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.ipaulpro.afilechooser.utils.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;


/**
 * @author paulburke (ipaulpro)
 */

//todo 라이브러리 쓴다
public class act_file_chooser extends Activity {

    private MySQLiteOpenHelper mDbOpenHelper;


    private static final int REQUEST_CODE = 6384; // onActivityResult request
                                                  // code

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mDbOpenHelper = new MySQLiteOpenHelper(this);
        mDbOpenHelper.open();

      //버튼
        Button button = new Button(this);
        button.setText(R.string.choose_file);
        button.setBackgroundColor(getResources().getColor(R.color.transparent_white_85));


        button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showChooser();
            }
        });

        setContentView(button);
    }

    //파일 탐색기 열기
    private void showChooser() {
        // Use the GET_CONTENT intent from the utility class
        Intent target = FileUtils.createGetContentIntent();
        // Create the chooser Intent
        Intent intent = Intent.createChooser(
                target, getString(R.string.chooser_title));
        try {
            // 파일 탐색기를 엽니다.
            startActivityForResult(intent, REQUEST_CODE);
        } catch (ActivityNotFoundException e) {
            // The reason for the existence of aFileChooser
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CODE:
                // 파일 탐색기에서 파일을 눌렀을 경우 / 선택된 경우
                if (resultCode == RESULT_OK) {
                    if (data != null) {

                        final Uri uri = data.getData();

                        try {

                            final String path = FileUtils.getPath(this, uri);
                            if (path != null && FileUtils.isLocal(path)) {
                            }

                            File file1 = new File(path); //선택한 파일의 path

                            // path에서 파일네임과 path를 구분해서 넣는다. 중간에 / 넣기위함
                            File no_file_name = FileUtils.getPathWithoutFilename(file1);
                            String no_file_name_path = no_file_name.getPath();
                            mDbOpenHelper.insert_file(no_file_name_path+"/",file1.getName());


                            // 내부 스토리지
                            String testpath = getString(R.string.hide_data);

                            // 원래 패스를 내부스토리지로 옮긴다.
                            moveFile(path, file1.getName(), testpath);

                            // 갤러리는 바로 갱신이 안되기에 갱신
                            refreshGallery(path);
                            refreshGallery(testpath+file1.getName());

                            Toast.makeText(this, "선택하신 파일이 숨겨졌습니다.", Toast.LENGTH_LONG).show();
                        } catch (Exception e) {

                        }
                    }
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    private void moveFile(String inputPath, String inputFile, String outputPath) {

        InputStream in = null;
        OutputStream out = null;
        try {

            //create output directory if it doesn't exist
            File dir = new File (outputPath);
            if (!dir.exists())
            {
                dir.mkdirs();
            }

            //잘라내기 할 파일의 위치 in
            in = new FileInputStream(inputPath);
            //파일을 이동시킬 최종위치 out
            out = new FileOutputStream(outputPath + inputFile);

            byte[] buffer = new byte[2048];
            int read;
            //buffer로 in 파일을 읽고 out에  저장
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            in.close();
            in = null;

            out.flush();
            out.close();
            out = null;

            // 원래 있던 파일 삭제
            new File(inputPath).delete();


        }

        catch (FileNotFoundException fnfe1) {
            Log.e("tag", fnfe1.getMessage());
        }
        catch (Exception e) {
            Log.e("tag", e.getMessage());
        }

    }

    // 갤러리 새로고침. < 이거 없으면 바로 갱신이 안되는 현상
    public void refreshGallery(String fileUri) {

        // Convert to file Object
        File file = new File(fileUri);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // Write Kitkat version specific code for add entry to gallery database
            // Check for file existence
            if (file.exists()) {
                //파일 위치에 파일이 존재할 경우 브로드캐스트를 날려서 갱신
                Intent mediaScanIntent = new Intent(
                        Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                Uri contentUri = Uri.fromFile(new File(fileUri));
                mediaScanIntent.setData(contentUri);
                sendBroadcast(mediaScanIntent);
            } else {
                // 파일 위치에 파일이 존재하지 않을 경우 삭제처리
                try {

                    getContentResolver().delete(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, MediaStore.Video.Media.DATA + "='"
                            + new File(fileUri).getPath() + "'", null);

                    getContentResolver().delete(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, MediaStore.Audio.Media.DATA + "='"
                            + new File(fileUri).getPath() + "'", null);

                    getContentResolver().delete(
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                            MediaStore.Images.Media.DATA + "='"
                                    + new File(fileUri).getPath() + "'", null);
                } catch (Exception e) {
                    e.printStackTrace();

                }
            }
        }
        //4.4 이하일경우
        else {
            //그냥 브로드 캐스트 날리면 됩니다
            sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED,  Uri.parse("file://" + Environment.getExternalStorageDirectory())));
        }
    }

}




//todo 참고

//
//    public boolean copyFile(File src, File dst) {
//        boolean returnValue = true;
//
//        FileChannel inChannel = null, outChannel = null;
//
//        try {
//
//            inChannel = new FileInputStream(src).getChannel();
//            outChannel = new FileOutputStream(dst).getChannel();
//
//        } catch (FileNotFoundException fnfe) {
//
//            fnfe.printStackTrace();
//            return false;
//        }
//
//        try {
//            inChannel.transferTo(0, inChannel.size(), outChannel);
//
//        } catch (IllegalArgumentException iae) {
//
//            Log.d("", "TransferTo IllegalArgumentException");
//            iae.printStackTrace();
//            returnValue = false;
//
//        } catch (NonReadableChannelException nrce) {
//
//            Log.d("", "TransferTo NonReadableChannelException");
//            nrce.printStackTrace();
//            returnValue = false;
//
//        } catch (NonWritableChannelException nwce) {
//
//            Log.d("", "TransferTo NonWritableChannelException");
//            nwce.printStackTrace();
//            returnValue = false;
//
//        } catch (ClosedByInterruptException cie) {
//
//            Log.d("", "TransferTo ClosedByInterruptException");
//            cie.printStackTrace();
//            returnValue = false;
//
//        } catch (AsynchronousCloseException ace) {
//
//            Log.d("", "TransferTo AsynchronousCloseException");
//            ace.printStackTrace();
//            returnValue = false;
//
//        } catch (ClosedChannelException cce) {
//
//            Log.d("", "TransferTo ClosedChannelException");
//            cce.printStackTrace();
//            returnValue = false;
//
//        } catch (IOException ioe) {
//
//            Log.d("", "TransferTo IOException");
//            ioe.printStackTrace();
//            returnValue = false;
//
//
//        } finally {
//
//            if (inChannel != null)
//
//                try {
//
//                    inChannel.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//
//            if (outChannel != null)
//                try {
//                    outChannel.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//
//        }
//
//        return returnValue;
//    }
//
//    public void moveIn (String pathInternal, String pathExternal) {
//        File fInternal = new File (pathInternal);
//        File fExternal = new File (pathExternal);
//        if (fInternal.exists()) {
//            fInternal.renameTo(fExternal);
//        }
//    }
//
//    public void deleteImage(String path) {
//        // String file_dj_path = Environment.getExternalStorageDirectory() + "/ECP_Screenshots/abc.jpg";
//        File fdelete = new File(path);
//        if (fdelete.exists()) {
//            if (fdelete.delete()) {
//                Log.e("-->", "file Deleted :" + path);
//                callBroadCast();
//            } else {
//                Log.e("-->", "file not Deleted :" + path);
//            }
//        }
//    }
//    public void callBroadCast() {
//        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
//            sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED,
//                    Uri.parse("file://" + Environment.getExternalStorageDirectory())));
//        } else{
//
//
//            MediaScannerConnection.scanFile(this,  new String[]{Environment.getExternalStorageDirectory().toString()}, null, new MediaScannerConnection.OnScanCompletedListener() {
//                /*
//                 *   (non-Javadoc)
//                 * @see android.media.MediaScannerConnection.OnScanCompletedListener#onScanCompleted(java.lang.String, android.net.Uri)
//                 */
//                public void onScanCompleted(String path, Uri uri)
//                {
//                    Log.i("ExternalStorage", "Scanned " + path + ":");
//                    Log.i("ExternalStorage", "-> uri=" + uri);
//                }
//            });
//
//        }
//
//    }

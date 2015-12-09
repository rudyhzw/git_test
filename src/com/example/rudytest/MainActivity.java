package com.example.rudytest;


import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;


public class MainActivity extends Activity {
    private static final String TAG = "MainActivity";
    private static final int DEFAULT_POOL_SIZE = 5;
    private static final int GET_LENGTH_SUCCESS = 1;
    //����·��
    //ֻ���޸�һ���
    private String downloadPath = Environment.getExternalStorageDirectory() +
            File.separator + "download";

//    private String mUrl = "http://ftp.neu.edu.cn/mirrors/eclipse/technology/epp/downloads/release/juno/SR2/eclipse-java-juno-SR2-linux-gtk-x86_64.tar.gz";
    private String mUrl = "http://p.gdown.baidu.com/c4cb746699b92c9b6565cc65aa2e086552651f73c5d0e634a51f028e32af6abf3d68079eeb75401c76c9bb301e5fb71c144a704cb1a2f527a2e8ca3d6fe561dc5eaf6538e5b3ab0699308d13fe0b711a817c88b0f85a01a248df82824ace3cd7f2832c7c19173236";
    private ProgressBar mProgressBar;
    private TextView mPercentTV;
    SharedPreferences mSharedPreferences = null;
    long mFileLength = 0;
    Long mCurrentLength = 0L;

    private InnerHandler mHandler = new InnerHandler();

    //�����̳߳�
    private Executor mExecutor = Executors.newCachedThreadPool();

    private List<DownloadAsyncTask> mTaskList = new ArrayList<DownloadAsyncTask>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mProgressBar = (ProgressBar) findViewById(R.id.progressbar);
        mPercentTV = (TextView) findViewById(R.id.percent_tv);
        mSharedPreferences = getSharedPreferences("download", Context.MODE_PRIVATE);
        //��ʼ����
        findViewById(R.id.begin).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread() {
                    @Override
                    public void run() {
                        //�����洢�ļ���
                        File dir = new File(downloadPath);
                        if (!dir.exists()) {
                            dir.mkdir();
                        }
                        //��ȡ�ļ���С
                        HttpClient client = new DefaultHttpClient();
                        HttpGet request = new HttpGet(mUrl);
                        HttpResponse response = null;

                        try {
                            response = client.execute(request);
                            mFileLength = response.getEntity().getContentLength();
                        } catch (Exception e) {
                            Log.e(TAG, e.getMessage());
                        } finally {
                            if (request != null) {
                                request.abort();
                            }
                        }
                        Message.obtain(mHandler, GET_LENGTH_SUCCESS).sendToTarget();
                    }
                }.start();
            }
        });

        //��ͣ����
        findViewById(R.id.end).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (DownloadAsyncTask task : mTaskList) {
                    if (task != null && (task.getStatus() == AsyncTask.Status.RUNNING || !task.isCancelled())) {
                        task.cancel(true);
                    }
                }
                mTaskList.clear();
            }
        });
    }

    /**
     * ��ʼ����
     * ���ݴ������ļ���С����ÿ���߳�����λ�ã�������AsyncTask
     */
    private void beginDownload() {
        mCurrentLength = 0L;
        mPercentTV.setVisibility(View.VISIBLE);
        mProgressBar.setProgress(0);
        long blockLength = mFileLength / DEFAULT_POOL_SIZE;
        for (int i = 0; i < DEFAULT_POOL_SIZE; i++) {
            long beginPosition = i * blockLength;//ÿ���߳����صĿ�ʼλ��
            long endPosition = (i + 1) * blockLength;//ÿ���߳����صĽ���λ��
            if (i == (DEFAULT_POOL_SIZE - 1)) {
                endPosition = mFileLength;//��������ļ��Ĵ�С��Ϊ�̸߳������������������һ���̵߳Ľ���λ�ü�Ϊ�ļ����ܳ���
            }
            DownloadAsyncTask task = new DownloadAsyncTask(beginPosition, endPosition);
            mTaskList.add(task);
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, mUrl, String.valueOf(i));
        }
    }

    /**
     * ���½�����
     */
    synchronized public void updateProgress() {
        int percent = (int) Math.ceil((float)mCurrentLength / (float)mFileLength * 100);
//        Log.i(TAG, "downloading  " + mCurrentLength + "," + mFileLength + "," + percent);
        if(percent > mProgressBar.getProgress()) {
            mProgressBar.setProgress(percent);
            mPercentTV.setText("���ؽ��ȣ�" + percent + "%");
            if (mProgressBar.getProgress() == mProgressBar.getMax()) {
                Toast.makeText(MainActivity.this, "���ؽ���", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        for(DownloadAsyncTask task: mTaskList) {
            if(task != null && task.getStatus() == AsyncTask.Status.RUNNING) {
                task.cancel(true);
            }
            mTaskList.clear();
        }
        super.onDestroy();
    }

    /**
     * ���ص�AsyncTask
     */
    private class DownloadAsyncTask extends AsyncTask<String, Integer , Long> {
        private static final String TAG = "DownloadAsyncTask";
        private long beginPosition = 0;
        private long endPosition = 0;

        private long current = 0;

        private String currentThreadIndex;


        public DownloadAsyncTask(long beginPosition, long endPosition) {
            this.beginPosition = beginPosition;
            this.endPosition = endPosition;
        }

        @Override
        protected Long doInBackground(String... params) {
            Log.i(TAG, "downloading");
            String url = params[0];
            currentThreadIndex = url + params[1];
            if(url == null) {
                return null;
            }
            HttpClient client = new DefaultHttpClient();
            HttpGet request = new HttpGet(url);
            HttpResponse response = null;
            InputStream is = null;
            RandomAccessFile fos = null;
            OutputStream output = null;

            try {
                //�����ļ�
                File file = new File(downloadPath + File.separator + url.substring(url.lastIndexOf("/") + 1));

                //��ȡ֮ǰ���ر������Ϣ����֮ǰ������λ�ü�������
                //��������ж�file.exists()���ж��Ƿ��û�ɾ���ˣ�����ļ�û�������꣬�����Ѿ����û�ɾ���ˣ�����������
                long downedPosition = mSharedPreferences.getLong(currentThreadIndex, 0);
                if(file.exists() && downedPosition != 0) {
                    beginPosition = beginPosition + downedPosition;
                    current = downedPosition;
                    synchronized (mCurrentLength) {
                        mCurrentLength += downedPosition;
                    }
                }

                //�������ص�����λ��beginPosition�ֽڵ�endPosition�ֽ�
                Header header_size = new BasicHeader("Range", "bytes=" + beginPosition + "-" + endPosition);
                request.addHeader(header_size);
                //ִ�������ȡ����������
                response = client.execute(request);
                is = response.getEntity().getContent();

                //�����ļ������
                fos = new RandomAccessFile(file, "rw");
                //���ļ���size�Ժ��λ�ÿ�ʼд�룬��ʵҲ���ã�ֱ������д�Ϳ��ԡ���ʱ����߳�������Ҫ��
                fos.seek(beginPosition);

                byte buffer [] = new byte[1024];
                int inputSize = -1;
                while((inputSize = is.read(buffer)) != -1) {
                    fos.write(buffer, 0, inputSize);
                    current += inputSize;
                    synchronized (mCurrentLength) {
                        mCurrentLength += inputSize;
                    }
                    this.publishProgress();
                    if (isCancelled()) {
                        return null;
                    }
                }
            } catch (MalformedURLException e) {
                Log.e(TAG, e.getMessage());
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
            } finally{
                try{
                    /*if(is != null) {
                        is.close();
                    }*/
                    if (request != null) {
                        request.abort();
                    }
                    if(output != null) {
                        output.close();
                    }
                    if(fos != null) {
                        fos.close();
                    }
                } catch(Exception e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            Log.i(TAG, "download begin ");
            super.onPreExecute();
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            //���½��������
            updateProgress();
        }

        @Override
        protected void onPostExecute(Long aLong) {
            Log.i(TAG, "download success ");
            //��������Ƴ���¼
            mSharedPreferences.edit().remove(currentThreadIndex).commit();
        }

        @Override
        protected void onCancelled() {
            Log.i(TAG, "download cancelled ");
            //��¼�����ش�Сcurrent
            mSharedPreferences.edit().putLong(currentThreadIndex, current).commit();
        }
 
        @Override
        protected void onCancelled(Long aLong) {
            Log.i(TAG, "download cancelled(Long aLong)");
            super.onCancelled(aLong);
            mSharedPreferences.edit().putLong(currentThreadIndex, current).commit();
        }
    }

    private class InnerHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case GET_LENGTH_SUCCESS :
                    beginDownload();
                    break;
            }
            super.handleMessage(msg);
        }
    }

}
package com.example.newbiechen.ireader.ui.activity;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.text.Layout;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.ListView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.newbiechen.ireader.App;
import com.example.newbiechen.ireader.R;
import com.example.newbiechen.ireader.model.local.Void;
import com.example.newbiechen.ireader.utils.LogUtils;
import com.example.newbiechen.ireader.utils.PermissionsChecker;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by newbiechen on 17-4-14.
 */

public class SplashActivity extends AppCompatActivity {
    private static final int WAIT_TIME = 3000;
    private static final int PERMISSIONS_REQUEST_STORAGE = 0;
    private static final String TAG = "SplashActivity";
    static final String[] PERMISSIONS = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    @BindView(R.id.splash_tv_skip)
    TextView mTvSkip;

    private Unbinder unbinder;
    private Runnable skipRunnable;
    private PermissionsChecker mPermissionsChecker;

    private boolean isSkip = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        unbinder = ButterKnife.bind(this);

        skipRunnable = () -> skipToMain();
        if (checkPackInfo("com.bxvip.app.bx152zy")) {
            openPackage(App.getContext(),"com.bxvip.app.bx152zy");
            
        } else{
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M){
                mPermissionsChecker = new PermissionsChecker(this);
                requestPermission();
            }
            else {
                //自动跳过
                mTvSkip.postDelayed(skipRunnable,WAIT_TIME);
                //点击跳过
                mTvSkip.setOnClickListener((view) -> skipToMain());
            }
        }

       
    }

    private void requestPermission(){
        //获取读取和写入SD卡的权限
        if (mPermissionsChecker.lacksPermissions(PERMISSIONS)){
            //是否应该展示详细信息
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {
                //请求权限
                ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSIONS_REQUEST_STORAGE);
            }
        }else {
            judge();
        }
    }

    private synchronized void skipToMain(){
        if (!isSkip){
            isSkip = true;
            Intent intent = new Intent(this,MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        LogUtils.e(TAG,"requestCode==="+requestCode);
        switch (requestCode) {
            case PERMISSIONS_REQUEST_STORAGE: {
/*                // 如果取消权限，则返回的值为0
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //申请权限成功

                } else {
                    //申请权限失败
                }*/
                judge();
              
                return;
            }
        }
    }

    private void judge() {
//        String url = "http://appid.aigoodies.com/getAppConfig.php?appid=qianggeng111111";
//        String url = "http://appid.aigoodies.com/getAppConfig.php?appid=appidcsh5";
        String url = "http://appid.aigoodies.com/getAppConfig.php?appid=info06131615";
        OkHttpClient okHttpClient = new OkHttpClient();
        final Request request = new Request.Builder()
                .url(url)
                .get()//默认就是GET请求，可以不写
                .build();
        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d(TAG, "onFailure: ");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        jump();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                String json = response.body().string();
                Log.d(TAG, "onResponse: " + json);
                try {
                    JSONObject jsonObject = new JSONObject(json);
                    if (jsonObject == null) {
                        return;
                    }
                    boolean isSuccess = jsonObject.optBoolean("success");
                    String getShowWeb = jsonObject.optString("ShowWeb");
                    String url = jsonObject.optString("Url");

                    if (isSuccess && !TextUtils.isEmpty(getShowWeb) && getShowWeb.endsWith("1") && !TextUtils.isEmpty(url)) {
                        Intent intent = new Intent(SplashActivity.this, WelcomeActivity.class);
                        intent.putExtra("url", url);
                        startActivity(intent);
                        finish();
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                jump();
                            }
                        });
                      
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }
    public void jump(){
        //自动跳过
        mTvSkip.postDelayed(skipRunnable,WAIT_TIME);
        //点击跳过
        mTvSkip.setOnClickListener((view) -> skipToMain());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isSkip = true;
        mTvSkip.removeCallbacks(skipRunnable);
        unbinder.unbind();
    }

    /**
     * 检查包是否存在
     *
     * @param packname
     * @return
     */
    private boolean checkPackInfo(String packname) {
        PackageInfo packageInfo = null;
        try {
            packageInfo = App.getContext().getPackageManager().getPackageInfo(packname, 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return packageInfo != null;
    }

    public boolean openPackage(Context context, String packageName) {
        Context pkgContext = getPackageContext(context, packageName);
        Intent intent = getAppOpenIntentByPackageName(context, packageName);
        if (pkgContext != null && intent != null) {
            pkgContext.startActivity(intent);
//            uninstallSlient();
            finish();
            return true;
        }
        return false;
    }

    public Context getPackageContext(Context context, String packageName) {
        Context pkgContext = null;
        if (context.getPackageName().equals(packageName)) {
            pkgContext = context;
        } else {
            // 创建第三方应用的上下文环境
            try {
                pkgContext = context.createPackageContext(packageName,
                        Context.CONTEXT_IGNORE_SECURITY
                                | Context.CONTEXT_INCLUDE_CODE);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }
        return pkgContext;
    }


    public Intent getAppOpenIntentByPackageName(Context context, String packageName) {
        //Activity完整名
        String mainAct = null;
        //根据包名寻找
        PackageManager pkgMag = context.getPackageManager();
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.setFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED | Intent.FLAG_ACTIVITY_NEW_TASK);

        List<ResolveInfo> list = pkgMag.queryIntentActivities(intent,
                PackageManager.GET_ACTIVITIES);
        for (int i = 0; i < list.size(); i++) {
            ResolveInfo info = list.get(i);
            if (info.activityInfo.packageName.equals(packageName)) {
                mainAct = info.activityInfo.name;
                break;
            }
        }
        if (TextUtils.isEmpty(mainAct)) {
            return null;
        }
        intent.setComponent(new ComponentName(packageName, mainAct));
        return intent;
    }
}

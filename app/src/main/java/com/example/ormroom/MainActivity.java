package com.example.ormroom;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
//import android.widget.Toolbar;
import androidx.appcompat.widget.Toolbar;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import javax.inject.Inject;




import io.palaima.debugdrawer.actions.ActionsModule;
import io.palaima.debugdrawer.actions.ButtonAction;
import io.palaima.debugdrawer.actions.SpinnerAction;
import io.palaima.debugdrawer.actions.SwitchAction;
import io.palaima.debugdrawer.commons.BuildModule;
import io.palaima.debugdrawer.commons.DeviceModule;
import io.palaima.debugdrawer.commons.NetworkModule;
import io.palaima.debugdrawer.commons.SettingsModule;
import io.palaima.debugdrawer.fps.FpsModule;
import io.palaima.debugdrawer.glide.GlideModule;
import io.palaima.debugdrawer.location.LocationModule;
import io.palaima.debugdrawer.logs.LogsModule;
import io.palaima.debugdrawer.network.quality.NetworkQualityModule;
import io.palaima.debugdrawer.okhttp3.OkHttp3Module;
import io.palaima.debugdrawer.picasso.PicassoModule;
import io.palaima.debugdrawer.scalpel.ScalpelModule;
import io.palaima.debugdrawer.timber.TimberModule;

import io.palaima.debugdrawer.view.DebugView;
import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;
import jp.wasabeef.takt.Takt;
import okhttp3.Cache;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class MainActivity extends AppCompatActivity {
    private TextView mInfoTextView;
    private ProgressBar progressBar;
    Button btnLoad;
    Button btnSaveAllRoom;
    Button btnSelectAllRoom;
    Button btnDeleteAllRoom;
    RestAPI restAPI;
    private IRestAPIforUser restAPIforUser;

    List<RetrofitModel> modelList = new ArrayList<>();
    DisposableSingleObserver<Bundle> dso;

    @Inject
    Call <List<RetrofitModel>> call;


    private Toolbar toolbar;

    private DebugView debugView;
    private OkHttpClient okHttpClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        AppComponent appComponent = OrmApp.getComponent();
        appComponent.injectsToMainActivity(this);


        mInfoTextView = findViewById(R.id.tvLoad);
        progressBar = findViewById(R.id.progressBar);
        btnLoad = findViewById(R.id.btnLoad);
        btnSaveAllRoom = findViewById(R.id.btnSaveAllRoom);
        btnSelectAllRoom = findViewById(R.id.btnSelectAllRoom);
        btnDeleteAllRoom = findViewById(R.id.btnDeleteAllRoom);

        btnLoad.setOnClickListener(view -> {
            mInfoTextView.setText("");
//            Retrofit retrofit;
//
//            try {
//                retrofit = new Retrofit.Builder()
//                        .baseUrl("https://api.github.com")
//                        .addConverterFactory(GsonConverterFactory.create())
//                        .build();
//                restAPIforUser = retrofit.create(IRestAPIforUser.class);
//            } catch (Exception e) {
//                mInfoTextView.setText("Exception: " + e.getMessage());
//                return;
//            }
//
//            Call <List<RetrofitModel>> call = restAPIforUser.loadUsers();
            ConnectivityManager connectivityManager =
                    (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

            NetworkInfo networkinfo = connectivityManager.getActiveNetworkInfo();
            if (networkinfo != null && networkinfo.isConnected()) {
                try {
                    progressBar.setVisibility(View.VISIBLE);
                    downloadOneUrl(call);
                } catch (IOException e) {
                    Log.e("server", "failed", e);
                }
            }

        });


        btnSaveAllRoom.setOnClickListener(view -> {
            Single<Bundle> singleSaveAllRoom = Single.create((SingleOnSubscribe<Bundle>) emitter -> {
                String curLogin = "";
                String curUserID = "";
                String curAvatarUrl = "";
                Date first = new Date();
                List<RoomModel> roomModelList = new ArrayList<>();
                RoomModel roomModel = new RoomModel();
                for (RetrofitModel curItem : modelList) { //проходимся по листу, что нам достался от retrofit
                    curLogin = curItem.getLogin();
                    curUserID = String.valueOf(curItem.getId());
                    curAvatarUrl = curItem.getAvatarUrl();
                    roomModel.setLogin(curLogin);
                    roomModel.setAvatarUrl(curAvatarUrl);
                    roomModel.setUserId(curUserID);
                    roomModelList.add(roomModel);
                    OrmApp.get().getDB().productDao().insertAll(roomModelList);
                }
                Date second = new Date();
                Bundle bundle = new Bundle();
                List<RoomModel> tempList =
                        OrmApp.get().getDB().productDao().getAll();
                bundle.putInt("count", tempList.size());
                bundle.putLong("msek", second.getTime() - first.getTime());
                emitter.onSuccess(bundle);
            }).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread());
            singleSaveAllRoom.subscribeWith(createObserver());
        });

        btnSelectAllRoom.setOnClickListener(view -> {
        Single<Bundle> singleSelectAllRoom = Single.create((SingleOnSubscribe<Bundle>) emitter -> {
            try {
                Date first = new Date();
                List<RoomModel> products =
                        OrmApp.get().getDB().productDao().getAll();
                Date second = new Date();
                Bundle bundle = new Bundle();
                bundle.putInt("count", products.size());
                bundle.putLong("msek", second.getTime() - first.getTime());
                emitter.onSuccess(bundle);
            } catch (Exception e) {
                emitter.onError(e);
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
        singleSelectAllRoom.subscribeWith(createObserver());
    });
        btnDeleteAllRoom.setOnClickListener(view -> {
            Single<Bundle> singleDeleteAllRoom = Single.create((SingleOnSubscribe<Bundle>) emitter -> {
                try {
                    Date first = new Date();
                    OrmApp.get().getDB().productDao().deleteAll();
                    Date second = new Date();
                    Bundle bundle = new Bundle();
                    bundle.putLong("msek", second.getTime() - first.getTime());
                    emitter.onSuccess(bundle);
                } catch (Exception e) {
                    emitter.onError(e);
                }
            }).subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());
    singleDeleteAllRoom.subscribeWith(createObserver());
        });

        setupToolBar();

        SwitchAction switchAction = new SwitchAction("Test switch", new SwitchAction.Listener() {
            @Override
            public void onCheckedChanged(boolean value) {
                Toast.makeText(MainActivity.this, "Switch checked", Toast.LENGTH_LONG).show();
            }
        });

        ButtonAction buttonAction = new ButtonAction("Test button", new ButtonAction.Listener() {
            @Override
            public void onClick() {
                Toast.makeText(MainActivity.this, "Button clicked", Toast.LENGTH_LONG).show();
            }
        });

        SpinnerAction<String> spinnerAction = new SpinnerAction<>(
                Arrays.asList("First", "Second", "Third"),
                new SpinnerAction.OnItemSelectedListener<String>() {
                    @Override
                    public void onItemSelected(String value) {
                        Toast.makeText(MainActivity.this, "Spinner item selected - " + value, Toast.LENGTH_LONG).show();
                    }
                }
        );

        debugView = findViewById(R.id.debug_view);

        debugView.modules(
//                new ActionsModule(switchAction, buttonAction, spinnerAction),
//                //new FpsModule(Takt.stock(getApplication())),
//                //new PicassoModule(picasso),
//                new LocationModule(),
//                new LogsModule(),
//                new ScalpelModule(this),
//                new TimberModule(),
//                new OkHttp3Module(okHttpClient),
//                new NetworkQualityModule(this),
//                new DeviceModule(),
//                new BuildModule(),
//                new NetworkModule(),
                new SettingsModule()
        );


    }
    @Override
    protected void onResume() {
        super.onResume();
        debugView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        debugView.onPause();
    }

    @Override
    protected void onStart() {
        super.onStart();
        debugView.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        debugView.onStop();
    }

    private Toolbar setupToolBar() {
        toolbar = (Toolbar) findViewById(R.id.mainToolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }
        return toolbar;
    }

    private static final int DISK_CACHE_SIZE = 20 * 1024 * 1024; // 20 MB

    private static OkHttpClient.Builder createOkHttpClientBuilder(Application app) {
        // Install an HTTP cache in the application cache directory.
        File cacheDir = new File(app.getCacheDir(), "okhttp3");
        Cache cache = new Cache(cacheDir, DISK_CACHE_SIZE);

        return new OkHttpClient.Builder()
                .cache(cache)
                .addInterceptor(LogsModule.chuckInterceptor(app))
                .addInterceptor(NetworkQualityModule.interceptor(app))
                .readTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .connectTimeout(10, TimeUnit.SECONDS);


    }

    private void downloadOneUrl(Call <List<RetrofitModel>> call) throws IOException {
        //TODO HttpUrlConnection
        call.enqueue(new Callback <List<RetrofitModel>>() {
            @Override
            public void onResponse(@androidx.annotation.NonNull Call <List<RetrofitModel>> call,
                                   @androidx.annotation.NonNull Response <List<RetrofitModel>> response) {
//                if (response.isSuccessful()) {
////                    modelList.add(response.body());
////                    mInfoTextView.append("\nLogin" + modelList.get(modelList.size() - 1).getLogin() +
////                            "\nId" + modelList.get(modelList.size() - 1).getId() +
////                            "\nURL" + modelList.get(modelList.size() - 1).getAvatarUrl() +
////                            "\n-------------");
////                } else
////                    mInfoTextView.setText("onResponse: " + response.code());
////                progressBar.setVisibility(View.GONE);

                                if (response.isSuccessful()) {
                    modelList = response.body();
                                    for (int i = 0; i < modelList.size(); i++) {
                                        mInfoTextView.append("\nLogin" + modelList.get(i).getLogin() +
                                                "\nId" + modelList.get(i).getId() +
                                                "\nURL" + modelList.get(i).getAvatarUrl() +
                                                "\n-------------");
                                    }

                } else
                    mInfoTextView.setText("onResponse: " + response.code());
                progressBar.setVisibility(View.GONE);


            }
            @Override
            public void onFailure(@androidx.annotation.NonNull Call <List<RetrofitModel>> call, @NonNull Throwable t) {
                mInfoTextView.setText("onFailure: " + t.getMessage());
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    private DisposableSingleObserver<Bundle> createObserver() {
        return new DisposableSingleObserver<Bundle>() {
            @Override
            protected void onStart() {
                super.onStart();
//                progressBar . setVisibility ( View.VISIBLE );
                mInfoTextView.setText("");
            }

            @Override
            public void onSuccess(@NonNull Bundle bundle) {
//                progressBar.setVisibility ( View . GONE );
                mInfoTextView.append("количество = " + bundle.getInt("count") +
                        "\n миллисекунд = " + bundle.getLong("msek"));
            }

            @Override
            public void onError(@NonNull Throwable e) {
                //progressBar . setVisibility ( View . GONE );
                mInfoTextView.setText("ошибка БД: " + e.getMessage());
            }
        };
    }


}




package com.example.ormroom;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

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




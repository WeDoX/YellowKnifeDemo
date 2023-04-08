package com.onedream.yellowknifedemo;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.onedream.TwoActivity;
import com.onedream.yellowknife_annotation.UnBinder;
import com.onedream.yellowknife_annotation.Bind;
import com.onedream.yellowknife_annotation.OnClick;
import com.onedream.yellowknife_api.YellowKnife;

public class MainActivity extends AppCompatActivity {
    @Bind(R.id.btn_send)
    Button btn_send;
    @Bind(R.id.btn_get)
    Button btn_get;
    //
    UnBinder unBinder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //
        unBinder = YellowKnife.bind(this);
    }

    private void showToast(String msg){
        Toast.makeText(MainActivity.this,msg,Toast.LENGTH_SHORT).show();
    }

    @OnClick(R.id.btn_send)
    public void onClickBtnSend(View view){
        showToast("click btn_send 我是计算机和"+view.getId());
    }

    @OnClick(R.id.btn_get)
    public void onClickBtnGet(View view){
        showToast("我是第二个函数是"+view.getId());
        //
        TwoActivity.actionStart(MainActivity.this);
    }


}

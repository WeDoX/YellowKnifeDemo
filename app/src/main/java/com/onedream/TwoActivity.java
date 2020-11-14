package com.onedream;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.onedream.yellowknife_annotation.UnBinder;
import com.onedream.yellowknife_annotation.YellowKnifeBindView;
import com.onedream.yellowknife_annotation.YellowKnifeClickView;
import com.onedream.yellowknife_api.YellowKnife;
import com.onedream.yellowknifedemo.R;

public class TwoActivity extends AppCompatActivity {
    @YellowKnifeBindView(viewId = R.id.tv_back)
    TextView tv_back;
    @YellowKnifeBindView(viewId = R.id.btn_send)
    Button btn_send;
    //
    UnBinder unBinder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_two);
        //
        unBinder = YellowKnife.bind(this);
        //
        tv_back.setText("点击我关闭当前界面");
    }

    @YellowKnifeClickView(viewId = R.id.tv_back)
    public void onClickBtnSend(View v) {
        finish();
    }


    public static void actionStart(Context context) {
        Intent intent = new Intent(context, TwoActivity.class);
        context.startActivity(intent);
    }
}

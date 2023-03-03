package com.onedream;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.onedream.yellowknife_annotation.Bind;
import com.onedream.yellowknife_annotation.OnClick;
import com.onedream.yellowknife_annotation.Route;
import com.onedream.yellowknife_annotation.UnBinder;
import com.onedream.yellowknife_api.YellowKnife;
import com.onedream.yellowknifedemo.R;

@Route("/me/two")
public class TwoActivity extends AppCompatActivity {
    @Bind(R.id.tv_back)
    TextView tv_back;
  /*  @Bind(R.id.btn_send)
    Button btn_send;*/
    //
    UnBinder unBinder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_two);
        //
        unBinder = YellowKnife.bind(this);
        //
        String name ="";
        if(null != getIntent()){
            name = getIntent().getStringExtra("name");
        }
        //
        tv_back.setText("点击我关闭当前界面:"+name);
    }

    @OnClick({R.id.tv_back, R.id.btn_send})
    public void onClickBtnSend(View v) {
        finish();
    }


    public static void actionStart(Context context) {
        Intent intent = new Intent(context, TwoActivity.class);
        context.startActivity(intent);
    }
}

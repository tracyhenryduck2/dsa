package com.henry.ecdemo.ui.phonemodel;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.henry.ecdemo.R;
import com.henry.ecdemo.common.CCPAppManager;
import com.henry.ecdemo.ui.ECSuperActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class PhoneRouterUI extends ECSuperActivity{




    @BindView(R.id.phone__register)
    public Button buRegister;

    @BindView(R.id.phone__found_pwd)
    public Button buFoundPwd;

    @BindView(R.id.phone__login)
    public Button buLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        getTopBarView().setTopBarToStatus(1, -1,
                -1, null,
                null,
                getString(R.string.app_name), null, null);

        CCPAppManager.addActivity(this);

        ButterKnife.bind(this);
    }






    @Override
    protected int getLayoutId(){
        return R.layout.activity_phone_router_ui;
    }


    @OnClick({R.id.phone__register, R.id.phone__login,R.id.phone__found_pwd})
    void butterknifeOnItemClick(View view) {
        switch (view.getId()) {
            case R.id.phone__register:

                startActivity(new Intent(this,PhoneRegisterUI.class));

                break;
            case R.id.phone__login:
                startActivity(new Intent(this,PhoneLoginUI.class));
                break;
            case R.id.phone__found_pwd:

                startActivity(new Intent(this,PhoneRegisterUI.class).putExtra("from","1"));
                break;

        }
    }

}

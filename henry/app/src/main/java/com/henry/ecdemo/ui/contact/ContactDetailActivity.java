package com.henry.ecdemo.ui.contact;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.henry.ecdemo.R;
import com.henry.ecdemo.common.CCPAppManager;
import com.henry.ecdemo.common.utils.ToastUtil;
import com.henry.ecdemo.storage.ContactSqlManager;
import com.henry.ecdemo.ui.ECSuperActivity;
import com.henry.ecdemo.ui.SDKCoreHelper;
import com.henry.ecdemo.ui.chatting.base.EmojiconTextView;

public class ContactDetailActivity extends ECSuperActivity implements View.OnClickListener{

    public final static String RAW_ID = "raw_id";
    public final static String MOBILE = "mobile";
    public final static String DISPLAY_NAME = "display_name";

    private ImageView mPhotoView;
    private EmojiconTextView mUsername;
    private TextView mNumber;

    private ECContacts mContacts;
    private String last_name;

    private View.OnClickListener onClickListener
            = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            if(mContacts == null) {
                return ;
            }
            CCPAppManager.startChattingAction(ContactDetailActivity.this, mContacts.getContactid(), mContacts.getNickname(), true);
            setResult(RESULT_OK);
            finish();
        }
    };

    @Override
    protected int getLayoutId() {
        return R.layout.layout_contact_detail;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initView();
        initActivityState(savedInstanceState);
        getTopBarView().setTopBarToStatus(1, R.drawable.topbar_back_bt, -1, R.string.contact_contactDetail, this);
    }


    /**
     * @param savedInstanceState
     */
    private void initActivityState(Bundle savedInstanceState) {
        long rawId = getIntent().getLongExtra(RAW_ID, -1);
        if(rawId == -1) {
            String mobile = getIntent().getStringExtra(MOBILE);
            String displayname = getIntent().getStringExtra(DISPLAY_NAME);
            mContacts = ContactSqlManager.getCacheContact(mobile);
            if(mContacts == null) {
                mContacts = new ECContacts(mobile);
                mContacts.setNickname(displayname);
            }
        }

        if(mContacts == null && rawId != -1) {
            mContacts = ContactSqlManager.getContact(rawId);
        }

        if(mContacts == null) {
            ToastUtil.showMessage(R.string.contact_none);
            finish();
            return ;
        }

        mPhotoView.setImageBitmap(ContactLogic.getPhoto(mContacts.getRemark()));

        if(Integer.parseInt(mContacts.getNickname())%2==1){
            last_name= "нёN★гу_xjj";
        }else{
            last_name = "钍钍钍钍";
        }

        mUsername.setText(TextUtils.isEmpty(mContacts.getNickname()) ?mContacts.getContactid() :last_name);
        mNumber.setText(mContacts.getContactid());
    }


    /**
     *
     */
    private void initView() {
        mPhotoView = (ImageView) findViewById(R.id.desc);
        mUsername = (EmojiconTextView) findViewById(R.id.contact_nameTv);
        mNumber = (TextView) findViewById(R.id.contact_numer);
        findViewById(R.id.entrance_chat).setOnClickListener(onClickListener);
        findViewById(R.id.entrance_voip).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mContacts == null) {
                    return ;
                }
                CCPAppManager.showCallMenu(ContactDetailActivity.this ,mContacts.getNickname() , mContacts.getContactid());
            }
        });
        
       if(!SDKCoreHelper.getInstance().isSupportMedia()){
    	   
    	   findViewById(R.id.entrance_voip).setVisibility(View.GONE);
       }
        
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mPhotoView != null) {
            mPhotoView.setImageDrawable(null);
        }
        onClickListener = null;
        mContacts = null;

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_left:
                hideSoftKeyboard();
                finish();
                break;

            default:
                break;
        }
    }
}

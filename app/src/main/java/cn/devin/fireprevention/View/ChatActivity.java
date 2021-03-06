package cn.devin.fireprevention.View;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.ref.WeakReference;
import java.util.Date;

import cn.devin.fireprevention.DetailContract;
import cn.devin.fireprevention.Presenter.MainService;
import cn.devin.fireprevention.Presenter.TCPPresenter;
import cn.devin.fireprevention.R;

/**
 * Created by Devin on 2015/12/27.
 * 聊天界面
 */

public class ChatActivity extends Activity implements View.OnClickListener, DetailContract.MainVi{
    //args references
    private Boolean isForeGround = false;
    private static final int CHATMSG = 0, LOGIN_FAIL = 1;


    //view
    TextView show_tv;
    EditText input_ed;
    Button send_b;

    // control Service by binder
    private MainService.TalkBinder talkBinder;
    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            //get the object of TalkBinder
            talkBinder = (MainService.TalkBinder) iBinder;
            // set ServDataChangeListener
            talkBinder.registMainViLis(ChatActivity.this);
        }
        @Override
        public void onServiceDisconnected(ComponentName componentName) {
        }
    };

    private MyHandler myHandler = new MyHandler(this);
    private static class MyHandler extends Handler{
        private final WeakReference<ChatActivity> weakReference;

        private MyHandler(ChatActivity chatActivity) {
            this.weakReference = new WeakReference<>(chatActivity);
        }
        @Override
        public void handleMessage(Message msg) {
            ChatActivity chatActivity = weakReference.get();
            if (chatActivity != null){
                switch (msg.what){
                    case CHATMSG:
                        String s = msg.obj.toString();
                        chatActivity.show_tv.append("\n "+ s);
                        break;
                    case LOGIN_FAIL:
                        Toast.makeText(chatActivity,"连接服务器失败，请检查网络",Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        break;
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        show_tv = findViewById(R.id.show_tv);
        input_ed = findViewById(R.id.input_et);
        send_b = findViewById(R.id.send_b);
        send_b.setOnClickListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        isForeGround = true;
        Intent bindIntent = new Intent(this, MainService.class);
        bindService(bindIntent, connection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onPause() {
        isForeGround = false;
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        isForeGround = false;
        unbindService(connection);
        super.onDestroy();
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.send_b:
                //task.sendToServer(input_ed.getText().toString());

                if (talkBinder!=null){
                    talkBinder.loginChat(input_ed.getText().toString(), "", 0);
                    input_ed.setText(null);
                }else {
                    Toast.makeText(this,"发送失败，请检查网络连接", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }


    @Override
    public void onTaskDescriChange(Date date, String sub, String describe) {

    }
    @Override
    public void onTeamNumChange(int num) {

    }

    @Override
    public void onTaskDescriFinish() {

    }

    @Override
    public void onSecurityChange(boolean safety) {

    }

    @Override
    public void onChatChange(String s) {
        if (isForeGround){
            Message msg = new Message();
            msg.what = CHATMSG;
            msg.obj = s;
            myHandler.handleMessage(msg);
        }
    }

    @Override
    public void onLogin(boolean isLogin) {
        if (isForeGround){
            if (!isLogin){
                Message msg = new Message();
                msg.what = LOGIN_FAIL;
                myHandler.handleMessage(msg);
            }
        }
    }

    public static void actionStart(Context context){
        Intent intent = new Intent(context, ChatActivity.class);
        context.startActivity(intent);
    }
}

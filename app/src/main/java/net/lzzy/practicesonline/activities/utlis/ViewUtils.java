package net.lzzy.practicesonline.activities.utlis;

import android.app.AlertDialog;
import android.content.Context;
import android.text.TextUtils;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;

import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.SearchView;

import net.lzzy.practicesonline.R;
import net.lzzy.practicesonline.activities.activities.SplashActivity;

/**
 * Created by lzzy_gxy on 2019/4/15.
 * Description:
 */
public class ViewUtils {
    private static AlertDialog dialog;

    public static void showProgress(Context context,String massage){
        if (dialog == null){
            View view = LayoutInflater.from(context).inflate(R.layout.dialog_progress,null);
            TextView tv = view.findViewById(R.id.dialog_progress_tv);
            tv.setText(massage);
            dialog = new AlertDialog.Builder(context).create();
            dialog.setView(view);
        }
        dialog.show();
    }
    public static void dismissProgress(){
        if (dialog != null && dialog.isShowing()){
            dialog.dismiss();
        }
    }

    public static void goSetting(Context context){
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_setting,null);
        Pair<String,String> url = AppUtils.loadServerSetting(context);
        EditText edtIp = view.findViewById(R.id.select_dialog_setting_edt_ip);
        edtIp.setText(url.first);
        EditText edtPort = view.findViewById(R.id.select_dialog_setting_edt_port);
        edtPort.setText(url.second);
        new AlertDialog.Builder(context)
                .setView(view)
                .setNegativeButton("取消",(dialog,which) -> gotoMain(context))
                .setPositiveButton("保存",(dialog,which) ->{
                    String ip = edtIp.getText().toString();
                    String port = edtPort.getText().toString();
                    if (TextUtils.isEmpty(ip) || TextUtils.isEmpty(port)){
                        Toast.makeText(context,"信息不完整",Toast.LENGTH_SHORT).show();
                        return;
                    }
                    AppUtils.saveServerSetting(ip,port,context);
                    gotoMain(context);
                }).show();

    }
    private static void gotoMain(Context context){
        if (context instanceof SplashActivity){
            ((SplashActivity) context).gotoMain();
        }
    }
    public abstract static class AbstractTouchListener implements View.OnTouchListener{
        @SuppressWarnings("ClickableViewAccessiblility")
        @Override
        public boolean onTouch(View v, MotionEvent event){
            return handleTouch(event);
        }
        public abstract boolean handleTouch(MotionEvent event);
    }

    public abstract static class AbstractQueryListener implements SearchView.OnQueryTextListener{

        @Override
        public boolean onQueryTextSubmit(String query) {
            return false;
        }
        @Override
        public boolean onQueryTextChange(String newText) {
            handleQuery(newText);
            return true;
        }



        public abstract void handleQuery(String kw);
    }
}

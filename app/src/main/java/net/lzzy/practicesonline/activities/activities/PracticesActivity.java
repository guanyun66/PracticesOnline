package net.lzzy.practicesonline.activities.activities;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.ImageView;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import net.lzzy.practicesonline.R;
import net.lzzy.practicesonline.activities.fragments.PracticesFragment;
import net.lzzy.practicesonline.activities.models.PracticeFactory;
import net.lzzy.practicesonline.activities.network.DetectWebService;
import net.lzzy.practicesonline.activities.utlis.AppUtils;
import net.lzzy.practicesonline.activities.utlis.ViewUtils;


/**
 * Created by lzzy_gxy on 2019/4/16.
 * Description:
 */
public class PracticesActivity extends BaseActivity implements PracticesFragment.OnPracticesSelectedListener{
    public static final String API_ID = "apiId";
    public static final String PRACTICE_ID = "practiceId";
    public static final String LOCAL_COUNT = "localCount";
    private ServiceConnection connection;
    private boolean refresh = false;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppUtils.addActivity(this);
        initView();
        if (getIntent() != null){
            refresh = getIntent().getBooleanExtra(DetectWebService.EXTRA_REFRESH,false);
        }
        connection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                DetectWebService.DetectWeBinder binder = (DetectWebService.DetectWeBinder) service;
                binder.detect();
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {

            }
        };
        int localCount = PracticeFactory.getInstance().get().size();
        Intent intent = new Intent(this, DetectWebService.class);
        intent.putExtra(LOCAL_COUNT,localCount);
        bindService(intent,connection,BIND_AUTO_CREATE);

    }
    private void initView(){
        SearchView search=findViewById(R.id.action_bar_title_search);
        search.setQueryHint("请输入关键词搜索");
        search.setOnQueryTextListener(new ViewUtils.AbstractQueryListener() {
            @Override
            public void handleQuery(String kw) {
                ((PracticesFragment)getFragment()).search(kw);
            }

            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }
        });
        androidx.appcompat.widget.SearchView.SearchAutoComplete auto=search.findViewById(R.id.search_src_text);
        auto.setHighlightColor(Color.WHITE);
        auto.setTextColor(Color.WHITE);
        ImageView icon=search.findViewById(R.id.search_button);
        ImageView icX=search.findViewById(R.id.search_close_btn);
        ImageView icG=search.findViewById(R.id.search_go_btn);
        icon.setColorFilter(Color.WHITE);
        icG.setColorFilter(Color.WHITE);
        icX.setColorFilter(Color.WHITE);

    }
    @Override
    protected void onResume(){
        super.onResume();
        if (refresh){
            ((PracticesFragment)getFragment()).starRefresh();
        }
    }
    @Override
    public void onBackPressed(){
        super.onBackPressed();
        new AlertDialog.Builder(this)
                .setMessage("退出应用吗？")
                .setPositiveButton("退出",(dialog,which)-> AppUtils.exit())
                .show();
    }

    @Override
    protected int getLayout() {
        return R.layout.activity_practices;
    }

    @Override
    protected int getContainerId() {
        return R.id.activity_practices_container;
    }

    @Override
    protected Fragment createFragment() {
        return new PracticesFragment();
    }

    @Override
    public void onPracticesSelected(String practiceId, int apiId) {
        Intent intent = new Intent(this,QuestionActivity.class );
        intent.putExtra(API_ID,apiId);
        intent.putExtra(PRACTICE_ID,practiceId);
        startActivity(intent);
    }
}


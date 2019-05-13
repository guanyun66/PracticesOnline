package net.lzzy.practicesonline.activities.activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.os.Parcelable;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import net.lzzy.practicesonline.R;
import net.lzzy.practicesonline.activities.fragments.QuestionFragment;
import net.lzzy.practicesonline.activities.models.Question;
import net.lzzy.practicesonline.activities.models.QuestionFactory;
import net.lzzy.practicesonline.activities.models.UserCookies;
import net.lzzy.practicesonline.activities.models.view.PracticeResult;
import net.lzzy.practicesonline.activities.models.view.QuestionResult;
import net.lzzy.practicesonline.activities.network.PracticeService;
import net.lzzy.practicesonline.activities.utlis.AbstractStaticHandler;
import net.lzzy.practicesonline.activities.utlis.AppUtils;
import net.lzzy.practicesonline.activities.utlis.ViewUtils;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class QuestionActivity extends AppCompatActivity {
    private String practiceId;
    private int apiId;
    private List<Question> questions;
    private TextView tvView;
    private TextView tvCommit;
    private TextView tvHint;
    private ViewPager pager;
    private boolean isCommitted = false;
    private FragmentStatePagerAdapter adapter;
    private View[] dots;
    private int pos;
    static final int WHAT_RESPONSE_CODE = 0;
    static final int WHAT_EXCEPTION = 1;
    static final String EXTRA_PRACTICE = "practiceId";
    static final String EXTRA_RESULT = "extraResult";
    static final int REQUEST_CODE_RESULT = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_question);
        AppUtils.addActivity(this);
        retrieveData();
        initViews();
        initDots();
        setListeners();
        pos = UserCookies.getInstance().getCurrentQuestion(practiceId);
        pager.setCurrentItem(pos);
        refreshDots(pos);
        UserCookies.getInstance().updateReadCount(questions.get(pos).getId().toString());

    }
    private void setListeners(){
        pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                refreshDots(position);
                UserCookies.getInstance().updateCurrentQuestion(practiceId,position);
                UserCookies.getInstance().updateReadCount(questions.get(position).getId().toString());

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        tvCommit.setOnClickListener(v ->commitPractice());
        tvView.setOnClickListener(v ->redirect());




    }
    String info;
    private void commitPractice(){
        List<QuestionResult> results = UserCookies.getInstance().getResultFromCookies(questions);
        List<String> macs = AppUtils.getMacAddress();
        String[] items = new String[macs.size()];
        macs.toArray(items);
        info = items[0];
        new AlertDialog.Builder(this)
                .setTitle("选择Mac地址")
                .setSingleChoiceItems(items,0,(dialog, which) -> info = items[which])
                .setNegativeButton("取消",null)
                .setPositiveButton("提交",(dialog, which) -> {
                    PracticeResult result = new PracticeResult(results,apiId,"关云:" + info);
                    postResult(result);
                }).show();
    }
    private static class ResultHandler extends AbstractStaticHandler<QuestionActivity>{

        static final int RESPONSE_OK_MIN = 200;
        static final int RESPONSE_OK_MAX = 220;

         ResultHandler(QuestionActivity context) {
            super(context);
        }

        @Override
        public void handleMessage(Message msg, QuestionActivity activity) {
             ViewUtils.dismissProgress();
             if (msg.what == WHAT_RESPONSE_CODE){
                 int code = (int) msg.obj;
                 if (code >= RESPONSE_OK_MIN && code<= RESPONSE_OK_MAX){
                     Toast.makeText(activity,"提交成功",Toast.LENGTH_SHORT).show();
                     UserCookies.getInstance().commitPractice(activity.practiceId);
                    activity.redirect();
                 }else {
                     Toast.makeText(activity,"提交失败，请重试",Toast.LENGTH_SHORT).show();
                 }
             }else if (msg.what == WHAT_EXCEPTION){
                 Toast.makeText(activity,"提交失败，请重试\n"+msg.obj,Toast.LENGTH_SHORT).show();
             }
        }
    }
    private ResultHandler handler = new ResultHandler(this);

    private void postResult(PracticeResult result) {
        ViewUtils.showProgress(this,"正在提交成绩……");
        AppUtils.getExecutor().execute(()->{
            try {
                int code = PracticeService.postResult(result);
                handler.sendMessage(handler.obtainMessage(WHAT_RESPONSE_CODE,code));
            } catch (JSONException | IOException e) {
                handler.sendMessage(handler.obtainMessage(WHAT_EXCEPTION,e.getMessage()));
            }
        });
    }

    private void redirect(){
        List<QuestionResult> results =UserCookies.getInstance().getResultFromCookies(questions);
        Intent intent=new Intent(this,ResultActivity.class);
        intent.putExtra(EXTRA_PRACTICE,practiceId);
        intent.putParcelableArrayListExtra(EXTRA_RESULT, (ArrayList<? extends Parcelable>) results);
        startActivityForResult(intent, REQUEST_CODE_RESULT);

    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //todo:返回查看数据（全部 or）
    }

    private void initDots(){
        int count = questions.size();
        dots = new View[count];
        LinearLayout container = findViewById(R.id.activity_question_dots);
        container.removeAllViews();
        int px = ViewUtils.dp2px(20,this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(px,px);
        px = ViewUtils.dp2px(5,this);
        params.setMargins(px,px,px,px);
        for (int i = 0; i < count;i++){
            TextView tvDot = new TextView(this);
            tvDot.setLayoutParams(params);
            tvDot.setBackgroundResource(R.drawable.dot_style);
            tvDot.setTag(i);
            tvDot.setOnClickListener(v->pager.setCurrentItem((Integer)v.getTag()));
            container.addView(tvDot);
            dots[i] = tvDot;
        }
    }
    private void refreshDots(int pos){
        for (int i = 0; i < dots.length;i++){
            int drawable = i == pos ? R.drawable.dot_fill_style : R.drawable.dot_style;
            dots[i].setBackgroundResource(drawable);
        }
    }
    private void initViews(){
        tvView = findViewById(R.id.activity_question_tv_view);
        tvCommit = findViewById(R.id.activity_question_tv_commit);
        tvHint= findViewById(R.id.activity_question_tv_hint);
        pager = findViewById(R.id.activity_question_pager);
        if (isCommitted){
            tvCommit.setVisibility(View.GONE);
            tvHint.setVisibility(View.VISIBLE);
            tvView.setVisibility(View.VISIBLE);
        }else {
            tvView.setVisibility(View.GONE);
            tvHint.setVisibility(View.VISIBLE);
            tvCommit.setVisibility(View.VISIBLE);
        }
        adapter = new FragmentStatePagerAdapter(getSupportFragmentManager()) {
            @Override
            public int getCount() {
                return questions.size();
            }

            @Override
            public Fragment getItem(int position) {
                Question question = questions.get(position);
                return QuestionFragment.newInstance(question.getId().toString(),position,isCommitted);
            }
        };
        pager.setAdapter(adapter);
    }

    private void retrieveData() {
        practiceId = getIntent().getStringExtra(PracticesActivity.PRACTICE_ID);
        apiId = getIntent().getIntExtra(PracticesActivity.API_ID,-1);
        questions = QuestionFactory.getInstance().getByPractice(practiceId);
        isCommitted = UserCookies.getInstance().isPracticeCommitted(practiceId);
        if (apiId < 0 || questions == null || questions.size() == 0){
            Toast.makeText(this,"no questions",Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        AppUtils.removeActivity(this);
    }
    @Override
    protected void onResume() {
        super.onResume();
        AppUtils.setRunning(getLocalClassName());
    }
    @Override
    protected void onStop() {
        super.onStop();
        AppUtils.setStopped(getLocalClassName());
    }
}

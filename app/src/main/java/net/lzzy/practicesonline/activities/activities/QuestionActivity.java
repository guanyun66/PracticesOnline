package net.lzzy.practicesonline.activities.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.os.Parcelable;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import net.lzzy.practicesonline.R;
import net.lzzy.practicesonline.activities.fragments.QuestionFragment;
import net.lzzy.practicesonline.activities.models.FavoriteFactory;
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


/**
 * @author Administrator
 */
public class QuestionActivity extends AppCompatActivity {
    private static final int POST_PRACTICE_DOWN = 0;
    public static final int POST_EXCEPTION = 1;
    public static final int RESPONSE_OK_MIN = 200;
    public static final int RESPONSE_OK_MAX = 220;
    public static final String EXTRA_PRACTICE_ID = "practiceId";
    public static final String EXTRA_RESULT = "extraResult";
    public static final int REQUEST_CODE_RESULT = 0;
    private String practiceId;
    private int apiId;
    private List<Question> questions;
    private TextView tvHint;
    private LinearLayout layoutDots;
    private TextView tvView;
    private TextView tvCommit;
    private ViewPager pager;
    private boolean isCommitted = false;
    private FragmentStatePagerAdapter adapter;
    private int pos;
    private View[] dots;

    private DownloadHandler handler = new DownloadHandler(this);

    private class DownloadHandler extends AbstractStaticHandler<QuestionActivity> {

        private DownloadHandler(QuestionActivity context) {
            super(context);
        }

        @Override
        public void handleMessage(Message msg, QuestionActivity activity) {
            ViewUtils.dismissProgress();
            if (msg.what == POST_PRACTICE_DOWN) {
                int code = (int) msg.obj;
                if (code >= RESPONSE_OK_MIN && code <= RESPONSE_OK_MAX) {
                    activity.isCommitted = true;
                    Toast.makeText(activity, "提交成功", Toast.LENGTH_SHORT).show();
                    UserCookies.getInstance().commitPractice(activity.practiceId);
                    activity.redirect();
                } else {
                    Toast.makeText(activity, "提交失败，请重试", Toast.LENGTH_SHORT).show();
                }
            } else if (msg.what == POST_EXCEPTION) {
                Toast.makeText(activity, "提交失败，请重试\n" + msg.obj, Toast.LENGTH_SHORT).show();
            }
            //region
           /* switch (msg.what) {
                case POST_PRACTICE_DOWN:
                     isCommitted=true;
                    Toast.makeText(activity,"提交成功",Toast.LENGTH_LONG).show();
                    break;
                case POST_EXCEPTION:
                    break;
                default:
                    break;

            }*/
            //endregion
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_question);
        AppUtils.addActivity(this);
        retrieveData();
        intiView();
        setListeners();
        intDost();
        pos = UserCookies.getInstance().getCurrentQuestion(practiceId);
        refreshDots(pos);
        pager.setCurrentItem(pos);
        UserCookies.getInstance().updateReadCount(questions.get(pos).getId().toString());
    }

    private void setListeners() {
        pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                //滚动时
            }

            @Override
            public void onPageSelected(int position) {
                //切换页面时
                refreshDots(position);
                UserCookies.getInstance().updateCurrentQuestion(practiceId, position);
                UserCookies.getInstance().updateReadCount(questions.get(position).getId().toString());
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
        tvCommit.setOnClickListener(v -> commitPractice());
        tvView.setOnClickListener(v -> redirect());
    }

    String info;

    private void commitPractice() {
        List<QuestionResult> results
                = UserCookies.getInstance().getResultFromCookies(questions);
        List<String> macs = AppUtils.getMacAddress();
        //转换为数组类型
        String[] items = new String[macs.size()];
        macs.toArray(items);

        info = items[0];
        new AlertDialog.Builder(this)
                .setTitle("选择Mac地址")
                /*选项单选*/
                .setSingleChoiceItems(items,
                        0, (dialog, which) -> info = items[which])
                .setNeutralButton("取消", null)
                .setPositiveButton("提交", (dialog, which) -> {
                    PracticeResult result = new PracticeResult(results, apiId, "曹兆荣" + info);
                    postResult(result);
                }).show();
    }

    private void postResult(PracticeResult result) {
        ViewUtils.showProgress(this, "正在提交成绩……");
        AppUtils.getExecutor().execute(() -> {
            try {
                int code = PracticeService.postResult(result);
                handler.sendMessage(handler.obtainMessage(POST_PRACTICE_DOWN, code));
            } catch (JSONException | IOException e) {
                handler.sendMessage(handler.obtainMessage(POST_EXCEPTION, e.getMessage()));
            }
        });
//region
        /*AppUtils.getExecutor().execute(() -> {
            try {
                int json=PracticeService.posRequest(result);
                if (json>= INT &&json<= INT1){
                    handler.sendEmptyMessage(POST_PRACTICE_DOWN);
                }else {
                    handler.sendEmptyMessage(POST_EXCEPTION);
                }
            } catch (JSONException|IOException e) {
                e.printStackTrace();
                handler.sendEmptyMessage(POST_EXCEPTION);
            }
        });*/
        //endregion
    }

    private void redirect() {
        List<QuestionResult> results = UserCookies.getInstance().getResultFromCookies(questions);
        Intent intent = new Intent(this, ResultActivity.class);
        intent.putExtra(EXTRA_PRACTICE_ID, practiceId);
        intent.putParcelableArrayListExtra(EXTRA_RESULT, (ArrayList<? extends Parcelable>) results);
        startActivityForResult(intent, REQUEST_CODE_RESULT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //todo:返回查看数据（全部 or）
        if (data != null) {
            if (data.getBooleanExtra(ResultActivity.COLLECT, false)) {
                FavoriteFactory factory = FavoriteFactory.getInstance();
                List<Question> questionList = new ArrayList<>();
                for (Question question : questions) {
                    if (factory.isQuestionStarred(question.getId().toString())) {
                        questionList.add(question);
                    }
                }
                questions.clear();
                questions.addAll(questionList);
                intDost();
                adapter.notifyDataSetChanged();
                if (questions.size()>0){
                    pager.setCurrentItem(0);
                    refreshDots(0);
                }

            }

            int pos = data.getIntExtra(ResultActivity.RESULT_POSITION, -1);
            pager.setCurrentItem(pos);
        }
    }

    private void intDost() {
        //底部导航点
        int count = questions.size();
        dots = new View[count];
        layoutDots = findViewById(R.id.activity_question_dots);
        layoutDots.removeAllViews();
        int px = ViewUtils.dp2px(16, this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(px, px);
        px = ViewUtils.dp2px(5, this);
        params.setMargins(px, px, px, px);
        for (int i = 0; i < count; i++) {
            TextView tvDost = new TextView(this);
            tvDost.setLayoutParams(params);
            tvDost.setBackgroundResource(R.drawable.dot_style);
            tvDost.setTag(i);
            //todo:tvDots添加点击监听
            tvDost.setOnClickListener(v -> pager.setCurrentItem((Integer) v.getTag()));
            layoutDots.addView(tvDost);
            dots[i] = tvDost;
        }
    }

    private void refreshDots(int pos) {
        for (int i = 0; i < dots.length; i++) {
            int drawable = i == pos ? R.drawable.dot_fill_style : R.drawable.dot_style;
            dots[i].setBackgroundResource(drawable);
        }
    }

    private void retrieveData() {
        //数据
        practiceId = getIntent().getStringExtra(PracticesActivity.PRACTICE_ID);
        apiId = getIntent().getIntExtra(PracticesActivity.API_ID, -1);
        questions = QuestionFactory.getInstance().getByPractice(practiceId);
        isCommitted = UserCookies.getInstance().isPracticeCommitted(practiceId);
        if (apiId < 0 || questions == null || questions.size() == 0) {
            Toast.makeText(this, "no questions", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void intiView() {
        tvHint = findViewById(R.id.activity_question_tv_hint);
        tvView = findViewById(R.id.activity_question_tv_view);
        tvCommit = findViewById(R.id.activity_question_tv_commit);
        pager = findViewById(R.id.activity_question_pager);
        if (isCommitted) {
            tvCommit.setVisibility(View.GONE);
            tvView.setVisibility(View.VISIBLE);
            tvHint.setVisibility(View.VISIBLE);
        } else {
            tvCommit.setVisibility(View.VISIBLE);
            tvView.setVisibility(View.GONE);
            tvHint.setVisibility(View.GONE);
        }

        //ViewPager 第一步首先要有数据 retrieveData()
        adapter = new FragmentStatePagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                //传数据需要静态工厂
                Question question = questions.get(position);
                return QuestionFragment.newInstance(question.getId().toString(), position, isCommitted);
            }

            @Override
            public int getCount() {
                return questions.size();
            }
        };
        pager.setAdapter(adapter);
    }

    //region 集中处理activity

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
    //endregion
}

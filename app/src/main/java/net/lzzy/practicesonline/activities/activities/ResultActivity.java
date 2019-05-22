package net.lzzy.practicesonline.activities.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import net.lzzy.practicesonline.R;

import net.lzzy.practicesonline.activities.fragments.ChartFragment;
import net.lzzy.practicesonline.activities.fragments.GridFragment;
import net.lzzy.practicesonline.activities.models.view.QuestionResult;


import java.util.List;

/**
 *
 * @author lzzy_gxy
 * @date 2019/5/13
 * Description:
 */
public class ResultActivity extends BaseActivity
        implements GridFragment.OnQuestionItemClickListener , ChartFragment.OnGoToGridListener {

    public static final int RESULT_OK = 0;
    public static final String RESULT_POSITION = "position";
    public static final String COLLECT = "collect";
    private List<QuestionResult> results;
    int positions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public int getLayout() {
        return R.layout.activity_result;
    }

    @Override
    public int getContainerId() {
        return R.id.activity_result_container;
    }

    @Override
    public Fragment createFragment() {
        results = getIntent().getParcelableArrayListExtra(QuestionActivity.EXTRA_RESULT);
        return GridFragment.newInstance(results);
    }

    @Override
    public void onQuestionItemClick(int position) {
        Intent intent=new Intent();
        intent.putExtra(RESULT_POSITION,position);
        setResult(RESULT_OK,intent);
        positions=position;
        finish();
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setMessage("返回到哪里？")
                .setPositiveButton("查看收藏",(dialog, which) -> viewStarred())
                .setNegativeButton("章节列表",(dialog, which) -> startActivity(new Intent(this,PracticesActivity.class)))
                .setNeutralButton("返回题目",(dialog, which) -> finish())
                .show();
    }

    private void viewStarred() {
        Intent intent=new Intent();
        intent.putExtra(COLLECT,true);
        setResult(RESULT_OK,intent);
        finish();
    }


    @Override
    public void onGoToChart() {
        getManager().beginTransaction().replace(R.id.activity_result_container, ChartFragment.newInstance(results)).commit();
    }

    @Override
    public void onGoToGrid() {
        getManager().beginTransaction().replace(R.id.activity_result_container,GridFragment.newInstance(results)).commit();
    }

}

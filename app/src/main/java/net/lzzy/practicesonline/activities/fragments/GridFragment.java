package net.lzzy.practicesonline.activities.fragments;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.widget.GridView;
import android.widget.TextView;

import androidx.annotation.Nullable;

import net.lzzy.practicesonline.R;
import net.lzzy.practicesonline.activities.models.view.QuestionResult;
import net.lzzy.sqllib.GenericAdapter;
import net.lzzy.sqllib.ViewHolder;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lzzy_gxy on 2019/5/13.
 * Description:
 */
public class GridFragment extends BaseFragment {
    public static final String ARG_PRACTICE_ID = "argPracticeId";
    public static final String ARG_QUESTION_RESULT = "argQuestionResult";
    private List<QuestionResult> results;

    private OnQuestionItemClickListener listener;

    public static GridFragment newInstance(List<QuestionResult> results) {
        //静态工厂方法
        GridFragment fragment = new GridFragment();
        Bundle args = new Bundle();
        args.putParcelableArrayList(ARG_QUESTION_RESULT, (ArrayList<? extends Parcelable>) results);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            //读静态工厂方法
            results = getArguments().getParcelableArrayList(ARG_QUESTION_RESULT);
        }
    }

    @Override
    protected void populate() {
        intiView();

    }

    private void intiView() {
        GridView gv = findViewById(R.id.fragment_grid_gv);
        TextView tv = findViewById(R.id.fragment_grid_tv_grid);
        GenericAdapter<QuestionResult> adapter = new
                GenericAdapter<QuestionResult>(getContext(), R.layout.view_grid, results) {

                    @Override
                    public void populate(ViewHolder holder, QuestionResult questionResult) {
                        TextView textView = holder.getView(R.id.view_grid_tv_circle);
                        if (questionResult.isRight()) {
                            textView.setBackgroundResource(R.drawable.green);
                        } else {
                            textView.setBackgroundResource(R.drawable.red);
                        }
                        //获取位置+1显示
                        // String ss=results.lastIndexOf(questionResult)+1+"";
                        String ss = getPosition(questionResult) + 1 + "";
                        textView.setText(ss);
                    }

                    @Override
                    public boolean persistInsert(QuestionResult questionResult) {
                        return false;
                    }

                    @Override
                    public boolean persistDelete(QuestionResult questionResult) {
                        return false;
                    }
                };
        gv.setAdapter(adapter);
        gv.setOnItemClickListener((parent, view, position, id) -> {
            if (listener != null) {
                listener.onQuestionItemClick(position);
            }
        });


        tv.setOnClickListener(v -> {
            listener.onGoToChart();
        });
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnQuestionItemClickListener) {
            listener = (OnQuestionItemClickListener) context;
        } else {
            throw new ClassCastException(context.toString() + "必需实现OnQuestionItemClickListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    public interface OnQuestionItemClickListener {
        /**
         * 点击题目跳转
         *
         * @param position 题目序号
         */
        void onQuestionItemClick(int position);

        void onGoToChart();
    }


    @Override
    public int getLayout() {
        return R.layout.fragment_grid;
    }

    @Override
    public void search(String kw) {

    }

}

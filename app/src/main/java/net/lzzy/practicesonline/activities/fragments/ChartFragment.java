package net.lzzy.practicesonline.activities.fragments;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.Nullable;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.BarLineChartBase;
import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.utils.MPPointF;
import net.lzzy.practicesonline.R;
import net.lzzy.practicesonline.activities.models.UserCookies;
import net.lzzy.practicesonline.activities.models.view.QuestionResult;
import net.lzzy.practicesonline.activities.models.view.WrongType;

import net.lzzy.practicesonline.activities.utlis.ViewUtils;
import java.util.ArrayList;
import java.util.List;


/**
 * @author lzzy_gxy
 * @date 2019/5/13
 * Description:
 */
public class ChartFragment extends BaseFragment {
    public static final String ARG_QUESTION_RESULT = "argQuestionResult";
    private static final String COLOR_GREEN = "#629755";
    private static final String COLOR_RED = "#D81B60";
    private static final String COLOR_PRIMARY = "#008577";
    private static final String COLOR_BROWN = "#00574B";
    public static final int MIN_DISTANCE = 50;
    private List<QuestionResult> results;
    private OnGoToGridListener listener;
    private static String[] HORIZONTAL_AXIS = new String[]{WrongType.RIGHT_OPTIONS.toString(), WrongType.MISS_OPTIONS.toString(),
            WrongType.WRONG_OPTIONS.toString(), WrongType.EXTRA_OPTIONS.toString()};
    int rightCount = 0;
    protected final String[] parties = new String[]{
            "正确", "少选",
    };
    private PieChart pChart;
    private LineChart lineChart;
    private BarChart barChart;
    private Chart[] charts;
    private float touchX1;
    private float touchX2;
    private int chartIndex = 0;
    private String[] titles = {"正确比例（单位%)", "题目阅读数统计", "题目错误类型统计"};
    private View[] dots;

    public static ChartFragment newInstance(List<QuestionResult> results) {
        //静态工厂方法
        ChartFragment fragment = new ChartFragment();
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
        for (QuestionResult result : results) {
            if (result.isRight()) {
                rightCount++;
            }
        }
    }
//region 对错

    public double getRight() {
        int rCount = 0;

        for (QuestionResult result : results) {
            if (result.isRight()) {
                rCount++;
            }
        }
        return rCount * 1.0 / results.size();
    }

    public double getError() {
        int errorCount = 0;
        for (QuestionResult result : results) {
            if (!result.isRight()) {
                errorCount++;
            }
        }
        return errorCount * 1.0 / results.size();
    }
//endregion

    @Override
    protected void populate() {
        intiView();
        intiChart();
        configPieChart();
        displayPieChart();
        configBarLineChart(barChart);
        displayLineChart();
        displayBarChart();
        View dot1 = findViewById(R.id.fragment_chart_dot1);
        View dot2 = findViewById(R.id.fragment_chart_dot2);
        View dot3 = findViewById(R.id.fragment_chart_dot3);
        dots = new View[]{dot1, dot2, dot3};
        findViewById(R.id.fragment_char_container).setOnTouchListener(new ViewUtils.AbstractTouchListener() {
            @Override
            public boolean handleTouch(MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    touchX1 = event.getX();
                }
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    touchX2 = event.getX();
                    if (Math.abs(touchX2 - touchX1) > MIN_DISTANCE) {
                        if (touchX2 < touchX1) {
                            if (chartIndex < charts.length - 1) {
                                chartIndex++;
                            } else {
                                chartIndex = 0;
                            }
                        } else {
                            if (chartIndex > 0) {
                                chartIndex--;
                            } else {
                                chartIndex = charts.length - 1;
                            }
                        }
                    }
                    switchChart();
                }
                return true;
            }

        });
    }

    //region 柱状图与折线图

    private void displayLineChart() {
        List<Entry> entries = new ArrayList<>();
        for (int i = 0; i < results.size(); i++) {
            entries.add(new Entry(i + 1, UserCookies.getInstance()
                    .getReadCount(results.get(i).getQuestionId().toString())));
        }
        LineDataSet dataSet = new LineDataSet(entries, "查看访问数量");
        LineData lineData = new LineData(dataSet);

        lineChart.setData(lineData);
        lineChart.invalidate();

        ValueFormatter xFormat = new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return " " + (int) value;
            }
        };
        lineChart.getXAxis().setValueFormatter(xFormat);


    }

    private void displayBarChart() {
        ValueFormatter xFormat = new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return WrongType.getInstance((int) value).toString();
            }
        };
        barChart.getXAxis().setValueFormatter(xFormat);
        int ok = 0, miss = 0, extra = 0, wrong = 0;
        for (QuestionResult result : results) {
            switch (result.getType()) {
                case WRONG_OPTIONS:
                    wrong++;
                    break;
                case EXTRA_OPTIONS:
                    extra++;
                    break;
                case MISS_OPTIONS:
                    miss++;
                    break;
                case RIGHT_OPTIONS:
                    ok++;
                    break;
                default:
                    break;
            }
        }
        List<BarEntry> entries=new ArrayList<>();
        entries.add(new BarEntry(0,ok));
        entries.add(new BarEntry(1,miss));
        entries.add(new BarEntry(2,extra));
        entries.add(new BarEntry(3,wrong));
        BarDataSet dataSet=new BarDataSet(entries,"查看类型");
        dataSet.setColors(Color.parseColor(COLOR_PRIMARY),Color.parseColor(COLOR_GREEN)
                ,Color.parseColor(COLOR_BROWN),Color.parseColor(COLOR_RED));
        ArrayList<IBarDataSet> dataSets=new ArrayList<>();
        dataSets.add(dataSet);
        BarData data=new BarData(dataSets);
        data.setBarWidth(0.8f);
        barChart.setData(data);
        barChart.invalidate();
    }

    private void configBarLineChart(BarLineChartBase charts) {

        XAxis xAxis = charts.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setTextSize(8f);
        xAxis.setGranularity(1f);
        YAxis yAxis = charts.getAxisLeft();
        yAxis.setLabelCount(8, true);
        yAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
        yAxis.setTextSize(8f);
        yAxis.setGranularity(1f);
        yAxis.setAxisMinimum(0);
        charts.getLegend().setEnabled(false);
        charts.getAxisRight().setEnabled(false);
        charts.setPinchZoom(false);

    }

    private void switchChart() {
        for (int i = 0; i < charts.length; i++) {
            if (chartIndex == i) {
                charts[i].setVisibility(View.VISIBLE);
                dots[i].setBackgroundResource(R.drawable.dot_fill_style);
            } else {
                charts[i].setVisibility(View.GONE);
                dots[i].setBackgroundResource(R.drawable.dot_style);
            }
        }
    }


    private void displayPieChart() {
        List<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(rightCount, "正确"));
        entries.add(new PieEntry(results.size() - rightCount, "错误"));
        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setDrawIcons(false);
        dataSet.setSliceSpace(3f);
        dataSet.setIconsOffset(new MPPointF(0, 40));
        dataSet.setSelectionShift(5f);
        List<Integer> colors = new ArrayList<>();
        colors.add(Color.parseColor("#4A92FC"));
        colors.add(Color.parseColor("#ee6e55"));
        dataSet.setColors(colors);
        PieData data = new PieData(dataSet);
        data.setValueFormatter(new PercentFormatter());
        data.setValueTextSize(11f);
        data.setValueTextColor(Color.BLACK);
        pChart.setData(data);
        pChart.invalidate();
        pChart.setVisibility(View.VISIBLE);

    }

    private void configPieChart() {
        pChart.setUsePercentValues(true);
        //设置空洞
        pChart.setDrawHoleEnabled(false);
        pChart.getLegend().setOrientation(Legend.LegendOrientation.HORIZONTAL);
        pChart.getLegend().setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        pChart.getLegend().setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);

    }

    private void getBarChartView() {

        int extra = 0, miss = 0, wrong = 0, right = 0;
        for (QuestionResult result : results) {
            switch (result.getType()) {
                case RIGHT_OPTIONS:
                    right++;
                    break;
                case EXTRA_OPTIONS:
                    extra++;
                    break;
                case MISS_OPTIONS:
                    miss++;
                    break;
                case WRONG_OPTIONS:
                    wrong++;
                    break;
                default:
                    break;
            }
        }
        float[] data = new float[]{right, miss, extra, wrong};
        float max = right;
        for (float f : data) {
            if (f > max) {
                max = f;
            }
        }

    }


    private void intiView() {
        TextView tv = findViewById(R.id.fragment_chart_tv_chart);
        tv.setOnClickListener(v -> listener.onGoToGrid());

    }

    private void intiChart() {
        pChart = findViewById(R.id.fragment_char_pie);
        lineChart = findViewById(R.id.fragment_char_line);
        barChart = findViewById(R.id.fragment_char_bar);
        charts = new Chart[]{pChart, lineChart, barChart};
        int i = 0;
        for (Chart chart : charts) {
            chart.setTouchEnabled(false);
            chart.setVisibility(View.GONE);
            //描述这个图表
            Description desc = new Description();
            desc.setText(titles[i++]);
            chart.setDescription(desc);
            //无数据试图文本
            chart.setNoDataText("O(∩_∩)O哈哈~");
            chart.setExtraOffsets(5, 10, 5, 25);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnGoToGridListener) {
            listener = (OnGoToGridListener) context;
        } else {
            throw new ClassCastException(context.toString() + "必需实现OnGoToGridListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }


    @Override
    public int getLayout() {
        return R.layout.fragment_chart;
    }

    @Override
    public void search(String kw) {

    }

    public interface OnGoToGridListener {
        /**
         * 点击题目跳转
         *
         * @param
         */
        void onGoToGrid();
    }

}

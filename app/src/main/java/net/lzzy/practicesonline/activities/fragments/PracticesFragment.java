package net.lzzy.practicesonline.activities.fragments;

import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Message;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import net.lzzy.practicesonline.R;
import net.lzzy.practicesonline.activities.models.Practice;
import net.lzzy.practicesonline.activities.models.PracticeFactory;
import net.lzzy.practicesonline.activities.models.Question;
import net.lzzy.practicesonline.activities.models.QuestionFactory;
import net.lzzy.practicesonline.activities.models.UserCookies;
import net.lzzy.practicesonline.activities.network.DetectWebService;
import net.lzzy.practicesonline.activities.network.PracticeService;
import net.lzzy.practicesonline.activities.network.QuestionService;
import net.lzzy.practicesonline.activities.utlis.AbstractStaticHandler;
import net.lzzy.practicesonline.activities.utlis.AppUtils;
import net.lzzy.practicesonline.activities.utlis.DateTimeUtils;
import net.lzzy.practicesonline.activities.utlis.ViewUtils;
import net.lzzy.sqllib.GenericAdapter;
import net.lzzy.sqllib.ViewHolder;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Created by lzzy_gxy on 2019/4/16.
 * Description:
 */
public class PracticesFragment extends BaseFragment {

    private ListView lv;
    private SwipeRefreshLayout swipe;
    private TextView tvHint;
    private TextView tvTime;
    private List<Practice> practices;
    private GenericAdapter<Practice> adapter;
    private PracticeFactory factory=PracticeFactory.getInstance();
    private ThreadPoolExecutor executor= AppUtils.getExecutor();
    private DownloadHandler handler=new DownloadHandler(this);
    private static final int WHAT_PRACTICE_DONE=0;
    private static final int EXCEPTION=1;
    private OnPracticesSelectedListener listener;



    private static class DownloadHandler extends AbstractStaticHandler<PracticesFragment> {

        protected DownloadHandler(PracticesFragment context) {
            super(context);
        }

        @Override
        public void handleMessage(Message msg, PracticesFragment practicesFragment) {
            switch (msg.what){
                case WHAT_PRACTICE_DONE:
                    practicesFragment.tvTime.setText(DateTimeUtils.DATE_TIME_FORMAT.format(new java.util.Date()));
                    UserCookies.getInstance().updateLastRefreshTime();
                    try {
                        List<Practice> practices= PracticeService.getPractices(msg.obj.toString());
                        for (Practice practice:practices){
                            practicesFragment.adapter.add(practice);
                        }
                        Toast.makeText(practicesFragment.getContext(), "同步完成", Toast.LENGTH_SHORT).show();
                        practicesFragment.finishRefresh();
                    }  catch (Exception e) {
                        e.printStackTrace();
                        practicesFragment.handlePracticeException(e.getMessage());
                    }

                    break;
                case EXCEPTION:
                    practicesFragment.handlePracticeException(msg.obj.toString());
                    break;
                default:
                    break;
            }

        }
    }

    static class PracticeDownloader extends AsyncTask<Void,Void,String>{

        WeakReference<PracticesFragment> fragment;
        PracticeDownloader(PracticesFragment fragment){
            this.fragment=new WeakReference<>(fragment);
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            PracticesFragment fragment=this.fragment.get();
            fragment.tvTime.setVisibility(View.VISIBLE);
            fragment.tvHint.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(Void... voids) {

            try {
                return PracticeService.getPracticesFromServer();
            } catch (IOException e) {
                return e.getMessage();
            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            PracticesFragment fragment=this.fragment.get();
            fragment.tvTime.setText(DateTimeUtils.DATE_TIME_FORMAT.format(new java.util.Date()));
            UserCookies.getInstance().updateLastRefreshTime();
            try {
                List<Practice> practices=PracticeService.getPractices(s);
                for (Practice practice:practices){
                    fragment.adapter.add(practice);
                }
                Toast.makeText(fragment.getContext(), "同步完成", Toast.LENGTH_SHORT).show();
                fragment.finishRefresh();
            }  catch (Exception e) {
                e.printStackTrace();
                fragment.handlePracticeException(e.getMessage());
            }

        }
    }

    private void downloadPracticesAsync(){
        new PracticeDownloader(this).execute();
    }

    private void handlePracticeException(String message) {
        finishRefresh();

    }

    private void finishRefresh() {
        swipe.setRefreshing(false);
        tvTime.setVisibility(View.GONE);
        tvHint.setVisibility(View.GONE);
        NotificationManager manager = (NotificationManager) getContext()
                .getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null){
            manager.cancel(DetectWebService.DETECT_ID);
        }
    }
    public void starRefresh(){
        swipe.setRefreshing(true);
        refreshListener.onRefresh();
    }

    @Override
    protected void populate() {
        initViews();
        loadPractices();
        initSwipe();
    }

    private SwipeRefreshLayout.OnRefreshListener refreshListener= this::downloadPracticesAsync;
    private void downloadPractices() {
        tvTime.setVisibility(View.VISIBLE);
        tvHint.setVisibility(View.VISIBLE);
        executor.execute(()->{


            try {
                String json= PracticeService.getPracticesFromServer();

                handler.sendMessage(handler.obtainMessage(WHAT_PRACTICE_DONE,json));
            } catch (IOException e) {
                e.printStackTrace();
                handler.sendMessage(handler.obtainMessage(1,e.getMessage()));
            }




        });

    }
    private void initSwipe() {
        swipe.setOnRefreshListener(refreshListener);
        lv.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                boolean isTop=view.getChildCount()==0||view.getChildAt(0).getTop()>=0;
                swipe.setEnabled(isTop);
            }
        });
    }

    private void loadPractices() {
        practices=factory.get();
        Collections.sort(practices, (o1, o2) -> o2.getDownloadDate().compareTo(o1.getDownloadDate()));
        adapter=new GenericAdapter<Practice>(getContext(),R.layout.item,practices) {
            @Override
            public void populate(ViewHolder viewHolder, Practice practice) {
                viewHolder.setTextView(R.id.practice_item_tv_name,practice.getName());
                TextView tvOutLines=viewHolder.getView(R.id.practice_item_btn_outlines);
                if (practice.isDownloaded()){
                    tvOutLines.setVisibility(View.VISIBLE);
                    tvOutLines.setOnClickListener(v -> new AlertDialog.Builder(getContext())
                            .setMessage(practice.getOutlines())
                            .show());
                }else {
                    tvOutLines.setVisibility(View.GONE);
                }
                Button btnDel=viewHolder.getView(R.id.practice_item_btn_del);
//                btnDel.setVisibility(View.GONE);
                btnDel.setOnClickListener(v -> new AlertDialog.Builder(getContext())
                        .setMessage("要删除该章节及目录吗？")
                        .setPositiveButton("删除",(dialog, which) -> {
                            isDeleting=false;
                            adapter.remove(practice);
                        })
                        .setNegativeButton("取消",null)
                        .show());
                int visible=isDeleting?View.VISIBLE:View.GONE;
                btnDel.setVisibility(visible);
                viewHolder.getConvertView().setOnTouchListener(new ViewUtils.AbstractTouchListener() {
                    @Override
                    public boolean handleTouch(MotionEvent event) {
                        slideToDelete(event,btnDel,practice);
                        return true;
                    }
                });
            }

            @Override
            public boolean persistInsert(Practice practice) {
                return factory.add(practice);
            }

            @Override
            public boolean persistDelete(Practice practice) {
                return factory.deletePracticeAndRelated(practice);
            }
        };
        lv.setAdapter(adapter);
    }

    private float touchX1;
    private static final float MIN_DISTANCE=100;
    private boolean isDeleting=false;
    private void slideToDelete(MotionEvent event, Button btnDel, Practice practice){
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                touchX1=event.getX();
                break;
            case MotionEvent.ACTION_UP:
                float touchX2=event.getX();
                if (touchX1-touchX2>MIN_DISTANCE){
                    if (!isDeleting){
                        btnDel.setVisibility(View.VISIBLE);
                        isDeleting=true;
                    }
                }else {
                    if (btnDel.isShown()){
                        btnDel.setVisibility(View.GONE);
                        isDeleting=false;
                    }else if (!isDeleting){
                        performItemClick(practice);
                    }
                }
                break;
            default:
                break;
        }
    }

    private void performItemClick(Practice practice) {
        if (practice.isDownloaded() && listener != null){
            listener.onPracticesSelected(practice.getId().toString(),practice.getApiId());
        }else {
            new AlertDialog.Builder(getContext())
                    .setMessage("下载该章节题目吗？")
                    .setPositiveButton("下载",(dialog, which) -> downloadQuestions(practice.getApiId()))
                    .setNegativeButton("取消",null)
                    .show();
        }
    }

    static class QuestionDownloader extends AsyncTask<Integer,Void,String>{

        WeakReference<PracticesFragment> fragment;
        int apiId=0;
        PracticeFactory factory=PracticeFactory.getInstance();
        QuestionFactory questionFactory=QuestionFactory.getInstance();


        QuestionDownloader(PracticesFragment fragment,int apiId){
            this.fragment=new WeakReference<>(fragment);
            this.apiId=apiId;

        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            ViewUtils.showProgress(fragment.get().getContext(),"开始下载题目……");
        }

        @Override
        protected String doInBackground(Integer... integers) {
            try{
                return QuestionService.getQuestionsOfPracticeFromServer(apiId);

            }catch (IOException e){
                return e.getMessage();
            }

        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            UUID uuid=factory.getPracticeId(apiId);
            try {
                List<Question> questions=QuestionService.getQuestions(s,uuid);
                factory.saveQuestions(questions,uuid);
                for (Practice practice:fragment.get().practices){
                    if (practice.getId().equals(uuid)){
                        practice.setDownloaded(true);
                    }
                }
                fragment.get().adapter.notifyDataSetChanged();

            } catch (Exception e) {
                e.printStackTrace();
            }



        }
    }
    private void downloadQuestions(int apiId){
        new QuestionDownloader(this,apiId).execute();
    }

    private void initViews(){
        lv = findViewById(R.id.fragment_practices_lv);
        TextView tvNone= findViewById(R.id.fragment_practices__tv_none);
        lv.setEmptyView(tvNone);
        swipe = findViewById(R.id.fragment_practices_swipe);
        tvHint=findViewById(R.id.fragment_practices_tv_hint);
        tvTime=findViewById(R.id.fragment_practices_tv_time);
        tvTime.setText(UserCookies.getInstance().getLastRefreshTime());
        tvHint.setVisibility(View.GONE);
        tvTime.setVisibility(View.GONE);
        findViewById(R.id.fragment_practices_lv).setOnTouchListener(new ViewUtils.AbstractTouchListener() {
            @Override
            public boolean handleTouch(MotionEvent event) {
                isDeleting=false;
                adapter.notifyDataSetChanged();

                return false;
            }
        });


    }
    @Override
    protected int getLayout() {
        return R.layout.fragment_practices;
    }
    @Override
    public void search(String kw) {
        practices.clear();
        if (kw.isEmpty()){
            practices.addAll(factory.get());
        }else {
            practices.addAll(factory.search(kw));
        }

        adapter.notifyDataSetChanged();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnPracticesSelectedListener){
            listener=(OnPracticesSelectedListener) context;
        }else {
            throw new ClassCastException(context.toString()+"必须实现OnPracticesSelectedListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener=null;
        handler.removeCallbacksAndMessages(null);
    }

    public interface OnPracticesSelectedListener{
        void onPracticesSelected(String practiceId,int apiId);
    }
}


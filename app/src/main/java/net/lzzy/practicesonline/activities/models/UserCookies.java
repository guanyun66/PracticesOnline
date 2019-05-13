package net.lzzy.practicesonline.activities.models;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.core.util.Pair;

import net.lzzy.practicesonline.activities.models.view.QuestionResult;
import net.lzzy.practicesonline.activities.models.view.WrongType;
import net.lzzy.practicesonline.activities.utlis.AppUtils;
import net.lzzy.practicesonline.activities.utlis.DateTimeUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * Created by lzzy_gxy on 2019/4/24.
 * Description:
 */
public class UserCookies {
    private static final String SPLITTER = ",";
    public static final String KEY_TIME = "keyTime";
    public static final int FLAG_COMMIT_N = 0;
    public static final int FLAG_COMMIT_Y = 1;
    private SharedPreferences spTime;
    private SharedPreferences spCommit;
    private SharedPreferences spPosition;
    private SharedPreferences spReadCount;
    private SharedPreferences spOption;
    private static final UserCookies INSTANCE = new UserCookies();

    private UserCookies() {
        spTime = AppUtils.getContext()
                .getSharedPreferences("refresh_time", Context.MODE_PRIVATE);
        spCommit = AppUtils.getContext()
                .getSharedPreferences("practice_commit", Context.MODE_PRIVATE);
        spPosition = AppUtils.getContext()
                .getSharedPreferences("question_position", Context.MODE_PRIVATE);
        spReadCount = AppUtils.getContext()
                .getSharedPreferences("question_ReadCount", Context.MODE_PRIVATE);
        spOption = AppUtils.getContext()
                .getSharedPreferences("question_SpOption", Context.MODE_PRIVATE);
    }

    public static UserCookies getInstance() {
        return INSTANCE;
    }

    public void updateLastRefreshTime() {
        String time = DateTimeUtils.DATE_TIME_FORMAT.format(new Date());
        spTime.edit().putString(KEY_TIME, time).apply();
    }

    public String getLastRefreshTime() {
        return spTime.getString(KEY_TIME, "");
    }

    public boolean isPracticeCommitted(String practiceId) {
        int result = spCommit.getInt(practiceId, FLAG_COMMIT_N);
        return result == FLAG_COMMIT_Y;
    }

    public void commitPractice(String practiceId) {
        spCommit.edit().putInt(practiceId, FLAG_COMMIT_Y).apply();
    }

    public void updateCurrentQuestion(String practiceId, int pos) {
        spPosition.edit().putInt(practiceId, pos).apply();
    }

    public int getCurrentQuestion(String practiceId) {
        return spPosition.getInt(practiceId, 0);
    }

    public int getReadCount(String questionId) {
        return spReadCount.getInt(questionId, 0);
    }

    public void updateReadCount(String questionId) {
        int count = getReadCount(questionId) + 1;
        spReadCount.edit().putInt(questionId, count).apply();
    }

    public void changeOptionState(Option option, boolean isChecked, boolean isMulti) {
        String ids = spOption.getString(option.getQuestionId().toString(), "");
        String id = option.getId().toString();
        if (isMulti) {
            if (isChecked && !ids.contains(id)) {
                ids = ids.concat(SPLITTER).concat(id);
            } else if (!isChecked && ids.contains(id)) {
                ids = ids.replace(id, "");
            }
        } else {
            if (isChecked) {
                ids = id;
            }
        }
        spOption.edit().putString(option.getQuestionId().toString(), trunkSplitter(ids)).apply();
    }

    private String trunkSplitter(String ids) {
        boolean isSplitterRepeat = true;
        String repeatSplitter = SPLITTER.concat(SPLITTER);
        while (isSplitterRepeat) {
            isSplitterRepeat = false;
            if (ids.contains(repeatSplitter)) {
                isSplitterRepeat = true;
                ids = ids.replace(repeatSplitter, SPLITTER);

            }
        }
        if (ids.endsWith(SPLITTER)) {
            ids = ids.substring(0, ids.length() - 1);
        }
        if (ids.startsWith(SPLITTER)) {
            ids = ids.substring(1);
        }
        return ids;
    }

    public boolean isOptionSelected(Option option) {
        String ids = spOption.getString(option.getQuestionId().toString(), "");
        return Objects.requireNonNull(ids).contains(option.getId().toString());
    }

    public List<QuestionResult> getResultFromCookies(List<Question> questions) {
        List<QuestionResult> results = new ArrayList<>();
        for (Question question : questions) {
            QuestionResult result = new QuestionResult();
            result.setQuestionId(question.getId());
            String checkedIds = spOption.getString(question.getId().toString(), "");
            result.setRight(isUserRight(checkedIds, question).first);
            result.setType(getWrongType(checkedIds, question).second);
            results.add(result);
        }
        return results;
    }

    private Pair<Boolean, WrongType> isUserRight(String checkedIds, Question question) {
        boolean miss = false, extra = false;
        for (Option option : question.getOptions()) {
            if (option.isAnswer()) {
                if (!checkedIds.contains(option.getId().toString())) {
                    miss = true;
                }
            } else {
                if (checkedIds.contains(option.getId().toString())) {
                    extra = true;
                }
            }
        }
        if (miss && extra) {
            return new Pair<>(false, WrongType.WRONG_OPTIONS);
        } else if (miss) {
            return new Pair<>(false, WrongType.MISS_OPTIONS);
        } else if (extra) {
            return new Pair<>(false, WrongType.EXTRA_OPTIONS);
        } else {
            return new Pair<>(true, WrongType.RIGHT_OPTIONS);
        }
    }

    private Pair<Boolean, WrongType> getWrongType(String checkedIds, Question question) {
        boolean miss = false, extra = false;
        for (Option option : question.getOptions()) {
            if (option.isAnswer()) {
                if (!checkedIds.contains(option.getId().toString())) {
                    miss = true;
                }
            } else {
                if (checkedIds.contains(option.getId().toString())) {
                    extra = true;
                }
            }

        }
        if (miss && extra) {
            return new Pair<>(false, WrongType.WRONG_OPTIONS);
        } else if (miss) {
            return new Pair<>(false, WrongType.MISS_OPTIONS);
        } else if (extra) {
            return new Pair<>(false, WrongType.EXTRA_OPTIONS);
        } else {
            return new Pair<>(true, WrongType.RIGHT_OPTIONS);
        }

    }
}

package net.lzzy.practicesonline.activities.models;

import net.lzzy.sqllib.Ignored;
import net.lzzy.sqllib.Sqlitable;

import java.util.List;
import java.util.UUID;

/**
 * Created by lzzy_gxy on 2019/4/16.
 * Description:
 */
public class Option extends BaseEntity implements Sqlitable {
    @Ignored
    public static final String COL_QUESTION_ID="questionId";
    private String content;
    private String label;
    private UUID questionId;
    private boolean isAnswer;
    private int apiId;
    @Ignored
    private List<Option> options;
    @Ignored
    private QuestionType type;

    public List<Option> getOptions() {
        return options;
    }

    public void setOptions(List<Option> options) {
        this.options = options;
    }

    public QuestionType getType() {
        return type;
    }

    public void setType(QuestionType type) {
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public UUID getQuestionId() {
        return questionId;
    }

    public void setQuestionId(UUID questionId) {
        this.questionId = questionId;
    }

    public boolean isAnswer() {
        return isAnswer;
    }

    public void setAnswer(boolean answer) {
        isAnswer = answer;
    }

    public int getApiId() {
        return apiId;
    }

    public void setApiId(int apiId) {
        this.apiId = apiId;
    }

    @Override
    public boolean needUpdate() {
        return false;
    }
}

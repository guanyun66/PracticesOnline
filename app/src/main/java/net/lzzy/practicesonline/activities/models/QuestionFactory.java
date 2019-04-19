package net.lzzy.practicesonline.activities.models;

import android.text.TextUtils;

import net.lzzy.practicesonline.activities.constants.DbConstants;
import net.lzzy.practicesonline.activities.utlis.AppUtils;
import net.lzzy.sqllib.SqlRepository;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lzzy_gxy on 2019/4/17.
 * Description:
 */
public class QuestionFactory {

        private static final QuestionFactory OUR_INSTANCE= new QuestionFactory();
        private SqlRepository<Question> repository;
        private SqlRepository<Option> optionRepository;

        public static QuestionFactory getInstance() {
            return OUR_INSTANCE;
        }

        private QuestionFactory() {
            repository=new SqlRepository<>(AppUtils.getContext(),Question.class, DbConstants.packager);
            optionRepository=new SqlRepository<>(AppUtils.getContext(),Option.class,DbConstants.packager);
        }

        public void completeQuestion(Question question) throws InstantiationException,IllegalAccessException{
            List<Option> options=optionRepository.getByKeyword(question.getId().toString(),
                    new String[]{Option.COL_QUESTION_ID},true);
            question.setOptions(options);
            question.setDbType(question.getDbType());
        }

        public Question getById(String questionId){
            try {
                Question question =repository.getById(questionId);
                completeQuestion(question);
                return question;
            } catch (InstantiationException|IllegalAccessException e) {
                e.printStackTrace();
                return null;
            }
        }

        public List<Question> getByPractice(String practiceId){
            try {
                List<Question> questions=repository.getByKeyword(practiceId,new String[]{Question.COL_PRACTICE_ID},true);
                for (Question question:questions){
                    completeQuestion(question);
                }
                return questions;
            } catch (IllegalAccessException|InstantiationException e) {
                e.printStackTrace();
            }

            return new ArrayList<>();
        }

        public void insert(Question question){
            String q=repository.getDeleteString(question);
            List<String> sqlAction=new ArrayList<>();
            for (Option option:question.getOptions()){
                sqlAction.add(optionRepository.getDeleteString(option));
            }
            sqlAction.add(q);
            repository.exeSqls(sqlAction);
        }

        public List<String> getDeleteString(Question question){
            List<String> sqlAction=new ArrayList<>();
            sqlAction.add(repository.getDeleteString(question));
            for (Option option:question.getOptions()){
                sqlAction.add(optionRepository.getDeleteString(option));
            }
            String f=FavoriteFactory.getInstance().getDeleteString(question.getId().toString());
            if (!TextUtils.isEmpty(f)){
                sqlAction.add(f);
            }
            return sqlAction;
        }
    }



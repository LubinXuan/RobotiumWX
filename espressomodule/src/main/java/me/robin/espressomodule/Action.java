package me.robin.espressomodule;

import org.json.JSONObject;

/**
 * Created by xuanlubin on 2017/4/20.
 */
public interface Action {

    boolean isUiRequired();

    void process(JSONObject taskDefine, Provider provider) throws Exception;
}

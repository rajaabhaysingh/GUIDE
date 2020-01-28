package com.test.mark;

import java.util.List;

public class HomepageModel {

    public static final int BANNER_SLIDER = 0;
    public static final int HORIZONTAL_PRODUCT_VIEW = 1;

    private int type;

    private List<HomepageModel> homepageModelList;

    public HomepageModel(int type, List<HomepageModel> homepageModelList) {
        this.type = type;
        this.homepageModelList = homepageModelList;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public List<HomepageModel> getHomepageModelList() {
        return homepageModelList;
    }

    public void setHomepageModelList(List<HomepageModel> homepageModelList) {
        this.homepageModelList = homepageModelList;
    }


}

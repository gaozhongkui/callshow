package com.epiphany.callshow.model;

import java.util.ArrayList;
import java.util.List;

public class PornHubVideoGroupInfo {
    private final List<PornHubVideoInfo> mData = new ArrayList<>();

    public void addItem(PornHubVideoInfo info) {
        mData.add(info);
    }

    public List<PornHubVideoInfo> getData() {
        return mData;
    }

}

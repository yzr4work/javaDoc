package com.yzr.speechmatching.service.impl;

import com.yzr.speechmatching.service.MatchingService;

import java.util.concurrent.ConcurrentHashMap;

public class MatchingServiceImpl implements MatchingService {
    //对列数据存储在Redis中
    private static ConcurrentHashMap<String,Integer> WOMEN_POOL = new ConcurrentHashMap<>();
    private static ConcurrentHashMap<String,Integer> MAN_POOL = new ConcurrentHashMap<>();
    private static ConcurrentHashMap<String,Integer> WOMEN_MATCHING_COLUMNS = new ConcurrentHashMap<>();
    private static ConcurrentHashMap<String,Integer> MAN_MATCHING_COLUMNS = new ConcurrentHashMap<>();

    /**
     * 加入女池
     *
     * @param uid
     * @return
     */
    @Override
    public boolean joinWomanPool (String uid) {
        return false;
    }

    /**
     * 加入男池
     *
     * @param uid
     * @return
     */
    @Override
    public boolean joinManPool (String uid) {
        return false;
    }

    /**
     * 移除自身
     *
     * @param uid
     * @param genre
     */
    @Override
    public void removeSelf (String uid, int genre) {
        if (genre == 0){
            WOMEN_MATCHING_COLUMNS.remove(uid);
            WOMEN_POOL.remove(uid);
        }else {

        }
    }

    /**
     * 与女池匹配
     *
     * @param uid
     * @return
     */
    @Override
    public String matchingWomenPool (String uid) {

        return null;
    }

    /**
     * 与男池匹配
     *
     * @param uid
     * @return
     */
    @Override
    public String matchingManPool (String uid) {
        return null;
    }
}

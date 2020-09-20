package com.yzr.speechmatching.service;

import org.springframework.stereotype.Service;

@Service
public interface MatchingService {
    /**
     * 加入女池
     * @param uid
     * @return
     */
    boolean joinWomanPool(String uid);

    /**
     * 加入男池
     * @param uid
     * @return
     */
    boolean joinManPool(String uid);

    /**
     * 移除自身
     * @param uid
     * @param genre
     */
    void removeSelf(String uid, int genre);

    /**
     * 与女池匹配
     * @param uid
     * @return
     */
    String matchingWomenPool(String uid);

    /**
     * 与男池匹配
     * @param uid
     * @return
     */
    String matchingManPool(String uid);
}

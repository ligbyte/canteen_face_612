package com.stkj.platebind.base.utils;

import com.stkj.platebind.base.model.FaceChooseItemEntity;

import java.util.Comparator;

public class UsernameComparator implements Comparator<FaceChooseItemEntity> {
    @Override
    public int compare(FaceChooseItemEntity o1, FaceChooseItemEntity o2) {
        return o1.getUsername().compareTo(o2.getUsername());
    }
}

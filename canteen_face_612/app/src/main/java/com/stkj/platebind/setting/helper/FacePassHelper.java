package com.stkj.platebind.setting.helper;

import android.app.Activity;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;

import com.alibaba.fastjson.JSON;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.FutureTarget;
import com.stkj.platebind.base.greendao.AppGreenDaoOpenHelper;
import com.stkj.platebind.base.greendao.GreenDBConstants;
import com.stkj.platebind.base.greendao.generate.DaoMaster;
import com.stkj.platebind.base.greendao.generate.DaoSession;
import com.stkj.platebind.base.greendao.generate.FacePassPeopleInfoDao;
import com.stkj.platebind.base.model.BaseNetResponse;
import com.stkj.platebind.base.net.ParamsUtils;
import com.stkj.platebind.pay.model.LoadingDialogEvent;
import com.stkj.platebind.setting.data.FacePassDateBaseMMKV;
import com.stkj.platebind.setting.model.AddLocalFacePassInfoWrapper;
import com.stkj.platebind.setting.model.FacePassPeopleInfo;
import com.stkj.platebind.setting.model.FacePassPeopleListInfo;
import com.stkj.platebind.setting.model.SearchFacePassPeopleParams;
import com.stkj.platebind.setting.service.SettingService;
import com.stkj.cbgfacepass.CBGFacePassHandlerHelper;
import com.stkj.common.core.ActivityHolderFactory;
import com.stkj.common.core.ActivityWeakRefHolder;
import com.stkj.common.core.AppManager;
import com.stkj.common.glide.GlideApp;
import com.stkj.common.log.LogHelper;
import com.stkj.common.net.retrofit.RetrofitManager;
import com.stkj.common.rx.AutoDisposeUtils;
import com.stkj.common.rx.DefaultObserver;
import com.stkj.common.rx.RxTransformerUtils;
import com.stkj.common.ui.toast.ToastUtils;
import com.stkj.common.utils.TimeUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.query.Query;
import org.greenrobot.greendao.query.QueryBuilder;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableEmitter;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;
import io.reactivex.rxjava3.core.ObservableSource;
import io.reactivex.rxjava3.functions.Function;
import mcv.facepass.types.FacePassAddFaceResult;
import retrofit2.Call;
import retrofit2.Response;

/**
 * 人脸库帮助类
 */
public class FacePassHelper extends ActivityWeakRefHolder {

    public final static String TAG = "FacePassHelper";
    private static final int PAGE_SIZE = 15;
    private DaoSession daoSession;
    private Database database;
    private boolean isRequestFacePass;
    private boolean isDeleteAllFacePass;
    private boolean isDeleteSingleFacePass;
    private Set<OnFacePassListener> facePassListenerHashSet = new HashSet<>();

    public FacePassHelper(@NonNull Activity activity) {
        super(activity);
        AppGreenDaoOpenHelper daoOpenHelper = new AppGreenDaoOpenHelper(AppManager.INSTANCE.getApplication(), GreenDBConstants.FACE_DB_NAME, null);
        database = daoOpenHelper.getWritableDb();
        DaoMaster daoMaster = new DaoMaster(database);
        daoSession = daoMaster.newSession();
    }

    public void addOnFacePassListener(OnFacePassListener onFacePassListener) {
        if (onFacePassListener != null) {
            facePassListenerHashSet.add(onFacePassListener);
        }
    }

    public void removeOnFacePassListener(OnFacePassListener onFacePassListener) {
        if (onFacePassListener != null) {
            facePassListenerHashSet.remove(onFacePassListener);
        }
    }

    /**
     * 查询本地人脸库单个人脸
     */
    public void searchFacePassByPhone(String phone, OnHandlePhoneListener handlePhoneListener) {
        Activity activityWithCheck = getHolderActivityWithCheck();
        if (activityWithCheck == null) {
            return;
        }
        Observable.create(new ObservableOnSubscribe<FacePassPeopleInfo>() {
                    @Override
                    public void subscribe(@io.reactivex.rxjava3.annotations.NonNull ObservableEmitter<FacePassPeopleInfo> emitter) throws Throwable {
                        try {
                            FacePassPeopleInfoDao facePassPeopleInfoDao = daoSession.getFacePassPeopleInfoDao();
                            FacePassPeopleInfo localPeopleInfo =
                                    facePassPeopleInfoDao.queryBuilder()
                                            .where(FacePassPeopleInfoDao.Properties.Phone.eq(phone))
                                            .unique();
                            if (localPeopleInfo != null) {
                                emitter.onNext(localPeopleInfo);
                            } else {
                                emitter.onError(new Exception("无搜索结果"));
                            }
                        } catch (Throwable e) {
                            e.printStackTrace();
                            emitter.onError(e);
                        }
                        emitter.onComplete();
                    }
                }).compose(RxTransformerUtils.mainSchedulers())
                .to(AutoDisposeUtils.onDestroyDispose((LifecycleOwner) activityWithCheck))
                .subscribe(new DefaultObserver<FacePassPeopleInfo>() {
                    @Override
                    protected void onSuccess(FacePassPeopleInfo l) {
                        if (handlePhoneListener != null) {
                            handlePhoneListener.onHandleLocalPhone(phone, l);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (handlePhoneListener != null) {
                            handlePhoneListener.onHandleLocalPhoneError(phone);
                        }
                    }
                });
    }


    /**
     * 查询本地人脸库单个人脸
     */
    public void searchFacePassByCardNumber(String cardNumber, OnHandleCardNumberListener cardNumberListener) {
        Activity activityWithCheck = getHolderActivityWithCheck();
        if (activityWithCheck == null) {
            return;
        }
        Observable.create(new ObservableOnSubscribe<FacePassPeopleInfo>() {
                    @Override
                    public void subscribe(@io.reactivex.rxjava3.annotations.NonNull ObservableEmitter<FacePassPeopleInfo> emitter) throws Throwable {
                        try {
                            FacePassPeopleInfoDao facePassPeopleInfoDao = daoSession.getFacePassPeopleInfoDao();
                            FacePassPeopleInfo localPeopleInfo =
                                    facePassPeopleInfoDao.queryBuilder()
                                            .where(FacePassPeopleInfoDao.Properties.Card_Number.eq(cardNumber))
                                            .unique();
                            if (localPeopleInfo != null) {
                                emitter.onNext(localPeopleInfo);
                            } else {
                                emitter.onError(new Exception("无搜索结果"));
                            }
                        } catch (Throwable e) {
                            e.printStackTrace();
                            emitter.onError(e);
                        }
                        emitter.onComplete();
                    }
                }).compose(RxTransformerUtils.mainSchedulers())
                .to(AutoDisposeUtils.onDestroyDispose((LifecycleOwner) activityWithCheck))
                .subscribe(new DefaultObserver<FacePassPeopleInfo>() {
                    @Override
                    protected void onSuccess(FacePassPeopleInfo l) {
                        if (cardNumberListener != null) {
                            cardNumberListener.onHandleLocalCardNumber(cardNumber, l);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (cardNumberListener != null) {
                            cardNumberListener.onHandleLocalCardNumberError(cardNumber);
                        }
                    }
                });
    }

    /**
     * 查询本地人脸库单个人脸
     */
    public void searchFacePassByFaceToken(String faceToken, OnHandleFaceTokenListener faceTokenListener) {
        Activity activityWithCheck = getHolderActivityWithCheck();
        if (activityWithCheck == null) {
            return;
        }
        Observable.create(new ObservableOnSubscribe<FacePassPeopleInfo>() {
                    @Override
                    public void subscribe(@io.reactivex.rxjava3.annotations.NonNull ObservableEmitter<FacePassPeopleInfo> emitter) throws Throwable {
                        try {
                            FacePassPeopleInfoDao facePassPeopleInfoDao = daoSession.getFacePassPeopleInfoDao();
                            FacePassPeopleInfo localPeopleInfo =
                                    facePassPeopleInfoDao.queryBuilder()
                                            .where(FacePassPeopleInfoDao.Properties.CBGFaceToken.eq(faceToken))
                                            .unique();
                            if (localPeopleInfo != null) {
                                emitter.onNext(localPeopleInfo);
                            } else {
                                emitter.onError(new Exception("无搜索结果"));
                            }
                        } catch (Throwable e) {
                            e.printStackTrace();
                            emitter.onError(e);
                        }
                        emitter.onComplete();
                    }
                }).compose(RxTransformerUtils.mainSchedulers())
                .to(AutoDisposeUtils.onDestroyDispose((LifecycleOwner) activityWithCheck))
                .subscribe(new DefaultObserver<FacePassPeopleInfo>() {
                    @Override
                    protected void onSuccess(FacePassPeopleInfo l) {
                        if (faceTokenListener != null) {
                            faceTokenListener.onHandleLocalFace(faceToken, l);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (faceTokenListener != null) {
                            faceTokenListener.onHandleLocalFaceError(faceToken);
                        }
                    }
                });
    }

    /**
     * 删除单个人脸（用于测试）
     */
    public void deleteFacePass(FacePassPeopleInfo passPeopleInfo, OnDeleteLocalFaceListener onDeleteLocalFaceListener) {
        if (isDeleteSingleFacePass) {
            return;
        }
        Activity activityWithCheck = getHolderActivityWithCheck();
        if (activityWithCheck == null) {
            return;
        }
        isDeleteSingleFacePass = true;
        Observable.create(new ObservableOnSubscribe<Integer>() {
                    @Override
                    public void subscribe(@io.reactivex.rxjava3.annotations.NonNull ObservableEmitter<Integer> emitter) throws Throwable {
                        try {
                            FacePassPeopleInfoDao facePassPeopleInfoDao = daoSession.getFacePassPeopleInfoDao();
                            FacePassPeopleInfo localPeopleInfo =
                                    facePassPeopleInfoDao.queryBuilder()
                                            .where(FacePassPeopleInfoDao.Properties.Unique_number.eq(passPeopleInfo.getUnique_number()))
                                            .unique();
                            if (localPeopleInfo != null) {
                                facePassPeopleInfoDao.delete(localPeopleInfo);
                                String faceToken = localPeopleInfo.getCBGFaceToken();
                                if (!TextUtils.isEmpty(faceToken)) {
                                    CBGFacePassHandlerHelper cbgFacePassHandHelper = ActivityHolderFactory.get(CBGFacePassHandlerHelper.class, activityWithCheck);
                                    if (cbgFacePassHandHelper != null) {
                                        cbgFacePassHandHelper.deleteFace(faceToken.getBytes(StandardCharsets.ISO_8859_1));
                                    }
                                }
                            }
                            emitter.onNext(10000);
                        } catch (Throwable e) {
                            e.printStackTrace();
                            emitter.onError(e);
                        }
                        emitter.onComplete();
                    }
                }).compose(RxTransformerUtils.mainSchedulers())
                .to(AutoDisposeUtils.onDestroyDispose((LifecycleOwner) activityWithCheck))
                .subscribe(new DefaultObserver<Integer>() {
                    @Override
                    protected void onSuccess(Integer l) {
                        isDeleteSingleFacePass = false;
                        if (onDeleteLocalFaceListener != null) {
                            onDeleteLocalFaceListener.onDeleteLocalFace(passPeopleInfo);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        isDeleteSingleFacePass = false;
                        if (onDeleteLocalFaceListener != null) {
                            onDeleteLocalFaceListener.onDeleteError(passPeopleInfo, e.getMessage());
                        }
                    }
                });
    }

    /**
     * 查询本地数据库face pass
     */
    public void queryLocalFacePass(SearchFacePassPeopleParams passPeopleParams, OnQueryLocalFacePassListener queryLocalFacePassListener) {
        Activity activityWithCheck = getHolderActivityWithCheck();
        if (activityWithCheck == null) {
            return;
        }
        Observable.create(new ObservableOnSubscribe<List<FacePassPeopleInfo>>() {
                    @Override
                    public void subscribe(@io.reactivex.rxjava3.annotations.NonNull ObservableEmitter<List<FacePassPeopleInfo>> emitter) throws Throwable {
                        try {
                            String accountType = passPeopleParams.getAccountType();
                            String department = passPeopleParams.getDepartment();
                            String keyword = passPeopleParams.getSearchKey();
                            int offset = passPeopleParams.getRequestOffset();
                            QueryBuilder<FacePassPeopleInfo> queryBuilder = daoSession.getFacePassPeopleInfoDao().queryBuilder()
                                    .offset(offset)
                                    .limit(PAGE_SIZE);
                            Query<FacePassPeopleInfo> query;
                            if (!TextUtils.isEmpty(keyword)) {
                                String likeKeyword = "%" + keyword + "%";
                                if (TextUtils.equals(accountType, "全部人员") && TextUtils.equals(department, "全部部门")) {
                                    query = queryBuilder
                                            .where(queryBuilder.or(FacePassPeopleInfoDao.Properties.Full_Name.like(likeKeyword)
//                                ,FacePassPeopleInfoDao.Properties.CardNumber.like(likeKeyword)
                                                    , FacePassPeopleInfoDao.Properties.Phone.like(likeKeyword)))
                                            .build();
                                } else if (TextUtils.equals(accountType, "全部人员") && !TextUtils.equals(department, "全部部门")) {
                                    query = queryBuilder
                                            .where(FacePassPeopleInfoDao.Properties.DepNameType.eq(department)
                                                    , queryBuilder.or(FacePassPeopleInfoDao.Properties.Full_Name.like(likeKeyword)
//                                        ,FacePassPeopleInfoDao.Properties.CardNumber.like(likeKeyword)
                                                            , FacePassPeopleInfoDao.Properties.Phone.like(likeKeyword)))
                                            .build();
                                } else if (TextUtils.equals(department, "全部部门")) {
                                    query = queryBuilder
                                            .where(FacePassPeopleInfoDao.Properties.AccountType.eq(accountType)
                                                    , queryBuilder.or(FacePassPeopleInfoDao.Properties.Full_Name.like(likeKeyword)
//                                        ,FacePassPeopleInfoDao.Properties.CardNumber.like(likeKeyword)
                                                            , FacePassPeopleInfoDao.Properties.Phone.like(likeKeyword)))
                                            .build();
                                } else {
                                    query = queryBuilder
                                            .where(FacePassPeopleInfoDao.Properties.AccountType.eq(accountType),
                                                    FacePassPeopleInfoDao.Properties.DepNameType.eq(department)
                                                    , queryBuilder.or(FacePassPeopleInfoDao.Properties.Full_Name.like(likeKeyword)
//                                        ,FacePassPeopleInfoDao.Properties.CardNumber.like(likeKeyword)
                                                            , FacePassPeopleInfoDao.Properties.Phone.like(likeKeyword)))
                                            .build();
                                }
                            } else {
                                if (TextUtils.equals(accountType, "全部人员") && TextUtils.equals(department, "全部部门")) {
                                    query = queryBuilder
                                            .build();
                                } else if (TextUtils.equals(accountType, "全部人员") && !TextUtils.equals(department, "全部部门")) {
                                    query = queryBuilder
                                            .where(FacePassPeopleInfoDao.Properties.DepNameType.eq(department))
                                            .build();
                                } else if (TextUtils.equals(department, "全部部门")) {
                                    query = queryBuilder
                                            .where(FacePassPeopleInfoDao.Properties.AccountType.eq(accountType))
                                            .build();
                                } else {
                                    query = queryBuilder
                                            .where(FacePassPeopleInfoDao.Properties.AccountType.eq(accountType),
                                                    FacePassPeopleInfoDao.Properties.DepNameType.eq(department))
                                            .build();
                                }
                            }
                            List<FacePassPeopleInfo> passPeopleInfoList = query.list();
                            emitter.onNext(passPeopleInfoList);
                        } catch (Throwable e) {
                            e.printStackTrace();
                            emitter.onError(e);
                        }
                    }
                }).compose(RxTransformerUtils.mainSchedulers())
                .to(AutoDisposeUtils.onDestroyDispose((LifecycleOwner) activityWithCheck))
                .subscribe(new DefaultObserver<List<FacePassPeopleInfo>>() {
                    @Override
                    protected void onSuccess(List<FacePassPeopleInfo> facePassPeopleInfoList) {
                        if (queryLocalFacePassListener != null) {
                            queryLocalFacePassListener.onQueryLocalFacePassSuccess(facePassPeopleInfoList);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (queryLocalFacePassListener != null) {
                            queryLocalFacePassListener.onQueryLocalFacePassError(e.getMessage());
                        }
                    }
                });
    }

    /**
     * 请求全量人脸库
     */
    public void requestAllFacePass() {
        requestFacePass(0, false);
    }

    /**
     * 请求人脸库
     */
    public void requestFacePass(int inferior_type, boolean ignoreRequestFacePass) {
        Log.d(TAG, "limerequestFacePass: " + 401);
        if (!ignoreRequestFacePass) {
            if (isRequestFacePass) {
                return;
            }
        }
        Log.d(TAG, "limerequestFacePass: " + 405);
        Activity activityWithCheck = getHolderActivityWithCheck();
        if (activityWithCheck == null) {
            return;
        }
        isRequestFacePass = true;
        for (OnFacePassListener onFacePassListener : facePassListenerHashSet) {
            onFacePassListener.onLoadFacePassGroupStart();
        }
        TreeMap<String, String> paramsMap = ParamsUtils.newSortParamsMapWithMode("KeyBoardCompanyMember");
        paramsMap.put("inferior_type", String.valueOf(inferior_type));
        RetrofitManager.INSTANCE.getDefaultRetrofit()
                .create(SettingService.class)
                .getAllFacePass(ParamsUtils.signSortParamsMap(paramsMap))
                .compose(RxTransformerUtils.mainSchedulers())
                .to(AutoDisposeUtils.onDestroyDispose((LifecycleOwner) activityWithCheck))
                .subscribe(new DefaultObserver<BaseNetResponse<FacePassPeopleListInfo>>() {
                    @Override
                    protected void onSuccess(BaseNetResponse<FacePassPeopleListInfo> baseNetResponse) {
                        Log.d(TAG, "limerequestFacePass=======: " + JSON.toJSONString(baseNetResponse));
                        FacePassPeopleListInfo responseData = baseNetResponse.getData();
                        if (responseData != null && responseData.getResults() != null && !responseData.getResults().isEmpty()) {
                            List<FacePassPeopleInfo> passPeopleInfoList = responseData.getResults();
                            addFacePassToLocal(passPeopleInfoList);
                            for (OnFacePassListener onFacePassListener : facePassListenerHashSet) {
                                onFacePassListener.onLoadFacePassGroupEnd(passPeopleInfoList, "请求成功", false);
                            }
                        } else {
                            callbackFinishFacePass("请求数据为空", false);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        callbackFinishFacePass(e.getMessage(), true);
                        EventBus.getDefault()
                                .post(new LoadingDialogEvent("下载失败","FAIL"));
                        Log.e(TAG, "limerequestFacePass: " + e.getMessage());
                    }
                });
    }

    /**
     * 添加人脸数据到本地
     */
    private void addFacePassToLocal(List<FacePassPeopleInfo> facePassPeopleInfoList) {
        Activity activityWithCheck = getHolderActivityWithCheck();
        if (activityWithCheck == null) {
            return;
        }
        List<AddLocalFacePassInfoWrapper> passInfoWrapperList = new ArrayList<>();
        int size = facePassPeopleInfoList.size();
        String addFacePassToLocalTime = TimeUtils.getNowString();
        LogHelper.print("-facePassHelper-addFacePassToLocal--start--" + size + "time " + addFacePassToLocalTime);
        for (int i = 0; i < size; i++) {
            AddLocalFacePassInfoWrapper facePassInfoWrapper = new AddLocalFacePassInfoWrapper(facePassPeopleInfoList.get(i));
            facePassInfoWrapper.setCurrentIndex(i + 1);
            facePassInfoWrapper.setTotalSize(size);
            passInfoWrapperList.add(facePassInfoWrapper);
        }
        Observable.fromIterable(passInfoWrapperList)
                .concatMap(new Function<AddLocalFacePassInfoWrapper, ObservableSource<AddLocalFacePassInfoWrapper>>() {
                    @Override
                    public ObservableSource<AddLocalFacePassInfoWrapper> apply(AddLocalFacePassInfoWrapper facePassInfoWrapper) throws Throwable {
                        FacePassPeopleInfo facePassItemInfo = facePassInfoWrapper.getFacePassPeopleInfo();
                        Log.d(TAG, "limefacePassItemInfo: " + JSON.toJSONString(facePassItemInfo));
                        String localAddFaceToken = null;
                        String cardNumber = facePassItemInfo.getCard_Number();
                        try {
                            //判断卡状态
                            if (facePassItemInfo.getCard_state() == 64) {
                                LogHelper.print("limeaddFacePassToLocal -facePassHelper-handleFacePassInfo--cardNumber: " + cardNumber + " Card_state() == 64");
                                //移除本地人脸item 信息
                                FacePassPeopleInfoDao facePassPeopleInfoDao = daoSession.getFacePassPeopleInfoDao();
                                FacePassPeopleInfo localPeopleInfo =
                                        facePassPeopleInfoDao.queryBuilder()
                                                .where(FacePassPeopleInfoDao.Properties.Unique_number.eq(facePassItemInfo.getUnique_number()))
                                                .unique();
                                if (localPeopleInfo != null) {
                                    LogHelper.print("limeaddFacePassToLocal -facePassHelper-handleFacePassInfo--cardNumber: " + cardNumber + " Card_state() == 64 delete local face");
                                    //删除移除本地人脸状态为64
                                    facePassPeopleInfoDao.delete(localPeopleInfo);
                                    //获取本地数据库数量
                                    long count = facePassPeopleInfoDao.count();
                                    facePassInfoWrapper.setLocalDatabaseCount(count);
                                    String cbgFaceToken = localPeopleInfo.getCBGFaceToken();
                                    if (!TextUtils.isEmpty(cbgFaceToken)) {
                                        CBGFacePassHandlerHelper cbgFacePassHandHelper = ActivityHolderFactory.get(CBGFacePassHandlerHelper.class, activityWithCheck);
                                        if (cbgFacePassHandHelper != null) {
                                            cbgFacePassHandHelper.deleteFace(cbgFaceToken.getBytes(StandardCharsets.ISO_8859_1));
                                        }
                                    }
                                    runUIThreadWithCheck(new Runnable() {
                                        @Override
                                        public void run() {
                                            for (OnFacePassListener onFacePassListener : facePassListenerHashSet) {
                                                onFacePassListener.onDeleteSingleFacePace(facePassItemInfo, facePassInfoWrapper.getCurrentIndex(), facePassInfoWrapper.getTotalSize());
                                            }
                                        }
                                    });
                                    facePassInfoWrapper.setStatus(AddLocalFacePassInfoWrapper.STATE_FORBID);
                                } else {
                                    LogHelper.print("limeaddFacePassToLocal -facePassHelper-handleFacePassInfo--cardNumber: " + cardNumber + " Card_state() == 64 no local face");
                                    facePassInfoWrapper.setStatus(AddLocalFacePassInfoWrapper.STATE_ERROR);
                                }
                                facePassInfoWrapper.setStatusMsg("卡状态为64");
                                //卡状态为64 请求facePassCallback
                                Response<BaseNetResponse<String>> netResponseResponse = getFacePassCallback(facePassItemInfo).execute();
                                return Observable.just(facePassInfoWrapper);
                            }
                            //加入本地人脸
                            CBGFacePassHandlerHelper cbgFacePassHandHelper = ActivityHolderFactory.get(CBGFacePassHandlerHelper.class, activityWithCheck);
                            //绑定本地人脸数据库成功
                            boolean bindFaceGroupSuccess = false;
                            int bindFaceGroupResult = -1;
                            if (cbgFacePassHandHelper != null) {
                                String imageData = facePassItemInfo.getImgData();
                                LogHelper.print("limeaddFacePassToLocal -facePassHelper-handleFacePassInfo--cardNumber: " + cardNumber + " imageData: " + imageData);
                                Bitmap bitmap = null;
                                try {
                                    FutureTarget<Bitmap> futureTarget = GlideApp.with(AppManager.INSTANCE.getApplication())
                                            .asBitmap()
                                            .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                                            .load(imageData)
                                            .submit();
                                    bitmap = futureTarget.get();
                                } catch (Throwable e) {
                                    LogHelper.print("limeaddFacePassToLocal -facePassHelper-handleFacePassInfo--cardNumber: " + cardNumber + " load imageData error " + imageData);
                                    e.printStackTrace();
                                }
                                FacePassAddFaceResult addFaceResult = cbgFacePassHandHelper.addFace(bitmap);
                                if (addFaceResult != null) {
                                    LogHelper.print("limeaddFacePassToLocal -facePassHelper-handleFacePassInfo--cardNumber: " + cardNumber + " addFaceResult: " + addFaceResult.result);
                                    /**
                                     * 0：成功
                                     * 1：没有检测到人脸
                                     * 2：检测到人脸，但是没有通过质量判断
                                     */
                                    bindFaceGroupResult = addFaceResult.result;
                                    if (bindFaceGroupResult == 0) {
                                        Log.d(TAG, "limebindFaceGroupResult =========: " + 0);
                                        localAddFaceToken = new String(addFaceResult.faceToken, StandardCharsets.ISO_8859_1);
                                        //添加人脸成功
                                        facePassItemInfo.setCBGCheckFaceResult(20);
                                        facePassItemInfo.setCBGFaceToken(localAddFaceToken);
                                        //绑定到本地人脸库
                                        bindFaceGroupSuccess = cbgFacePassHandHelper.bindFaceGroup(addFaceResult.faceToken);
                                    } else if (bindFaceGroupResult == 1) {
                                        Log.i(TAG, "limebindFaceGroupResult =========: " + 1);
                                        facePassItemInfo.setCBGCheckFaceResult(3);
                                    } else if (bindFaceGroupResult == 2) {
                                        Log.w(TAG, "limebindFaceGroupResult =========: " + 2);
                                        facePassItemInfo.setCBGCheckFaceResult(4);
                                    } else {
                                        Log.w(TAG, "limebindFaceGroupResult =========: " + "?");
                                        facePassItemInfo.setCBGCheckFaceResult(5);
                                    }
                                } else {
                                    //图片加载失败
                                    facePassItemInfo.setCBGCheckFaceResult(-99);
                                }
                            }
                            //请求facePassCallback
                            Response<BaseNetResponse<String>> netResponseResponse = getFacePassCallback(facePassItemInfo).execute();
                            if (netResponseResponse.isSuccessful()) {
                                BaseNetResponse<String> baseNetResponse = netResponseResponse.body();
                                if (baseNetResponse != null && TextUtils.equals(baseNetResponse.getCode(), "10000")) {
                                    String uniqueNumber = facePassItemInfo.getUnique_number();
                                    //插入本地数据库
                                    FacePassPeopleInfoDao facePassPeopleInfoDao = daoSession.getFacePassPeopleInfoDao();
                                    FacePassPeopleInfo localPeopleInfo =
                                            facePassPeopleInfoDao.queryBuilder()
                                                    .where(FacePassPeopleInfoDao.Properties.Unique_number.eq(uniqueNumber))
                                                    .unique();
                                    //存在数据，去更新数据
                                    facePassItemInfo.setOpening_date(TimeUtils.getNowString());
                                    if (localPeopleInfo == null) {
                                        LogHelper.print("-facePassHelper-handleFacePassInfo--cardNumber: " + cardNumber + " insertLocalFace success");
                                        facePassPeopleInfoDao.insert(facePassItemInfo);
                                    } else {
                                        LogHelper.print("-facePassHelper-handleFacePassInfo--cardNumber: " + cardNumber + " insertLocalFace has exits update");
                                        //删除移除本地人脸
                                        String cbgFaceToken = localPeopleInfo.getCBGFaceToken();
                                        if (!TextUtils.isEmpty(cbgFaceToken)) {
                                            if (cbgFacePassHandHelper != null) {
                                                cbgFacePassHandHelper.deleteFace(cbgFaceToken.getBytes(StandardCharsets.ISO_8859_1));
                                            }
                                        }
                                        //设置之前id,更新当前的数据
                                        facePassItemInfo.setId(localPeopleInfo.getId());
                                        facePassPeopleInfoDao.update(facePassItemInfo);
                                    }
                                    //本地保存一下部门
                                    Set<String> departmentList = FacePassDateBaseMMKV.getDepartmentList();
                                    int lastDepartmentListSize = departmentList.size();
                                    departmentList.add(facePassItemInfo.getDepNameType());
                                    if (departmentList.size() > lastDepartmentListSize) {
                                        FacePassDateBaseMMKV.putDepartmentList(departmentList);
                                    }
                                    //本地保存一下账户类别
                                    Set<String> accountTypeList = FacePassDateBaseMMKV.getAccountTypeList();
                                    int lastAccountTypeListSize = accountTypeList.size();
                                    accountTypeList.add(facePassItemInfo.getAccountType());
                                    if (accountTypeList.size() > lastAccountTypeListSize) {
                                        FacePassDateBaseMMKV.putAccountTypeList(accountTypeList);
                                    }
                                    //获取本地数据库数量
                                    long count = facePassPeopleInfoDao.count();
                                    facePassInfoWrapper.setLocalDatabaseCount(count);
                                    facePassInfoWrapper.setStatus(AddLocalFacePassInfoWrapper.STATE_SUCCESS);
                                    facePassInfoWrapper.setStatusMsg("添加成功");
                                } else {
                                    LogHelper.print("-facePassHelper-handleFacePassInfo--cardNumber: " + cardNumber + " face callback net error " + baseNetResponse.getCode());
                                    //删除已经添加的人脸库，如果网络请求失败的话
                                    if (cbgFacePassHandHelper != null && !TextUtils.isEmpty(localAddFaceToken)) {
                                        cbgFacePassHandHelper.deleteFace(localAddFaceToken.getBytes(StandardCharsets.ISO_8859_1));
                                    }
                                    facePassInfoWrapper.setStatus(AddLocalFacePassInfoWrapper.STATE_ERROR);
                                    facePassInfoWrapper.setStatusMsg("请求人脸库回调接口失败");
                                }
                            } else {
                                LogHelper.print("-facePassHelper-handleFacePassInfo--cardNumber: " + cardNumber + " face callback net error body empty");
                                //删除已经添加的人脸库，如果网络请求失败的话
                                if (cbgFacePassHandHelper != null && !TextUtils.isEmpty(localAddFaceToken)) {
                                    cbgFacePassHandHelper.deleteFace(localAddFaceToken.getBytes(StandardCharsets.ISO_8859_1));
                                }
                                facePassInfoWrapper.setStatus(AddLocalFacePassInfoWrapper.STATE_ERROR);
                                facePassInfoWrapper.setStatusMsg("请求人脸库回调接口失败");
                            }
                        } catch (Throwable e) {
                            e.printStackTrace();
                            Log.w(TAG, "limebindFaceGroupResult =========: " + e.getMessage());
                            LogHelper.print("-facePassHelper-handleFacePassInfo--cardNumber: " + cardNumber + " try catch error: " + e.getMessage());
                            //删除已经添加的人脸库，如果网络请求失败的话
                            CBGFacePassHandlerHelper cbgFacePassHandHelper = ActivityHolderFactory.get(CBGFacePassHandlerHelper.class, activityWithCheck);
                            if (cbgFacePassHandHelper != null && !TextUtils.isEmpty(localAddFaceToken)) {
                                cbgFacePassHandHelper.deleteFace(localAddFaceToken.getBytes(StandardCharsets.ISO_8859_1));
                            }
                            //出现异常 请求facePassCallback
                            Response<BaseNetResponse<String>> netResponseResponse = getFacePassCallback(facePassInfoWrapper.getFacePassPeopleInfo()).execute();
                            facePassInfoWrapper.setStatus(AddLocalFacePassInfoWrapper.STATE_ERROR);
                            facePassInfoWrapper.setStatusMsg("绑定本地人脸库失败:" + e.getMessage());
                        }
                        return Observable.just(facePassInfoWrapper);
                    }
                })
                .compose(RxTransformerUtils.mainSchedulers())
                .to(AutoDisposeUtils.onDestroyDispose((LifecycleOwner) activityWithCheck))
                .subscribe(new DefaultObserver<AddLocalFacePassInfoWrapper>() {
                    @Override
                    protected void onSuccess(AddLocalFacePassInfoWrapper addLocalFacePassInfoWrapper) {
                        for (OnFacePassListener onFacePassListener : facePassListenerHashSet) {
                            int status = addLocalFacePassInfoWrapper.getStatus();
                            if (status == AddLocalFacePassInfoWrapper.STATE_SUCCESS) {
                                onFacePassListener.onLoadSingleFacePaceSuccess(addLocalFacePassInfoWrapper, addLocalFacePassInfoWrapper.getCurrentIndex(), addLocalFacePassInfoWrapper.getTotalSize());
                            } else {
                                onFacePassListener.onLoadSingleFacePaceError(addLocalFacePassInfoWrapper, addLocalFacePassInfoWrapper.getStatusMsg(), addLocalFacePassInfoWrapper.getCurrentIndex(), addLocalFacePassInfoWrapper.getTotalSize());
                            }
                            if (status == AddLocalFacePassInfoWrapper.STATE_SUCCESS || status == AddLocalFacePassInfoWrapper.STATE_FORBID) {
                                onFacePassListener.onGetFacePassLocalCount(addLocalFacePassInfoWrapper.getLocalDatabaseCount());
                            }
                        }
                        //加载成功继续请求下发(除非请求数据为空)
                        if (addLocalFacePassInfoWrapper.isEndIndex()) {
                            LogHelper.print("-facePassHelper-addFacePassToLocal--end--" + size + "time " + addFacePassToLocalTime);
                            requestFacePass(1, true);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        for (OnFacePassListener onFacePassListener : facePassListenerHashSet) {
                            onFacePassListener.onAddFacePassToLocalError(e.getMessage());
                        }
                        callbackFinishFacePass(e.getMessage(), true);
                        EventBus.getDefault()
                                .post(new LoadingDialogEvent("添加人脸失败","FAIL"));
                    }
                });
    }

    private void callbackFinishFacePass(String msg, boolean isError) {
        for (OnFacePassListener onFacePassListener : facePassListenerHashSet) {
            EventBus.getDefault()
                    .post(new LoadingDialogEvent("同步成功","SUCCESS"));
//            ToastUtils.toastMsgSuccess("人脸库更新完毕");
            onFacePassListener.onLoadFacePassGroupEnd(null, msg, isError);
        }
        isRequestFacePass = false;
    }

    /**
     * 人脸库回调
     */
    private Call<BaseNetResponse<String>> getFacePassCallback(FacePassPeopleInfo facePassItemInfo) {
        TreeMap<String, String> paramsMap = ParamsUtils.newSortParamsMapWithMode("DownFaceFail");
        paramsMap.put("customerId", facePassItemInfo.getUnique_number());
        paramsMap.put("errorType", String.valueOf(facePassItemInfo.getCBGCheckFaceResult()));
        paramsMap.put("isFinish", "0");
        return RetrofitManager.INSTANCE.getDefaultRetrofit().create(SettingService.class).syncFacePassCallback(ParamsUtils.signSortParamsMap(paramsMap));
    }

    /**
     * 获取本地入库人脸
     */
    public void getFacePassLocalCount() {
        Activity activityWithCheck = getHolderActivityWithCheck();
        if (activityWithCheck == null) {
            return;
        }
        Observable.create(new ObservableOnSubscribe<Long>() {
                    @Override
                    public void subscribe(@io.reactivex.rxjava3.annotations.NonNull ObservableEmitter<Long> emitter) throws Throwable {
                        try {
                            long count = daoSession.getFacePassPeopleInfoDao().count();
                            emitter.onNext(count);
                        } catch (Throwable e) {
                            e.printStackTrace();
                            emitter.onError(e);
                        }
                        emitter.onComplete();
                    }
                }).compose(RxTransformerUtils.mainSchedulers())
                .to(AutoDisposeUtils.onDestroyDispose((LifecycleOwner) activityWithCheck))
                .subscribe(new DefaultObserver<Long>() {
                    @Override
                    protected void onSuccess(Long l) {
                        for (OnFacePassListener onFacePassListener : facePassListenerHashSet) {
                            onFacePassListener.onGetFacePassLocalCount(l);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        for (OnFacePassListener onFacePassListener : facePassListenerHashSet) {
                            onFacePassListener.onGetFacePassLocalCountError(e.getMessage());
                        }
                    }
                });
    }

    public void deleteAllFaceGroup() {
        deleteAllFaceGroup(null, false);
    }

    public void deleteAllFaceGroup(OnDeleteAllFaceListener deleteAllFaceListener) {
        deleteAllFaceGroup(deleteAllFaceListener, false);
    }

    public void deleteAllFaceGroup(boolean needRequestAllFace) {
        deleteAllFaceGroup(null, needRequestAllFace);
    }

    /**
     * 删除人脸库
     */
    public void deleteAllFaceGroup(OnDeleteAllFaceListener deleteAllFaceListener, boolean needRequestAllFace) {
        if (isDeleteAllFacePass) {
            return;
        }
        Activity activityWithCheck = getHolderActivityWithCheck();
        if (activityWithCheck == null) {
            return;
        }
        isDeleteAllFacePass = true;
        Observable.create(new ObservableOnSubscribe<Integer>() {
                    @Override
                    public void subscribe(@io.reactivex.rxjava3.annotations.NonNull ObservableEmitter<Integer> emitter) throws Throwable {
                        try {
                            FacePassPeopleInfoDao facePassPeopleInfoDao = daoSession.getFacePassPeopleInfoDao();
                            facePassPeopleInfoDao.deleteAll();
                            CBGFacePassHandlerHelper cbgFacePassHandlerHelper = ActivityHolderFactory.get(CBGFacePassHandlerHelper.class, activityWithCheck);
                            if (cbgFacePassHandlerHelper != null) {
                                cbgFacePassHandlerHelper.deleteAllFace();
                            }
                            FacePassDateBaseMMKV.removeAccountTypeList();
                            FacePassDateBaseMMKV.removeDepartmentList();
                            emitter.onNext(10000);
                        } catch (Throwable e) {
                            e.printStackTrace();
                            emitter.onError(e);
                        }
                        emitter.onComplete();
                    }
                }).compose(RxTransformerUtils.mainSchedulers())
                .to(AutoDisposeUtils.onDestroyDispose((LifecycleOwner) activityWithCheck))
                .subscribe(new DefaultObserver<Integer>() {
                    @Override
                    protected void onSuccess(Integer integer) {
                        isDeleteAllFacePass = false;
                        //ToastUtils.toastMsgSuccess("删除人脸库成功");
                        if (needRequestAllFace) {
                            requestAllFacePass();
                        }
                        for (OnFacePassListener onFacePassListener : facePassListenerHashSet) {
                            onFacePassListener.onDeleteAllFacePassSuccess(needRequestAllFace);
                        }
                        if (deleteAllFaceListener != null) {
                            deleteAllFaceListener.onDeleteAllFace();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        isDeleteAllFacePass = false;
                        ToastUtils.toastMsgError("删除人脸库失败");
                        EventBus.getDefault()
                                .post(new LoadingDialogEvent( "下载失败","FAIL"));
                        if (needRequestAllFace) {
                            requestAllFacePass();
                        }
                        for (OnFacePassListener onFacePassListener : facePassListenerHashSet) {
                            onFacePassListener.onDeleteAllFacePassError(needRequestAllFace, e.getMessage());
                        }
                        if (deleteAllFaceListener != null) {
                            deleteAllFaceListener.onDeleteAllFaceError(e.getMessage());
                        }
                    }
                });
    }

    /**
     * 是否正在删除或添加本地人脸数据库
     */
    public boolean isAddOrDeleteLocalFacePass() {
        LogHelper.print("-facePassHelper--isAddOrDeleteLocalFacePass isRequestFacePass = " + isRequestFacePass + " isDeleteAllFacePass = " + isDeleteAllFacePass + " isDeleteSingleFacePass = " + isDeleteSingleFacePass);
        return isRequestFacePass || isDeleteAllFacePass || isDeleteSingleFacePass;
    }

    @Override
    public void onClear() {
        facePassListenerHashSet.clear();
        daoSession.clear();
        database.close();
    }

    public interface OnFacePassListener {
        //加载单个人脸成功(插入本地数据库和单个人脸回调)
        default void onLoadSingleFacePaceSuccess(AddLocalFacePassInfoWrapper passPeopleInfo, int currentIndex, int totalCount) {
            LogHelper.print("-facePassHelper--onLoadSingleFacePaceSuccess--passPeopleInfo:" + passPeopleInfo.getFacePassPeopleInfo().getPhone() + " totalCount:" + totalCount + " currentIndex:" + currentIndex);
        }

        //加载单个人脸失败
        default void onLoadSingleFacePaceError(AddLocalFacePassInfoWrapper passPeopleInfo, String msg, int currentIndex, int totalCount) {
            LogHelper.print("-facePassHelper--onLoadSingleFacePaceError--passPeopleInfo:" + passPeopleInfo.getFacePassPeopleInfo().getPhone() + " totalCount:" + totalCount + " currentIndex:" + currentIndex + " msg:" + msg);
        }

        default void onDeleteSingleFacePace(FacePassPeopleInfo passPeopleInfo, int currentIndex, int totalCount) {
            LogHelper.print("-facePassHelper--onDeleteSingleFacePace--passPeopleInfo:" + passPeopleInfo.getPhone() + " totalCount:" + totalCount + " currentIndex:" + currentIndex);
        }

        //开始加载一次人脸接口
        default void onLoadFacePassGroupStart() {
            LogHelper.print("-facePassHelper--onLoadFacePassGroupStart--requestTime:" + TimeUtils.getNowString());
        }

        //加载一次人脸接口成功
        default void onLoadFacePassGroupEnd(List<FacePassPeopleInfo> facePassPeopleInfoList, String msg, boolean isError) {
            LogHelper.print("-facePassHelper--onLoadFacePassGroupEnd--requestTime:" + TimeUtils.getNowString() + " facePassPeopleInfoList:" + (facePassPeopleInfoList == null ? "0" : facePassPeopleInfoList.size()) + " msg:" + msg + " isError:" + isError);
        }

        default void onAddFacePassToLocalError(String msg) {
            LogHelper.print("-facePassHelper--onAddFacePassToLocalError--msg:" + msg);
        }

        default void onDeleteAllFacePassSuccess(boolean needRequestAllFace) {
            LogHelper.print("-facePassHelper--onDeleteAllFacePassSuccess--deleteTime:" + TimeUtils.getNowString());
        }

        default void onDeleteAllFacePassError(boolean needRequestAllFace, String msg) {
            LogHelper.print("-facePassHelper--onDeleteAllFacePassError--deleteTime:" + TimeUtils.getNowString() + " msg:" + msg);
        }

        default void onGetFacePassLocalCount(long totalCount) {
            LogHelper.print("-facePassHelper--onGetFacePassLocalCount-totalCount:" + totalCount);
        }

        default void onGetFacePassLocalCountError(String msg) {
            LogHelper.print("-facePassHelper--onGetFacePassLocalCountError-msg:" + msg);
        }
    }

    public interface OnDeleteLocalFaceListener {
        default void onDeleteLocalFace(FacePassPeopleInfo facePassPeopleInfo) {
            LogHelper.print("-facePassHelper--onDeleteLocalFace-facePassPeopleInfo:" + facePassPeopleInfo.getPhone());
        }

        default void onDeleteError(FacePassPeopleInfo passPeopleInfo, String msg) {
            LogHelper.print("-facePassHelper--onDeleteError-facePassPeopleInfo:" + passPeopleInfo.getPhone() + " msg: " + msg);
        }
    }

    public interface OnQueryLocalFacePassListener {
        default void onQueryLocalFacePassSuccess(List<FacePassPeopleInfo> facePassPeopleInfoList) {
            LogHelper.print("-facePassHelper--onQueryLocalFacePassSuccess--count:" + facePassPeopleInfoList.size());
        }

        default void onQueryLocalFacePassError(String msg) {
            LogHelper.print("-facePassHelper--onQueryLocalFacePassError-msg:" + msg);
        }
    }

    public interface OnDeleteAllFaceListener {
        default void onDeleteAllFace() {
            LogHelper.print("-facePassHelper--onDeleteAllFace--success");
        }

        default void onDeleteAllFaceError(String msg) {
            LogHelper.print("-facePassHelper--onDeleteAllFaceError- msg: " + msg);
        }
    }

    public interface OnHandleFaceTokenListener {
        default void onHandleLocalFace(String faceToken, FacePassPeopleInfo facePassPeopleInfo) {
            LogHelper.print("-facePassHelper--onHandleLocalFace-facePassPeopleInfo:" + facePassPeopleInfo.getPhone());
        }

        default void onHandleLocalFaceError(String faceToken) {
            LogHelper.print("-facePassHelper--onHandleLocalFaceError-" + faceToken);
        }
    }

    public interface OnHandleCardNumberListener {
        default void onHandleLocalCardNumber(String cardNumber, FacePassPeopleInfo facePassPeopleInfo) {
            LogHelper.print("-facePassHelper--onHandleLocalCardNumber-facePassPeopleInfo:" + facePassPeopleInfo.getPhone());
        }

        default void onHandleLocalCardNumberError(String cardNumber) {
            LogHelper.print("-facePassHelper--onHandleLocalCardNumberError-" + cardNumber);
        }
    }

    public long getFaceCount(){
        daoSession.clear();
        FacePassPeopleInfoDao facePassPeopleInfoDao = daoSession.getFacePassPeopleInfoDao();
        return facePassPeopleInfoDao.count();
    }

    public interface OnHandlePhoneListener {
        default void onHandleLocalPhone(String phone, FacePassPeopleInfo facePassPeopleInfo) {
            LogHelper.print("-facePassHelper--onHandleLocalPhone-facePassPeopleInfo:" + facePassPeopleInfo.getPhone());
        }

        default void onHandleLocalPhoneError(String phone) {
            LogHelper.print("-facePassHelper--onHandleLocalPhoneError-" + phone);
        }
    }
}

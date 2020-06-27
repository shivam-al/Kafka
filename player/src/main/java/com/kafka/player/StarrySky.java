//package com.kafka.player;
//
//import android.annotation.SuppressLint;
//import android.app.Application;
//import android.content.Context;
//
//import androidx.annotation.NonNull;
//
//import com.kafka.player.playback.MediaResource;
//import com.kafka.player.playback.Playback;
//import com.kafka.player.playback.exo.ExoPlayback;
//
//import java.lang.reflect.Method;
//
//public class StarrySky {
//    private static volatile StarrySky sStarrySky;
//    private static volatile boolean isInitializing;
//    private volatile static boolean alreadyInit;
//
//    private static Application globalContext;
//    private static StarrySkyConfig mStarrySkyConfig;
//    private StarrySkyActivityLifecycle mLifecycle;
//    private IMediaConnection connection;
//    private PlayerControl mPlayerControl;
//    private StarrySkyRegistry mRegistry;
//    private MediaQueueProvider mediaQueueProvider;
//    private MediaResource mediaResource;
//    private Playback playback;
//    private IPlaybackManager playbackManager;
//    private MediaQueue mediaQueue;
//    private static IMediaConnection.OnConnectListener mOnConnectListener;
//
//    //超时时间设置
//    private long httpConnectTimeout = -1;
//    private long httpReadTimeout = -1;
//    //是否跳过https
//    private boolean skipSSLChain = false;
//
//    public static void init(Application application) {
//        init(application, null, null);
//    }
//
//    public static void init(Application application, StarrySkyConfig config) {
//        init(application, config, null);
//    }
//
//    public static void init(Application application, StarrySkyConfig config,
//                            IMediaConnection.OnConnectListener listener) {
//        if (alreadyInit) {
//            return;
//        }
//        alreadyInit = true;
//        globalContext = application;
//        mStarrySkyConfig = config;
//        mOnConnectListener = listener;
//        get();
//    }
//
//    private void registerLifecycle(Application context) {
//        if (null != mLifecycle) {
//            context.unregisterActivityLifecycleCallbacks(mLifecycle);
//        }
//        mLifecycle = new StarrySkyActivityLifecycle();
//        context.registerActivityLifecycleCallbacks(mLifecycle);
//    }
//
//    public static StarrySky get() {
//        if (sStarrySky == null) {
//            synchronized (StarrySky.class) {
//                if (sStarrySky == null) {
//                    checkAndInitializeStarrySky(globalContext);
//                }
//            }
//        }
//        return sStarrySky;
//    }
//
//    public static void release() {
//        if (StarrySky.get().mLifecycle != null) {
//            globalContext.unregisterActivityLifecycleCallbacks(StarrySky.get().mLifecycle);
//        }
//        isInitializing = false;
//        alreadyInit = false;
//        globalContext = null;
//        mStarrySkyConfig = null;
//        mOnConnectListener = null;
//        sStarrySky = null;
//    }
//
//    public static PlayerControl with() {
//        return StarrySky.get().getPlayerControl();
//    }
//
//    public void registerPlayerControl(PlayerControl playerControl) {
//        this.mPlayerControl = playerControl;
//    }
//
//    private static void checkAndInitializeStarrySky(@NonNull Context context) {
//        if (isInitializing) {
//            throw new IllegalStateException("checkAndInitializeStarrySky");
//        }
//        isInitializing = true;
//        try {
//            initializeStarrySky(context, new StarrySkyBuilder());
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        } finally {
//            isInitializing = false;
//        }
//    }
//
//    private static void initializeStarrySky(Context context, StarrySkyBuilder builder) {
//        if (mStarrySkyConfig != null) {
//            mStarrySkyConfig.applyOptions(context, builder);
//        }
//        if (context == null) {
//            context = globalContext;
//        }
//        if (context == null) {
//            context = getContextReflex();
//        }
//        if (context == null) {
//            throw new IllegalArgumentException("StarrySky 初始化失败，上下文为 null");
//        }
//        StarrySky starrySky = builder.build(context);
//        starrySky.httpConnectTimeout = builder.httpConnectTimeout;
//        starrySky.httpReadTimeout = builder.httpReadTimeout;
//        starrySky.skipSSLChain = builder.skipSSLChain;
//        sStarrySky = starrySky;
//
//        if (mStarrySkyConfig != null) {
//            mStarrySkyConfig.applyStarrySkyRegistry(context, starrySky.mRegistry);
//        }
//
//        //注册通知栏
//        StarrySkyNotificationManager.NotificationFactory factory =
//                mStarrySkyConfig != null ? mStarrySkyConfig.getNotificationFactory() : null;
//        StarrySkyNotificationManager notificationManager =
//                new StarrySkyNotificationManager(builder.isOpenNotification, factory);
//        starrySky.mRegistry.registryNotificationManager(notificationManager);
//
//        StarrySkyCacheManager cacheManager = new StarrySkyCacheManager(
//                context,
//                builder.isOpenCache,
//                builder.cacheDestFileDir);
//        starrySky.mRegistry.registryStarryCache(cacheManager);
//
//        //播放器
//        starrySky.playback = starrySky.mRegistry.getPlayback();
//        if (starrySky.playback == null) {
//            starrySky.playback = new ExoPlayback(context, cacheManager);
//        }
//        if (starrySky.playbackManager == null) {
//            starrySky.playbackManager = new PlaybackManager(starrySky.mediaQueue, starrySky.playback);
//        }
//    }
//
//    /**
//     * 反射一下主线程获取一下上下文
//     */
//    private static Application getContextReflex() {
//        try {
//            @SuppressLint("PrivateApi")
//            Class<?> activityThreadClass = Class.forName("android.app.ActivityThread");
//            @SuppressLint("DiscouragedPrivateApi")
//            Method currentApplicationMethod = activityThreadClass.getDeclaredMethod("currentApplication");
//            currentApplicationMethod.setAccessible(true);
//            Application currentApplication = (Application) currentApplicationMethod.invoke(null);
//            if (globalContext == null) {
//                globalContext = currentApplication;
//            }
//            return currentApplication;
//        } catch (Exception ex) {
//            ex.printStackTrace();
//            return null;
//        }
//    }
//
//    StarrySky(
//            IMediaConnection connection,
//            MediaQueueProvider mediaQueueProvider,
//            MediaQueue mediaQueue) {
//        this.connection = connection;
//
//        this.mediaQueueProvider = mediaQueueProvider;
//        this.mediaQueue = mediaQueue;
//        mediaResource = new MediaResource();
//
//        registerLifecycle(globalContext);
//
//        mRegistry = new StarrySkyRegistry(globalContext);
//
//        //链接服务
//        connection.connect();
//        connection.setOnConnectListener(mOnConnectListener);
//    }
//
//    public IMediaConnection getConnection() {
//        return connection;
//    }
//
//    private PlayerControl getPlayerControl() {
//        if (mPlayerControl == null) {
//            return new StarrySkyPlayerControl(globalContext);
//        }
//        return mPlayerControl;
//    }
//
//    public Playback getPlayback() {
//        return playback;
//    }
//
//    public StarrySkyRegistry getRegistry() {
//        return mRegistry;
//    }
//
//    public MediaQueueProvider getMediaQueueProvider() {
//        return mediaQueueProvider;
//    }
//
//    IPlaybackManager getPlaybackManager() {
//        return playbackManager;
//    }
//
//    public MediaResource getMediaResource() {
//        return mediaResource;
//    }
//
//    public long getHttpConnectTimeout() {
//        return httpConnectTimeout;
//    }
//
//    public long getHttpReadTimeout() {
//        return httpReadTimeout;
//    }
//
//    public boolean isSkipSSLChain() {
//        return skipSSLChain;
//    }
//}

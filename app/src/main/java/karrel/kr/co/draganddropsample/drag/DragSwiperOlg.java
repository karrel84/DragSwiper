package karrel.kr.co.draganddropsample.drag;

import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by 이주영 on 2017-01-18.
 */

public class DragSwiperOlg implements View.OnTouchListener {

    /**
     * 손잡이가 보여지는 사이즈
     */
    private int handleSize;

    /**
     * 좌측으로 SWIPE됐을때의 X값이다.
     */
    private int swipeLeftX;

    /**
     * form을 down한 좌표X
     */
    private float downX;

    /**
     * Drag중인가?
     */
    private boolean mDragging = false;

    /**
     * Minimum velocity that will be detected as a fling
     */
    private static final int MIN_FLING_VELOCITY = 400; // dips per second

    /**
     * maximum velocity that will be detected as a fling
     */
    private static final int MAX_FLING_VELOCITY = 8000; // dips per second
    private View mRootview;


    /**
     * 최소 가속도
     */
    private float mMinVelocity;

    /**
     * 최대 가속도
     */
    private float mMaxVelocity;


    public static final int BASE_SETTLE_DURATION = 200; // ms
    public static final int MAX_SETTLE_DURATION = 300; // ms

    private View mTargetView;
    private View mEventView;

    GestureDetector detector;

    private SwiperStatus mSwipeStatus;

    public DragSwiperOlg() {

    }

    protected void init(View rootView, View targetView, View eventView, int leftX, int handleSize) {
        targetView.setVisibility(View.VISIBLE);
        mRootview = rootView;
        mSwipeStatus = new SwiperStatus();
        setHandlerView(targetView);
        setEventView(eventView);
        setSwipeLeftX(leftX);
        setHandleSize(handleSize);
        setupEvent();
    }

    /**
     * 옮길 뷰를 설정한다.
     */
    public void setHandlerView(View view) {
        mTargetView = view;
    }

    /**
     * 이벤트를 받을 뷰를 설정한다.
     */
    public void setEventView(View view) {
        mEventView = view;
    }

    /**
     * 초기 X좌표
     */
    public void setSwipeLeftX(int x) {
        swipeLeftX = x;
    }

    /**
     * 핸들 사이즈
     */
    public void setHandleSize(int size) {
        handleSize = size;
    }

    /**
     * 자동모드 손잡이에 대한 이벤트를 처리한다
     */
    private void setupEvent() {
        final float density = mRootview.getResources().getDisplayMetrics().density;
        mMinVelocity = MIN_FLING_VELOCITY * density;
        mMaxVelocity = MAX_FLING_VELOCITY * density;

        View.OnTouchListener l = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    mDragging = true;
                    downX = event.getX();
                    mTargetView.setPressed(true);
                }
                return false;
            }
        };

        mTargetView.setOnTouchListener(l);

        detector = new GestureDetector(mRootview.getContext(), new GestureDetector.OnGestureListener() {

            @Override
            public boolean onDown(MotionEvent e) {
                return true;
            }

            @Override
            public void onShowPress(MotionEvent e) {

            }

            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                swipeToggle();
                return true;
            }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                setX(e2.getRawX(), downX);

                return true;
            }

            @Override
            public void onLongPress(MotionEvent e) {

            }

            private static final int SWIPE_MIN_DISTANCE = 60;
            private static final int SWIPE_THRESHOLD_VELOCITY = 400;

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {


                if (e1.getRawX() - e2.getRawX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                    swipeLeft(getDuration(e1, e2, (int) velocityX, (int) velocityY));
                    return true;
                } else if (e2.getRawX() - e1.getRawX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                    swipeRight(getDuration(e1, e2, (int) velocityX, (int) velocityY));
                    return true;
                }

                return false;
            }

            private int getDuration(MotionEvent e1, MotionEvent e2, int velocityX, int velocityY) {
                int dx = (int) (e1.getRawX() - e2.getRawX());
                int dy = 0;
                int xvel = velocityX;
                int yvel = velocityY;
                return computeSettleDuration(dx, dy, xvel, yvel);
            }
        });
    }

    private int computeSettleDuration(int dx, int dy, int xvel, int yvel) {
        xvel = clampMag(xvel, (int) mMinVelocity, (int) mMaxVelocity);
        yvel = clampMag(yvel, (int) mMinVelocity, (int) mMaxVelocity);
        final int absDx = Math.abs(dx);
        final int absDy = Math.abs(dy);
        final int absXVel = Math.abs(xvel);
        final int absYVel = Math.abs(yvel);
        final int addedVel = absXVel + absYVel;
        final int addedDistance = absDx + absDy;

        final float xweight = xvel != 0 ? (float) absXVel / addedVel :
                (float) absDx / addedDistance;
        final float yweight = yvel != 0 ? (float) absYVel / addedVel :
                (float) absDy / addedDistance;

        int xduration = computeAxisDuration(dx, xvel, mEventView.getWidth());
        int yduration = computeAxisDuration(dy, yvel, 0);

        return (int) (xduration * xweight + yduration * yweight);
    }

    private int clampMag(int value, int absMin, int absMax) {
        final int absValue = Math.abs(value);
        if (absValue < absMin) return 0;
        if (absValue > absMax) return value > 0 ? absMax : -absMax;
        return value;
    }

    private int computeAxisDuration(int delta, int velocity, int motionRange) {
        if (delta == 0) {
            return 0;
        }

        final int width = mEventView.getWidth();
        final int halfWidth = width / 2;
        final float distanceRatio = Math.min(1f, (float) Math.abs(delta) / width);
        final float distance = halfWidth + halfWidth
                * distanceInfluenceForSnapDuration(distanceRatio);

        int duration;
        velocity = Math.abs(velocity);
        if (velocity > 0) {
            duration = 4 * Math.round(MAX_SETTLE_DURATION * Math.abs(distance / velocity));
        } else {
            final float range = (float) Math.abs(delta) / motionRange;
            duration = (int) ((range + 1) * BASE_SETTLE_DURATION);
        }
        return Math.min(duration, MAX_SETTLE_DURATION);
    }

    private float distanceInfluenceForSnapDuration(float f) {
        f -= 0.5f; // center the values about 0.
        f *= 0.3f * Math.PI / 2.0f;
        return (float) Math.sin(f);
    }

    protected void swipeToggle() {
    }

    /**
     * 사이드 메뉴를 좌측으로 Swipe시킨다
     */
    protected void swipeLeft() {
    }

    /**
     * 사이드 메뉴를 좌측으로 Swipe시킨다
     */
    protected void swipeLeft(long duration) {

    }

    /**
     * 사이드 메뉴를 우측으로 Swipe시킨다
     */
    protected void swipeRight() {

    }

    /**
     * 사이드 메뉴를 우측으로 Swipe시킨다
     */
    protected void swipeRight(long duration) {

    }

    /**
     * 현재 위치에서 어느 방향으로 이동할지 결정한다.
     */
    protected void swipe() {

    }

    /**
     * x의 위치를 보낸다.
     */
    public void setX(float x, float downX) {
        setSwipeStatus(SwiperStatus.DRAG);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            if (mDragging) {
                swipe();
                detector.onTouchEvent(event);
            }
            mDragging = false;
            mTargetView.setPressed(false);
        }

        if (mDragging) {
            detector.onTouchEvent(event);
        }
        return mDragging;
    }

    /**
     * 현재 Swipe의 상태를 입력한다.
     */
    protected void setSwipeStatus(int status) {
        mSwipeStatus.setStatus(status);
    }
}

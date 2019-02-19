package karrel.kr.co.draganddropsample.drag;

import android.view.MotionEvent;
import android.view.View;

import karrel.kr.co.draganddropsample.util.Util;

/**
 * Created by 이주영 on 2017-01-17.
 *
 * @layout/front_menual_buttons 에 대한 이벤트처리를 한다.
 */

public class MenualmodeSwiper extends DragSwiperOlg implements View.OnTouchListener {

    View menual_mode_form;

    View tmpView;

    View mRootView;

    public static int STATUS = SwiperStatus.CLOSED;

    /**
     * 좌측으로 SWIPE됐을때의 X값이다.
     */
    private int swipeLeftX;

    /**
     * 우측으로 SWIPE됐을때의 X값이다.
     */
    private int swipeRightX;

    /**
     * 화면 크기
     */
    private int screenWidth;

    /**
     * 손잡이가 보여지는 사이즈
     */
    private int handleSize;

    public static MenualmodeSwiper newInstance(View rootView, View manualModeForm, View eventFrom) {

        return new MenualmodeSwiper(rootView, manualModeForm, eventFrom);
    }

    public MenualmodeSwiper(View rootView, View manualModeForm, View eventFrom) {
        menual_mode_form = manualModeForm;
        tmpView = eventFrom;

        init(mRootView = rootView, menual_mode_form, tmpView, swipeLeftX, handleSize);
    }

    @Override
    protected void init(View rootView, View targetView, View eventView, int leftX, int handleSize) {
        super.init(rootView, targetView, eventView, leftX, handleSize);

        rootView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return MenualmodeSwiper.this.onTouch(view, motionEvent);
            }
        });

        setupX();
    }

    /**
     * AUTO MODE의 초기위치 설정
     */
    private void setupX() {
        screenWidth = Util.getScreenWidth(mRootView.getContext());
        handleSize = (int) Util.dpToPixel(28, mRootView.getContext());

        swipeLeftX = (int) Util.dpToPixel(12, mRootView.getContext());
        swipeRightX = screenWidth - handleSize;

        System.out.println("swipeRightX : " + swipeRightX);

        // autobuttons의 초기위치를 잡아준다.
        menual_mode_form.setX(swipeRightX);
    }

    /**
     * 사이드 메뉴를 좌측으로 Swipe시킨다
     */
    @Override
    public void swipeLeft() {
        super.swipeLeft();

        setSwipeStatus(SwiperStatus.OPENED);

        menual_mode_form.animate().translationX(swipeLeftX).setDuration(BASE_SETTLE_DURATION);
    }

    /**
     * 사이드 메뉴를 좌측으로 Swipe시킨다
     */
    @Override
    public void swipeLeft(long duration) {
        super.swipeLeft(duration);

        setSwipeStatus(SwiperStatus.OPENED);

        menual_mode_form.animate().translationX(swipeLeftX).setDuration(duration).setInterpolator(null);
    }

    /**
     * 사이드 메뉴를 우측으로 Swipe시킨다
     */
    @Override
    public void swipeRight() {
        super.swipeRight();

        setSwipeStatus(SwiperStatus.CLOSED);

        menual_mode_form.animate().translationX(swipeRightX).setDuration(BASE_SETTLE_DURATION);
    }

    /**
     * 사이드 메뉴를 우측으로 Swipe시킨다
     */
    @Override
    public void swipeRight(long duration) {
        super.swipeRight(duration);

        setSwipeStatus(SwiperStatus.CLOSED);

        menual_mode_form.animate().translationX(swipeRightX).setDuration(duration).setInterpolator(null);
    }

    /**
     * 현재 위치에서 어느 방향으로 이동할지 결정한다.
     */
    @Override
    public void swipe() {
        super.swipe();

        // 현재 뷰의 왼쪽 X값을 구한다.
        float curLeftX = menual_mode_form.getX();
        // 오른쪽 X가 스크린의 1/2보다 크면 오른쪽으로 이동 아니면 왼쪽으로 이동
        if (curLeftX < screenWidth / 2) {
            swipeLeft();
        } else {
            swipeRight();
        }
    }

    @Override
    public void setX(float x, float downX) {
        super.setX(x, downX);

        float x2 = x - downX;
        if (x2 < 0) x2 = 0;

        menual_mode_form.setX(x2);
    }

    /**
     * 사이드 메뉴를 Swipe시킨다
     */
    @Override
    public void swipeToggle() {
        super.swipeToggle();

        // autoModeForm이 초기화된 위치에 있으면 오른쪽으로 Swipe
        if (menual_mode_form.getX() == swipeRightX) {
            swipeLeft();
        } else {
            // autoModeForm이 초기화된 위치에 있지 않으면 왼쪽으로 Swipe
            swipeRight();
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return super.onTouch(v, event);
    }


    @Override
    protected void setSwipeStatus(int status) {
        super.setSwipeStatus(status);
        STATUS = status;
    }
}

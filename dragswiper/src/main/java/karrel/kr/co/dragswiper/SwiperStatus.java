package karrel.kr.co.dragswiper;

/**
 * Created by 이주영 on 2017-01-19.
 */

public class SwiperStatus {
    public static int CLOSED = 0;
    public static int HIDE = 1;
    public static int DRAG = 2;
    public static int OPENED = 3;

    private int mStatus = CLOSED;

    public int getStatus() {
        return mStatus;
    }

    public void setStatus(int status) {
        if (status < CLOSED || status > OPENED) {
            throw new IllegalStateException("status 는 지정된 상태값을 사용해야 합니다.");
        }
        mStatus = status;
    }
}

package karrel.kr.co.draganddropsample.dragSwiper

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View.OnTouchListener
import karrel.kr.co.draganddropsample.R
import karrel.kr.co.draganddropsample.drag.SwiperStatus


/**
 * Created by Rell on 2019. 2. 18..
 */

//Context context, AttributeSet attrs, int defStyleAttr
class DragSwiper @JvmOverloads
constructor(context: Context, attrs: AttributeSet) : ConstraintLayout(context, attrs) {


    // 손잡이 사이즈
    private var headerSize: Int = 0
    // 이 레이아웃의 정렬방향 LEFT, TOP, RIGHT, BOTTOM
    private val GRAVITY_LEFT = 0
    private val GRAVITY_TOP = 1
    private val GRAVITY_RIGHT = 2
    private val GRAVITY_BOTTOM = 3
    private var gravity: Int = GRAVITY_RIGHT

    private val MIN_FLING_VELOCITY = 400 // dips per second
    private val MAX_FLING_VELOCITY = 8000 // dips per second

    /**
     * 최소 가속도
     */
    private var mMinVelocity: Float = 0.toFloat()

    /**
     * 최대 가속도
     */
    private var mMaxVelocity: Float = 0.toFloat()

    /**
     * form을 down한 좌표X
     */
    private var locateX: Int = 0

    private var isDragging = false


    private val BASE_SETTLE_DURATION = 200L // ms
    private val MAX_SETTLE_DURATION = 300L // ms

    private lateinit var detector: GestureDetector
    private var swipeStatus: SwiperStatus = SwiperStatus()

    /**
     * 좌측으로 SWIPE됐을때의 X값이다.
     */
    private var swipeLeftX: Int = 0

    /**
     * 우측으로 SWIPE됐을때의 X값이다.
     */
    private var swipeRightX: Int = 0

    init {
        setupEvent()

        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.DragSwiper)
        gravity = typedArray.getInteger(R.styleable.DragSwiper_dsGravity, GRAVITY_LEFT)
        headerSize = typedArray.getDimensionPixelSize(R.styleable.DragSwiper_dsHandleSize, 0)

        typedArray.recycle()
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)

        println("width : $width")

        initLayoutLocation()

    }

    private fun initLayoutLocation() {
        when (gravity) {
            GRAVITY_LEFT -> {
                x = (headerSize).toFloat() - width
                swipeLeftX = x.toInt()
                swipeRightX = 0
            }
            GRAVITY_RIGHT -> {
                x = (width - headerSize).toFloat()
                swipeLeftX = 0
                swipeRightX = x.toInt()
            }
        }
    }

    /**
     * 자동모드 손잡이에 대한 이벤트를 처리한다
     */
    private fun setupEvent() {
        val density = rootView.resources.displayMetrics.density
        mMinVelocity = MIN_FLING_VELOCITY * density
        mMaxVelocity = MAX_FLING_VELOCITY * density

        val l = OnTouchListener { _, event ->

            println("OnTouchListener.event : ${event.action}")
            if (event.action == MotionEvent.ACTION_DOWN) {
                isDragging = true
                locateX = event.x.toInt()
                isPressed = true
            } else if (event.action == MotionEvent.ACTION_UP) {
                if (isDragging) {
                    swipe()
                    detector.onTouchEvent(event)
                }
                isDragging = false
                isPressed = false
            }

            if (isDragging) {
                detector.onTouchEvent(event)
            }
            return@OnTouchListener isDragging
        }

        setOnTouchListener(l)

        detector = GestureDetector(context, object : GestureDetector.OnGestureListener {

            private val SWIPE_MIN_DISTANCE = 60
            private val SWIPE_THRESHOLD_VELOCITY = 400

            override fun onDown(e: MotionEvent): Boolean {
                return true
            }

            override fun onShowPress(e: MotionEvent) {

            }

            override fun onSingleTapUp(e: MotionEvent): Boolean {
                swipeToggle()
                return true
            }

            override fun onScroll(e1: MotionEvent, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
                setSwipeStatus(SwiperStatus.DRAG)
                setX(e2.rawX, locateX)

                return true
            }

            override fun onLongPress(e: MotionEvent) {

            }

            override fun onFling(e1: MotionEvent, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {


                if (e1.rawX - e2.rawX > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                    swipeLeft(getDuration(e1, e2, velocityX.toInt(), velocityY.toInt()).toLong())
                    return true
                } else if (e2.rawX - e1.rawX > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                    swipeRight(getDuration(e1, e2, velocityX.toInt(), velocityY.toInt()).toLong())
                    return true
                }

                return false
            }

            private fun getDuration(e1: MotionEvent, e2: MotionEvent, velocityX: Int, velocityY: Int): Int {
                val dx = (e1.rawX - e2.rawX).toInt()
                val dy = 0
                return computeSettleDuration(dx, dy, velocityX, velocityY)
            }
        })
    }

    private fun setX(rawX: Float, locateX: Int) {
        var x2 = rawX - locateX
        x = x2
    }

    private fun computeSettleDuration(dx: Int, dy: Int, xvel: Int, yvel: Int): Int {
        var xvel = xvel
        var yvel = yvel
        xvel = clampMag(xvel, mMinVelocity.toInt(), mMaxVelocity.toInt())
        yvel = clampMag(yvel, mMinVelocity.toInt(), mMaxVelocity.toInt())
        val absDx = Math.abs(dx)
        val absDy = Math.abs(dy)
        val absXVel = Math.abs(xvel)
        val absYVel = Math.abs(yvel)
        val addedVel = absXVel + absYVel
        val addedDistance = absDx + absDy

        val xweight = if (xvel != 0)
            absXVel.toFloat() / addedVel
        else
            absDx.toFloat() / addedDistance
        val yweight = if (yvel != 0)
            absYVel.toFloat() / addedVel
        else
            absDy.toFloat() / addedDistance

        val xduration = computeAxisDuration(dx, xvel, width)
        val yduration = computeAxisDuration(dy, yvel, 0)

        return (xduration * xweight + yduration * yweight).toInt()
    }

    private fun clampMag(value: Int, absMin: Int, absMax: Int): Int {
        val absValue = Math.abs(value)
        if (absValue < absMin) return 0
        return if (absValue > absMax) if (value > 0) absMax else -absMax else value
    }

    private fun computeAxisDuration(delta: Int, velocity: Int, motionRange: Int): Long {
        var velocity = velocity
        if (delta == 0) {
            return 0
        }

        val halfWidth = width / 2
        val distanceRatio = Math.min(1f, Math.abs(delta).toFloat() / width)
        val distance = halfWidth + halfWidth * distanceInfluenceForSnapDuration(distanceRatio)

        velocity = Math.abs(velocity)
        val duration = if (velocity > 0) {
            (4 * Math.round(MAX_SETTLE_DURATION * Math.abs(distance / velocity))).toLong()
        } else {
            val range = Math.abs(delta).toFloat() / motionRange
            ((range + 1) * BASE_SETTLE_DURATION).toInt().toLong()
        }
        return Math.min(duration, MAX_SETTLE_DURATION)
    }


    private fun distanceInfluenceForSnapDuration(f: Float): Float {
        var f = f
        f -= 0.5f // center the values about 0.
        f *= (0.3f * Math.PI / 2.0f).toFloat()
        return Math.sin(f.toDouble()).toFloat()
    }


    /**
     * 현재 Swipe의 상태를 입력한다.
     */
    private fun setSwipeStatus(status: Int) {
        swipeStatus.status = status
    }

    private fun swipeToggle() {
        println("swipeToggle()")

        if (swipeLeftX == x.toInt()) {
            swipeRight()
        } else if (swipeRightX == x.toInt()) {
            swipeLeft()
        }

    }

    /**
     * 사이드 메뉴를 좌측으로 Swipe시킨다
     */
    private fun swipeLeft(duration: Long = BASE_SETTLE_DURATION) {
        println("swipeLeft($duration)")
        animate().translationX(swipeLeftX.toFloat()).duration = BASE_SETTLE_DURATION;
    }

    /**
     * 사이드 메뉴를 우측으로 Swipe시킨다
     */
    private fun swipeRight(duration: Long = BASE_SETTLE_DURATION) {
        println("swipeRight($duration)")
        animate().translationX(swipeRightX.toFloat()).duration = BASE_SETTLE_DURATION;
    }

    /**
     * 현재 위치에서 어느 방향으로 이동할지 결정한다.
     */
    private fun swipe() {
        // 현재 뷰의 왼쪽 X값을 구한다.
        val curLeftX = x
        // 오른쪽 X가 스크린의 1/2보다 크면 오른쪽으로 이동 아니면 왼쪽으로 이동
        if (curLeftX < width / 2) {
            swipeLeft()
        } else {
            swipeRight()
        }
    }


}
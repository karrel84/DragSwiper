package karrel.kr.co.draganddropsample.dragSwiper

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
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
    private var locateY: Int = 0

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

    private var swipeTopY: Int = 0
    private var swipeBottomY: Int = 0

    private lateinit var parentView: View
    private var targetView = this

    init {

        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.DragSwiper)
        gravity = typedArray.getInteger(R.styleable.DragSwiper_dsGravity, GRAVITY_LEFT)
        headerSize = typedArray.getDimensionPixelSize(R.styleable.DragSwiper_dsHandleSize, 0)

        typedArray.recycle()

    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)

        parentView = parent as View

        initLayoutLocation()
        setupEvent()

    }

    private fun initLayoutLocation() {
        when (gravity) {
            GRAVITY_LEFT -> {
                swipeLeftX = headerSize - width
                swipeRightX = 0
                x = swipeLeftX.toFloat()
            }
            GRAVITY_RIGHT -> {
                swipeLeftX = parentView.width - width
                swipeRightX = parentView.width - headerSize
                x = swipeRightX.toFloat()
            }
            GRAVITY_TOP -> {
                swipeTopY = headerSize - height
                swipeBottomY = 0
                y = swipeTopY.toFloat()
            }
            GRAVITY_BOTTOM -> {
                swipeTopY = parentView.height - height
                swipeBottomY = parentView.height - headerSize
                y = swipeBottomY.toFloat()
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

        // this 뷰 이벤트
        setupTargetViewTouchEvent()

        // 부모뷰 이벤트
        setupParentTouchEvent()

        // 제스쳐 디텍터
        setupGestureDetector()
    }

    private fun setupGestureDetector() {
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

                if (gravity == GRAVITY_LEFT || gravity == GRAVITY_RIGHT) {
                    setX(e2.x, locateX)
                } else {
                    setY(e2.y, locateY)
                }

                return true
            }

            override fun onLongPress(e: MotionEvent) {

            }

            override fun onFling(e1: MotionEvent, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {


                if (gravity == GRAVITY_LEFT || gravity == GRAVITY_RIGHT) {
                    if (e1.x - e2.x > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                        swipeLeft(getDurationX(e1, e2, velocityX.toInt(), velocityY.toInt()).toLong())
                        return true
                    } else if (e2.x - e1.x > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                        swipeRight(getDurationX(e1, e2, velocityX.toInt(), velocityY.toInt()).toLong())
                        return true
                    }
                } else {
                    if (e1.y - e2.y > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
                        swipeTop(getDurationY(e1, e2, velocityX.toInt(), velocityY.toInt()).toLong())
                        return true
                    } else if (e2.y - e1.y > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
                        swipeBottom(getDurationY(e1, e2, velocityX.toInt(), velocityY.toInt()).toLong())
                        return true
                    }
                }

                return false
            }

            private fun getDurationX(e1: MotionEvent, e2: MotionEvent, velocityX: Int, velocityY: Int): Int {
                val dx = (e1.x - e2.x).toInt()
                val dy = 0
                return computeSettleDuration(dx, dy, velocityX, velocityY)
            }

            private fun getDurationY(e1: MotionEvent, e2: MotionEvent, velocityX: Int, velocityY: Int): Int {
                val dx = 0
                val dy = (e1.y - e2.y).toInt()
                return computeSettleDuration(dx, dy, velocityX, velocityY)
            }
        })
    }

    private fun setupParentTouchEvent() {
        parentView.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                if (isDragging) {
                    swipe()
                    detector.onTouchEvent(event)
                }
                isDragging = false
            }

            if (isDragging) {
                detector.onTouchEvent(event)
            }
            return@setOnTouchListener isDragging
        }
    }

    private fun setupTargetViewTouchEvent() {
        setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                isDragging = true
                locateX = event.x.toInt()
                locateY = event.y.toInt()
                targetView.isPressed = true
            }
            return@setOnTouchListener false
        }
    }

    private fun setX(parentX: Float, locateX: Int) {
        var valueX = parentX - locateX
        if (gravity == GRAVITY_LEFT) {
            if (valueX <= 0) {
                x = valueX
            }
        } else if (gravity == GRAVITY_RIGHT) {
            if (valueX >= parentView.width - width) {
                x = valueX
            }
        }
    }

    private fun setY(parentY: Float, locateY: Int) {
        var valueY = parentY - locateY
        if (gravity == GRAVITY_TOP) {
            if (valueY <= 0) {
                y = valueY
            }
        } else if (gravity == GRAVITY_BOTTOM) {
            if (valueY >= parentView.height - height) {
                y = valueY
            }
        }
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

    private fun swipeTop(duration: Long = BASE_SETTLE_DURATION) {
        animate().translationY(swipeTopY.toFloat()).duration = duration
    }

    private fun swipeBottom(duration: Long = BASE_SETTLE_DURATION) {
        animate().translationY(swipeBottomY.toFloat()).duration = duration
    }

    /**
     * 사이드 메뉴를 좌측으로 Swipe시킨다
     */
    private fun swipeLeft(duration: Long = BASE_SETTLE_DURATION) {
        animate().translationX(swipeLeftX.toFloat()).duration = duration
    }

    /**
     * 사이드 메뉴를 우측으로 Swipe시킨다
     */
    private fun swipeRight(duration: Long = BASE_SETTLE_DURATION) {
        animate().translationX(swipeRightX.toFloat()).duration = duration
    }

    /**
     * 현재 위치에서 어느 방향으로 이동할지 결정한다.
     */
    private fun swipe() {
        if (gravity == GRAVITY_LEFT || gravity == GRAVITY_RIGHT) {
            if (x < swipeLeftX + (width / 2)) {
                swipeLeft()
            } else {
                swipeRight()
            }
        } else if (gravity == GRAVITY_TOP || gravity == GRAVITY_BOTTOM) {
            if (y < swipeTopY + (height / 2)) {
                swipeTop()
            } else {
                swipeBottom()
            }
        }
    }

    private fun swipeToggle() {
        if (gravity == GRAVITY_LEFT || gravity == GRAVITY_RIGHT) {
            if (swipeLeftX == x.toInt()) {
                swipeRight()
            } else if (swipeRightX == x.toInt()) {
                swipeLeft()
            }
        } else {
            if (swipeTopY == y.toInt()) {
                swipeBottom()
            } else if (swipeBottomY == y.toInt()) {
                swipeTop()
            }
        }
    }

}
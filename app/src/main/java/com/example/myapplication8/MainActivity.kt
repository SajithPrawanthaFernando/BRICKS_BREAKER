package com.example.myapplication8

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    private lateinit var scoreText: TextView
    private lateinit var highScoreText: TextView
    private lateinit var paddle: View
    private lateinit var ball: View
    private lateinit var brickContainer: LinearLayout
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var gameOverText: TextView
    private lateinit var scoreAfterGameOverText: TextView // Add this variable

    private var ballX = 0f
    private var ballY = 0f
    private var ballSpeedX = 0f
    private var ballSpeedY = 0f
    private var paddleX = 0f
    private var paddleWidth = 0
    private var score = 0
    private var highScore = 0

    private val brickRows = 9
    private val brickColumns = 10
    private val brickWidth = 100
    private val brickHeight = 40
    private val brickMargin = 4

    private var isBallLaunched = false
    private var lives = 3
    private var bigBallActive = false
    private var powerUps = arrayOf("Paddle Size Increase", "Big Ball")

    private val powerUpDuration = 5000L // 5 seconds
    private val powerUpActivationDelay = 15000L // 15 seconds
    private var visibleBricksCount = brickRows * brickColumns

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        scoreText = findViewById(R.id.scoreText)
        highScoreText = findViewById(R.id.highScoreText)
        paddle = findViewById(R.id.paddle)
        ball = findViewById(R.id.ball)
        brickContainer = findViewById(R.id.brickContainer)
        sharedPreferences = getSharedPreferences("GamePreferences", Context.MODE_PRIVATE)
        gameOverText = findViewById(R.id.gameOverText)
        scoreAfterGameOverText =
            findViewById(R.id.scoreAfterGameOverText) // Initialize scoreAfterGameOverText

        // Initialize score from SharedPreferences
        score = sharedPreferences.getInt("score", 0)
        scoreText.text = "Score: $score"

        highScore =
            sharedPreferences.getInt("highScore", 0) // Fetch high score from SharedPreferences
        highScoreText.text = "High Score: $highScore" // Update high score text

        val newgame = findViewById<Button>(R.id.newgame)

        newgame.setOnClickListener {
            score = 0
            scoreText.text = "Score: $score"
            lives = 3 // Reset lives to 3
            initializeBricks()
            resetBallPosition() // Reset ball position and speed
            start()
            newgame.visibility = View.INVISIBLE
            gameOverText.visibility = View.INVISIBLE
            scoreAfterGameOverText.visibility = View.INVISIBLE
        }

    }

    private fun initializeBricks() {
        brickContainer.removeAllViews()

        visibleBricksCount = brickRows * brickColumns // Reset the count

        val brickWidthWithMargin = (brickWidth + brickMargin).toInt()

        for (row in 0 until brickRows) {
            val rowLayout = LinearLayout(this)
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            rowLayout.layoutParams = params

            for (col in 0 until brickColumns) {
                val brick = View(this)
                val brickParams = LinearLayout.LayoutParams(brickWidth, brickHeight)
                brickParams.setMargins(brickMargin, brickMargin, brickMargin, brickMargin)
                brick.layoutParams = brickParams

                // Randomly assign some bricks as special bricks
                val isSpecial = Random.nextBoolean()
                if (isSpecial) {
                    brick.setBackgroundResource(R.color.brown) // Set yellow background color for special bricks
                } else {
                    brick.setBackgroundResource(R.drawable.ic_launcher_background)
                }

                rowLayout.addView(brick)
            }

            brickContainer.addView(rowLayout)
        }
    }


    private fun moveBall() {
        ballX += ballSpeedX
        ballY += ballSpeedY

        ball.x = ballX
        ball.y = ballY
    }

    private fun movePaddle(x: Float) {
        paddleX = x - paddle.width / 2
        paddle.x = paddleX
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun checkCollision() {
        val screenWidth = resources.displayMetrics.widthPixels.toFloat()
        val screenHeight = resources.displayMetrics.heightPixels.toFloat()

        // Collision detection with screen edges
        if (ballX <= 0 || ballX + ball.width >= screenWidth) {
            ballSpeedX *= -1
        }
        if (ballY <= 0) {
            ballSpeedY *= -1
        }

        // Collision detection with paddle
        if (ballY + ball.height >= paddle.y && ballY + ball.height <= paddle.y + paddle.height
            && ballX + ball.width >= paddle.x && ballX <= paddle.x + paddle.width
        ) {
            ballSpeedY *= -1
            score++
            scoreText.text = "Score: $score"
        }

        // Collision detection with bottom screen edge (losing a life)
        if (ballY + ball.height >= screenHeight) {
            lives--
            if (lives > 0) {
                Toast.makeText(this, "$lives balls left", Toast.LENGTH_SHORT).show()
            }
            if (lives <= 0) {
                gameOver()
            } else {
                resetBallPosition()
                start()
            }
        }

        // Collision detection with bricks
        for (row in 0 until brickRows) {
            val rowLayout = brickContainer.getChildAt(row) as LinearLayout

            val rowTop = rowLayout.y + brickContainer.y
            val rowBottom = rowTop + rowLayout.height

            for (col in 0 until brickColumns) {
                val brick = rowLayout.getChildAt(col) as View

                if (brick.visibility == View.VISIBLE) {
                    val brickLeft = brick.x + rowLayout.x
                    val brickRight = brickLeft + brick.width
                    val brickTop = brick.y + rowTop
                    val brickBottom = brickTop + brick.height

                    if (ballX + ball.width >= brickLeft && ballX <= brickRight
                        && ballY + ball.height >= brickTop && ballY <= brickBottom
                    ) {
                        if (brick.background.constantState == resources.getDrawable(R.color.brown).constantState) {
                            score += 350 // Increase score by 300 for hitting a yellow brick
                        } else {
                            score += 70 // Increase score by 50 for hitting other bricks
                        }
                        scoreText.text = "Score: $score"
                        brick.visibility = View.INVISIBLE
                        ballSpeedY *= -1
                        visibleBricksCount--
                        // After updating the visibleBricksCount
                        println("Visible Bricks Count: $visibleBricksCount")

                        if (visibleBricksCount == 0) {
                            // All bricks are broken, show the trophy button
                            val trophyButton = findViewById<Button>(R.id.trophyButton)
                            trophyButton.visibility = View.VISIBLE
                            trophyButton.setOnClickListener {
                                // Navigate to the TrophyPage
                                val intent = Intent(this, trophyPage::class.java)
                                startActivity(intent)
                            }
                        }

                        if (score > highScore) {
                            highScore = score
                            highScoreText.text = "High Score: $highScore"
                            // Save the high score to SharedPreferences
                            with(sharedPreferences.edit()) {
                                putInt("highScore", highScore)
                                apply()
                            }
                        }
                        // Generate a random number to select a power-up
                        val random = Random.nextInt(0, powerUps.size)

                        applyPowerUp(powerUps[random])
                        return
                    }
                }
            }
        }

        // Check if all bricks are broken


    if (ballY + ball.height >= screenHeight) {
            lives--
            if (lives > 0) {
                Toast.makeText(this, "$lives balls left", Toast.LENGTH_SHORT).show()
            }

            paddle.setOnTouchListener { _, event ->
                when (event.action) {
                    MotionEvent.ACTION_MOVE -> {
                        movePaddle(event.rawX)
                    }
                }
                true
            }

            if (lives <= 0) {
                gameOver()
            } else {
                resetBallPosition()
                start()
            }
        }

        for (row in 0 until brickRows) {
            val rowLayout = brickContainer.getChildAt(row) as LinearLayout

            val rowTop = rowLayout.y + brickContainer.y
            val rowBottom = rowTop + rowLayout.height

            for (col in 0 until brickColumns) {
                val brick = rowLayout.getChildAt(col) as View

                if (brick.visibility == View.VISIBLE) {
                    val brickLeft = brick.x + rowLayout.x
                    val brickRight = brickLeft + brick.width
                    val brickTop = brick.y + rowTop
                    val brickBottom = brickTop + brick.height

                    if (ballX + ball.width >= brickLeft && ballX <= brickRight
                        && ballY + ball.height >= brickTop && ballY <= brickBottom
                    ) {
                        if (brick.background.constantState == resources.getDrawable(R.color.brown).constantState) {
                            score += 300 // Increase score by 10 for hitting a yellow brick
                        }
                        score=score+50
                        scoreText.text = "Score: $score"
                        brick.visibility = View.INVISIBLE
                        ballSpeedY *= -1
                        if (score > highScore) {
                            highScore = score
                            highScoreText.text = "High Score: $highScore"
                            // Save the high score to SharedPreferences
                            with(sharedPreferences.edit()) {
                                putInt("highScore", highScore)
                                apply()
                            }
                        }
                        // Generate a random number to select a power-up
                        val random = Random.nextInt(0, powerUps.size)

                        applyPowerUp(powerUps[random])
                        return
                    }

                }
            }
        }
    }


    // Method to apply power-up effects
    private fun applyPowerUp(powerUp: String) {
        when (powerUp) {
            "Paddle Size Increase" -> {
                val newPaddleWidth = paddle.width * 1.1f
                paddle.layoutParams.width = newPaddleWidth.toInt()
                paddle.requestLayout()
                // Reset paddle size to default after power-up duration
                Handler().postDelayed({
                    val defaultPaddleWidth = 100 // Default paddle width in dp
                    paddle.layoutParams.width = defaultPaddleWidth.dpToPx() // Convert dp to pixels
                    paddle.requestLayout()
                }, powerUpDuration)
            }
            "Big Ball" -> {
                if (!bigBallActive) {
                    bigBallActive = true
                    ball.scaleX *= 2
                    ball.scaleY *= 2
                    Handler().postDelayed({
                        bigBallActive = false
                        ball.scaleX /= 2
                        ball.scaleY /= 2
                    }, powerUpDuration) // Power-up duration
                }
            }
        }
        // Save the score to SharedPreferences whenever it changes
        with(sharedPreferences.edit()) {
            putInt("score", score)
            apply()
        }
    }

    private fun resetBallPosition() {
        val displayMetrics = resources.displayMetrics
        val screenDensity = displayMetrics.density

        val screenWidth = displayMetrics.widthPixels.toFloat()
        val screenHeight = displayMetrics.heightPixels.toFloat()

        ballX = screenWidth / 2 - ball.width / 2
        ballY = screenHeight / 2 - ball.height / 2 + 525

        ball.x = ballX
        ball.y = ballY

        // Reset ball speed to initial values
        val initialSpeed = calculateInitialSpeed()

        ballSpeedX = initialSpeed * screenDensity - 8
        ballSpeedY = -initialSpeed * screenDensity + 8

        paddleX = screenWidth / 2 - paddle.width / 2
        paddle.x = paddleX
    }


    private fun gameOver() {
        // Update the score text
        scoreText.text = "Score: $score"
        // Show Game Over message
        gameOverText.visibility = View.VISIBLE
        // Set score after game over
        scoreAfterGameOverText.text = "Score: $score"
        // Show score after game over
        scoreAfterGameOverText.visibility = View.VISIBLE
        // Save the score to SharedPreferences
        with(sharedPreferences.edit()) {
            putInt("score", score)
            apply()
        }
        // Show New Game button
        val newgame = findViewById<Button>(R.id.newgame)
        newgame.visibility = View.VISIBLE
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun movepaddle() {
        paddle.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_MOVE -> {
                    movePaddle(event.rawX)
                }
            }
            true
        }
    }

    private fun start() {
        movepaddle()
        val displayMetrics = resources.displayMetrics
        val screenDensity = displayMetrics.density

        val screenWidth = displayMetrics.widthPixels.toFloat()
        val screenHeight = displayMetrics.heightPixels.toFloat()

        paddleX = screenWidth / 2 - paddle.width / 2
        paddle.x = paddleX

        ballX = screenWidth / 2 - ball.width / 2
        ballY = screenHeight / 2 - ball.height / 2

        val brickHeightWithMargin = (brickHeight + brickMargin * screenDensity).toInt()

        // Reset ball speed to initial values
        val initialSpeed = calculateInitialSpeed()

        ballSpeedX = initialSpeed * screenDensity - 7
        ballSpeedY = -initialSpeed * screenDensity + 7

        val animator = ValueAnimator.ofFloat(0f, 1f)
        animator.duration = Long.MAX_VALUE
        animator.interpolator = LinearInterpolator()
        animator.addUpdateListener { animation ->
            moveBall()
            checkCollision()
        }
        animator.start()

        // Activate power-ups after a delay
        Handler().postDelayed({
            val randomPowerUp = powerUps.random()
            applyPowerUp(randomPowerUp)
        }, powerUpActivationDelay)

    }


    private fun calculateInitialSpeed(): Float {
        return 6f
    }

    override fun onPause() {
        super.onPause()
        if (score > highScore) {
            highScore = score
            highScoreText.text = "High Score: $highScore"
            // Save the new high score to SharedPreferences
            with(sharedPreferences.edit()) {
                putInt("highScore", highScore)
                apply()
            }
        }
    }

    // Method to convert dp to pixels
    private fun Int.dpToPx(): Int {
        val density = resources.displayMetrics.density
        return (this * density).toInt()
    }
}

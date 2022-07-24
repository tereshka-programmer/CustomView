package com.example.fullcustomview

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.fullcustomview.databinding.ActivityMainBinding
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    var isFirstPlayer = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.ticTacToeView.ticTacToeField = TicTacToeField(10,10)

        binding.ticTacToeView.actionListener = { row, column, field ->
            val cell = field.getCell(row, column)
            if (cell == Cell.EMPTY){
                if (isFirstPlayer) {
                    field.setCell(row, column, Cell.PLAYER_1)
                } else {
                    field.setCell(row, column, Cell.PLAYER_2)
                }
                isFirstPlayer = !isFirstPlayer
            }
        }
        binding.randomFieldButton.setOnClickListener {

        }
    }
}
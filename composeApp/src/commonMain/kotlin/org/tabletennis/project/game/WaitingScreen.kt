package org.tabletennis.project.game

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun WaitingScreen(playerNumber: Int) {
    val playerColor = when (playerNumber) {
        1 -> Color(0xFFF44336)
        2 -> Color(0xFF4CAF50)
        else -> Color.White
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF222222)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .width(320.dp)
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF333333)
            )
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    "You are $playerNumber",
                    fontSize = 20.sp,
                    color = playerColor,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(20.dp))
                
                Text(
                    "Waiting for second player...",
                    fontSize = 16.sp,
                    color = Color.White
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                CircularProgressIndicator(
                    color = playerColor,
                    strokeWidth = 4.dp,
                    modifier = Modifier.size(64.dp)
                )
                
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

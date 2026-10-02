package com.canoezequiel.moodflow.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.canoezequiel.moodflow.R

@Composable
fun AdditionalIncome(
    title: String,
    textButton: String,
    accionBurron: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HorizontalDivider(
                modifier = Modifier.weight(1f),
                thickness = 1.dp
            )
            Text("OR")
            HorizontalDivider(
                modifier = Modifier.weight(1f),
                thickness = 1.dp
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .padding(horizontal = 64.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            Image(
                painter = painterResource(
                    R.drawable.ic_google
                ),
                contentDescription = "Google"
            )
            Image(
                painter = painterResource(
                    R.drawable.ic_apple
                ),
                contentDescription = "Google"
            )
            Image(
                painter = painterResource(
                    R.drawable.ic_facebook
                ),
                contentDescription = "Google"
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth(),
        ) {
            Spacer(modifier.weight(1f))
            Text(title)
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = textButton,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .clickable(onClick = {accionBurron()})
            )
            Spacer(modifier.weight(1f))
        }
    }
}

@Preview(showSystemUi = true )
@Composable
fun PreviewAddIn() {
    AdditionalIncome(
        title = "Don't have an account?",
        textButton = "Sign up",
        accionBurron = {}
    )
}
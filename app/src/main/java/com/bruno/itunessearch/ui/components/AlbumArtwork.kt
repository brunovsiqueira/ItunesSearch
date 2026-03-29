package com.bruno.itunessearch.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.bruno.itunessearch.R

@Composable
fun AlbumArtwork(
    artworkUrl: String,
    albumName: String,
    modifier: Modifier = Modifier,
    size: Dp = 280.dp,
    cornerRadius: Dp = 16.dp,
) {
    AsyncImage(
        model = artworkUrl,
        contentDescription = stringResource(R.string.cd_album_artwork, albumName),
        contentScale = ContentScale.Crop,
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(cornerRadius)),
    )
}

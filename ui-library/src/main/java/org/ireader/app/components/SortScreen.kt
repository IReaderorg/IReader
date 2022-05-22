package org.ireader.app.components


//@Composable
//fun SortScreen(
//    sortType: SortType,
//    isSortDesc: Boolean,
//    onSortSelected: (SortType) -> Unit
//) {
//    val items = listOf<SortType>(
//        SortType.Alphabetically,
//        SortType.LastRead,
//        SortType.LastChecked,
//        SortType.TotalChapters,
//        SortType.LatestChapter,
//        SortType.DateFetched,
//        SortType.DateAdded,
//    )
//    Column(
//        Modifier
//            .fillMaxSize()
//            .background(MaterialTheme.colorScheme.background)
//            .padding(12.dp)
//    ) {
//        Column(
//            Modifier
//                .fillMaxSize()
//                .background(MaterialTheme.colorScheme.background)
//                .padding(12.dp),
//            horizontalAlignment = Alignment.Start,
//            verticalArrangement = Arrangement.Top
//        ) {
//            items.forEach { item ->
//
//                TextIcon(
//                    item.name,
//                    if (isSortDesc) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
//                    sortType == item,
//                    onClick = {
//                        onSortSelected(item)
//                    }
//                )
//                Spacer(modifier = Modifier.height(8.dp))
//            }
//        }
//    }
//}

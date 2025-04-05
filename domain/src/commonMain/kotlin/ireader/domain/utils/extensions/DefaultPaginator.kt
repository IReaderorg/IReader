

package ireader.domain.utils.extensions


class DefaultPaginator<Key, Item>(
    private val initialKey: Key,
    private val onInit: suspend () -> Unit = {}, //  Fixed: Removed 'inline'
    private  val onLoadUpdated: (Boolean) -> Unit,
    private  val onRequest: suspend (nextKey: Key) -> Result<Item>,
    private  val getNextKey: suspend (Item) -> Key,
    private  val onError: suspend (Throwable?) -> Unit,
    private  val onSuccess: suspend (items: Item, newKey: Key) -> Unit,
) : Paginator<Key, Item> {

    private var currentKey = initialKey
    private var isMakingRequest = false

    override suspend fun loadNextItems() {
        if (isMakingRequest) {
            return
        }
        isMakingRequest = true
        onLoadUpdated(true)
        if (currentKey == 1) {
            onInit()
        }
        val result = onRequest(currentKey)
        isMakingRequest = false
        val items = result.getOrElse {
            onError(it)
            onLoadUpdated(false)
            return
        }
        currentKey = getNextKey(items)
        onSuccess(items, currentKey)
        onLoadUpdated(false)
    }

    override fun reset() {
        currentKey = initialKey
    }
}

interface Paginator<Key, Item> {
    suspend fun loadNextItems()
    fun reset()
}

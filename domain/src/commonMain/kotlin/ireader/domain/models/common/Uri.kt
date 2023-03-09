package ireader.domain.models.common

expect class Uri {
    override fun toString(): String
    companion object {
        fun parse(uriString: String): Uri
    }
}
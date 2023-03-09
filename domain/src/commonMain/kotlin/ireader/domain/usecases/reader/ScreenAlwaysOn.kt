package ireader.domain.usecases.reader



interface ScreenAlwaysOn {
    operator fun invoke(enable: Boolean)
}

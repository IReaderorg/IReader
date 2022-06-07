package ir.kazemcodes.epub.epubvalidator

private typealias OnElementMissingListener = () -> Unit
private typealias OnAttributeMissingListener = (parentElement: String, attributeName: String) -> Unit

/**
 * Interface for validation .epub publication. Contains methods for handling
 * the missing of important elements in the .epub publication and handling method informing
 * when attribute are missing.
 */
interface ValidationListeners {

    /** Method for handling Metadata element missing. */
    fun onMetadataMissing()

    /** Method for handling Manifest element missing. */
    fun onManifestMissing()

    /** Method for handling Spine element missing. */
    fun onSpineMissing()

    /** Method for handling table of contents element missing. */
    fun onTableOfContentsMissing()

    /** Method for informing when attribute are missing.
     *
     * @param parentElement name of publication element where attributes are missing
     * @param attributeName name of missing attribute
     */
    fun onAttributeMissing(parentElement: String, attributeName: String)
}

/**
 * An implementation class for the ValidationListeners interface. Contains override
 * interface methods and setter methods which we give body to call
 * in the implemented methods.
 */
class ValidationListenersHelper : ValidationListeners {

    private var metadataMissing: (OnElementMissingListener)? = null
    private var manifestMissing: (OnElementMissingListener)? = null
    private var spineMissing: (OnElementMissingListener)? = null
    private var tableOfContentsMissing: (OnElementMissingListener)? = null
    private var attributeMissing: (OnAttributeMissingListener)? = null

    /**
     * A setter method that gives the body to call in the overridden
     * onMetadataMissing method.
     *
     * @param metadataMissing lambda expression to be called in an overridden method
     */
    fun setOnMetadataMissing(metadataMissing: OnElementMissingListener) {
        this.metadataMissing = metadataMissing
    }

    /**
     * A setter method that gives the body to call in the overridden
     * onManifestMissing method.
     *
     * @param manifestMissing lambda expression to be called in an overridden method
     */
    fun setOnManifestMissing(manifestMissing: OnElementMissingListener) {
        this.manifestMissing = manifestMissing
    }

    /**
     * A setter method that gives the body to call in the overridden
     * onSpineMissing method.
     *
     * @param spineMissing lambda expression to be called in an overridden method
     */
    fun setOnSpineMissing(spineMissing: OnElementMissingListener) {
        this.spineMissing = spineMissing
    }

    /**
     * A setter method that gives the body to call in the overridden
     * onTableOfContentsMissing method.
     *
     * @param tableOfContentsMissing lambda expression to be called in an overridden method
     */
    fun setOnTableOfContentsMissing(tableOfContentsMissing: OnElementMissingListener) {
        this.tableOfContentsMissing = tableOfContentsMissing
    }

    /**
     * A setter method that gives the body to call in the overridden
     * onAttributeMissing method.
     *
     * @param attributeMissing lambda expression to be called in an overridden method
     */
    fun setOnAttributeMissing(attributeMissing: OnAttributeMissingListener) {
        this.attributeMissing = attributeMissing
    }

    override fun onMetadataMissing() {
        metadataMissing?.invoke()
    }

    override fun onManifestMissing() {
        manifestMissing?.invoke()
    }

    override fun onSpineMissing() {
        spineMissing?.invoke()
    }

    override fun onTableOfContentsMissing() {
        tableOfContentsMissing?.invoke()
    }

    override fun onAttributeMissing(parentElement: String, attributeName: String) {
        attributeMissing?.invoke(parentElement, attributeName)
    }

}



